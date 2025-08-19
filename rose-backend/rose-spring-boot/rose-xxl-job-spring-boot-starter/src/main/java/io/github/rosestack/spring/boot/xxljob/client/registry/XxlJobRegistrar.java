package io.github.rosestack.spring.boot.xxljob.client.registry;

import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.rosestack.spring.boot.xxljob.client.annotation.XxlJobRegister;
import io.github.rosestack.spring.boot.xxljob.client.XxlJobClient;
import io.github.rosestack.spring.boot.xxljob.client.model.XxlJobGroup;
import io.github.rosestack.spring.boot.xxljob.client.model.XxlJobGroupPage;
import io.github.rosestack.spring.boot.xxljob.client.model.XxlJobInfo;
import io.github.rosestack.spring.boot.xxljob.client.model.XxlJobInfoPage;
import io.github.rosestack.spring.boot.xxljob.config.XxlJobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 启动时扫描 @XxlRegister + @XxlJob，自动在 admin 侧创建任务（若不存在）
 */
@Slf4j
@RequiredArgsConstructor
public class XxlJobRegistrar implements ApplicationContextAware, SmartInitializingSingleton {

	private ApplicationContext applicationContext;
	private final XxlJobClient client;
	private final XxlJobProperties props;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterSingletonsInstantiated() {
		Integer jobGroupId;
		try {
			jobGroupId = findOrCreateJobGroupId(props.getAppname());
		} catch (Exception e) {
			log.warn("XXL-Job register failed: app={}, err={}", props.getAppname(), e.toString());
			return;
		}
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for (String name : beanNames) {
			Object bean = applicationContext.getBean(name);
			processBean(bean, jobGroupId);
		}
	}

	private void processBean(Object bean, int jobGroupId) {
		Class<?> targetClass = AopUtils.getTargetClass(bean);
		for (Method m : targetClass.getDeclaredMethods()) {
			processMethod(m, jobGroupId);
		}
	}

	private void processMethod(Method m, int jobGroupId) {
		XxlJob xxlJob = m.getAnnotation(XxlJob.class);
		XxlJobRegister reg = m.getAnnotation(XxlJobRegister.class);
		if (xxlJob == null || reg == null) return;
		registerOrUpdateJob(xxlJob.value(), reg, jobGroupId);
	}

	private void registerOrUpdateJob(String handler, XxlJobRegister reg, int jobGroupId) {
		try {
			// 1) 查找是否已存在同名 handler 的任务
			XxlJobInfo exist = findJobByHandler(jobGroupId, handler);

			// 2) 组装 JobInfo 并创建/更新
			XxlJobInfo job = buildJobInfo(jobGroupId, handler, reg);
			XxlJobInfo saved;
			if (exist == null) {
				saved = client.addJob(job);
				log.info("XXL-Job created, handler={}, id={}", handler, saved != null ? saved.getId() : null);
			} else {
				job.setId(exist.getId());
				saved = client.updateJob(job);
				log.info("XXL-Job updated, handler={}, id={}", handler, saved != null ? saved.getId() : null);
			}

			// 3) 按需自动启动
			startIfNeeded(reg, saved, handler);
		} catch (Exception e) {
			log.warn("XXL-Job register failed: app={}, handler={}, err={}", props.getAppname(), handler, e.toString());
		}
	}

	private XxlJobInfo buildJobInfo(int jobGroupId, String handler, XxlJobRegister reg) {
		XxlJobInfo job = new XxlJobInfo();
		job.setJobGroup(jobGroupId);
		job.setExecutorHandler(handler);
		job.setJobDesc(reg.jobDesc().isEmpty() ? handler : reg.jobDesc());
		job.setAuthor(StringUtils.defaultIfBlank(
			reg.author(), props.getClient().getDefaults().getAuthor()));
		job.setAlarmEmail(StringUtils.defaultIfBlank(
			reg.alarmEmail(), props.getClient().getDefaults().getAlarmEmail()));
		job.setScheduleConf(reg.cron());
		job.setExecutorRouteStrategy(reg.executorRouteStrategy());
		job.setExecutorParam(reg.executorParam());
		// 可选的默认/保守设置
		io.github.rosestack.spring.boot.xxljob.config.XxlJobProperties.Client.Defaults defaults = props.getClient().getDefaults();
		job.setGlueType(defaults.getGlueType());
		job.setScheduleType(defaults.getScheduleType());
		job.setMisfireStrategy(defaults.getMisfireStrategy());
		job.setExecutorBlockStrategy(defaults.getExecutorBlockStrategy());
		job.setExecutorTimeout(defaults.getExecutorTimeout());
		job.setExecutorFailRetryCount(defaults.getExecutorFailRetryCount());
		job.setTriggerStatus(reg.autoStart() ? 1 : 0);
		return job;
	}

	private void startIfNeeded(XxlJobRegister reg, XxlJobInfo saved, String handler) {
		if (!reg.autoStart() || saved == null || saved.getId() <= 0) {
			return;
		}
		try {
			client.startJob(saved.getId());
			log.info("XXL-Job started, handler={}, id={}", handler, saved.getId());
		} catch (Exception startEx) {
			log.warn("XXL-Job start failed, handler={}, id={}, err={}", handler, saved.getId(), startEx.toString());
		}
	}

	private Integer findOrCreateJobGroupId(String appname) {
		Integer groupId = queryGroupId(appname);
		if (groupId != null) return groupId;

		XxlJobGroup created = client.addJobGroup(appname);
		if (created != null && created.getId() > 0) {
			return created.getId();
		}

		// 兼容有些 admin 不返回 id 的情况，再查一次
		groupId = queryGroupId(appname);
		if (groupId == null) {
			throw new RuntimeException("执行器 " + appname + " 没有注册");
		}
		return groupId;
	}

	private Integer queryGroupId(String appname) {
		XxlJobGroupPage page = client.pageJobGroup(appname);
		if (page == null || page.getData() == null) return null;
		return page.getData().stream()
				.filter(it -> appname.equals(it.getAppname()))
				.map(XxlJobGroup::getId)
				.findFirst().orElse(null);
	}

	private XxlJobInfo findJobByHandler(Integer jobGroupId, String handler) {
		XxlJobInfoPage page = client.pageJobInfo(jobGroupId, handler);
		List<XxlJobInfo> list = page != null ? page.getData() : null;
		if (list == null || list.isEmpty()) return null;
		return list.get(0);
	}
}

