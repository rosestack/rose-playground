package io.github.rosestack.spring.boot.xxljob.registry;

import com.xxl.job.core.handler.annotation.XxlJob;
import io.github.rosestack.spring.boot.xxljob.annotation.XxlRegister;
import io.github.rosestack.spring.boot.xxljob.client.XxlJobClient;
import io.github.rosestack.spring.boot.xxljob.client.model.XxlJobGroupPage;
import io.github.rosestack.spring.boot.xxljob.config.XxlJobProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Method;

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
		String[] beanNames = applicationContext.getBeanDefinitionNames();
		for (String name : beanNames) {
			Object bean = applicationContext.getBean(name);
			Class<?> targetClass = AopUtils.getTargetClass(bean);
			for (Method m : targetClass.getDeclaredMethods()) {
				XxlJob xxlJob = m.getAnnotation(XxlJob.class);
				XxlRegister reg = m.getAnnotation(XxlRegister.class);
				if (xxlJob == null || reg == null) continue;

				String handler = xxlJob.value();
				try {
					// 查询是否存在：通过 group 列表匹配 appname
					XxlJobGroupPage page = client.pageJobGroupList(props.getAppname());
					boolean exists = page != null && page.getData() != null && page.getData().stream().anyMatch(it -> props.getAppname().equals(it.getAppname()));
					if (!exists) {
						// 不存在执行器组，则先创建
						client.addJobGroup(props.getAppname());
					}

				} catch (Exception e) {
					log.warn("XXL-Job register failed: handler={}, err={}", handler, e.toString());
				}
			}
		}
	}
}

