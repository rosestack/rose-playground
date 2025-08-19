package io.github.rosestack.spring.boot.xxljob.client;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.rosestack.core.util.JsonUtils;
import io.github.rosestack.spring.boot.xxljob.client.model.*;
import lombok.RequiredArgsConstructor;
import io.github.rosestack.spring.boot.xxljob.config.XxlJobProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 * 极简 XXL-Job Admin 客户端（任务 CRUD）
 * 说明：XXL-Job Admin 公开的是基于表单的管理接口，此处仅封装最小能力
 */
@RequiredArgsConstructor
public class XxlJobClient {
	private final RestTemplate restTemplate;
	private final XxlJobProperties props;

	private String baseUrl() {
		// 取第一个 admin 地址作为管理端 base
		String admin = props.getAdminAddresses();
		if (admin == null || admin.isEmpty()) throw new IllegalStateException("rose.xxl-job.admin-addresses 未配置");
		String[] parts = admin.split(",");
		String base = parts[0].trim();
		if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
		return base;
	}

	public XxlRestResponse<?> addJob(XxlJobInfo job) {
		Objects.requireNonNull(job, "job");
		return postForXxlResponse(baseUrl() + "/jobinfo/add", JsonUtils.toString(job));
	}

	public XxlRestResponse<?> updateJob(XxlJobInfo job) {
		Objects.requireNonNull(job, "job");
		return postForXxlResponse(baseUrl() + "/jobinfo/update", JsonUtils.toString(job));
	}

	public XxlRestResponse<?> removeJob(long id) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		f.add("id", String.valueOf(id));
		return postForXxlResponse(baseUrl() + "/jobinfo/remove", JsonUtils.toString(f));
	}

	public XxlRestResponse<?> startJob(long id) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		f.add("id", String.valueOf(id));
		return postForXxlResponse(baseUrl() + "/jobinfo/start", JsonUtils.toString(f));
	}

	public XxlRestResponse<?> stopJob(long id) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		f.add("id", String.valueOf(id));
		return postForXxlResponse(baseUrl() + "/jobinfo/stop", JsonUtils.toString(f));
	}

	public XxlJobInfoPage pageJobList(int start, int length, Integer jobGroup, String executorHandler, String filterTime) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		f.add("start", String.valueOf(start));
		f.add("length", String.valueOf(length));
		if (jobGroup != null) f.add("jobGroup", String.valueOf(jobGroup));
		if (executorHandler != null) f.add("executorHandler", executorHandler);
		if (filterTime != null) f.add("filterTime", filterTime);
		return postFor(baseUrl() + "/jobinfo/pageList", JsonUtils.toString(f), XxlJobInfoPage.class);
	}

	public XxlJobInfo getJob(long id) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		f.add("id", String.valueOf(id));
		return postFor(baseUrl() + "/jobinfo/loadById", JsonUtils.toString(f), XxlJobInfo.class);
	}

	// job group create
	public XxlRestResponse<?> addJobGroup(String appname) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		if (appname != null) {
			f.add("appname", appname);
		}
		return postForXxlResponse(baseUrl() + "/jobgroup/save", JsonUtils.toString(f));
	}

	public XxlJobGroupPage pageJobGroupList(String appName) {
		MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
		if (appName != null) f.add("appname", appName);
		return postFor(baseUrl() + "/jobgroup/pageList", JsonUtils.toString(f), XxlJobGroupPage.class);
	}

	private XxlRestResponse<?> postForXxlResponse(String url, String form) {
		RequestEntity<String> req = RequestEntity
			.post(URI.create(url))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(form);
		var resp = restTemplate.exchange(req, String.class);
		return JsonUtils.fromString(resp.getBody(), XxlRestResponse.class);
	}

	private <T> T postFor(String url, String form, Class<T> clazz) {
		RequestEntity<String> req = RequestEntity
			.post(URI.create(url))
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(form);
		var resp = restTemplate.exchange(req, String.class);
		return JsonUtils.fromString(resp.getBody(), clazz);
	}
}

