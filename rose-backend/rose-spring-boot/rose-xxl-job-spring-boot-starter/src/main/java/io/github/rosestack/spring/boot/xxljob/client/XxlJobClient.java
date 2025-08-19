package io.github.rosestack.spring.boot.xxljob.client;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rosestack.core.util.JsonUtils;
import io.github.rosestack.core.util.StringPool;
import io.github.rosestack.spring.boot.xxljob.client.model.*;
import io.github.rosestack.spring.boot.xxljob.config.XxlJobProperties;
import java.net.URI;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class XxlJobClient {
    private final RestTemplate restTemplate;
    private final XxlJobProperties props;

    private String baseUrl() {
        // 取第一个 admin 地址作为管理端 base
        String admin = props.getAdminAddresses();
        if (admin == null || admin.isEmpty()) {
            throw new IllegalStateException("rose.xxl-job.admin-addresses 未配置");
        }
        String[] parts = admin.split(StringPool.COMMA);
        String base = parts[0].trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return base;
    }

    public XxlJobInfo addJob(XxlJobInfo job) {
        Objects.requireNonNull(job, "参数不能为空");
        return postForXxlContent(baseUrl() + "/jobinfo/add", toForm(job), XxlJobInfo.class);
    }

    public XxlJobInfo updateJob(XxlJobInfo job) {
        Objects.requireNonNull(job, "参数不能为空");
        return postForXxlContent(baseUrl() + "/jobinfo/update", toForm(job), XxlJobInfo.class);
    }

    public void removeJob(long id) {
        MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        f.add("id", String.valueOf(id));
        postForXxlContent(baseUrl() + "/jobinfo/remove", f, Void.class);
    }

    public void startJob(long id) {
        MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        f.add("id", String.valueOf(id));
        postForXxlContent(baseUrl() + "/jobinfo/start", f, Void.class);
    }

    public void stopJob(long id) {
        MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        f.add("id", String.valueOf(id));
        postForXxlContent(baseUrl() + "/jobinfo/stop", f, Void.class);
    }

    public XxlJobInfoPage pageJobInfo(long jobGroupId, String executorHandler) {
        LinkedMultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        f.add("jobGroup", String.valueOf(jobGroupId));
        if (executorHandler != null) f.add("executorHandler", executorHandler);
        return postForXxlContent(baseUrl() + "/jobinfo/pageList", f, XxlJobInfoPage.class);
    }

    public XxlJobInfo getJob(long id) {
        LinkedMultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        f.add("id", String.valueOf(id));
        return postForXxlContent(baseUrl() + "/jobinfo/loadById", f, XxlJobInfo.class);
    }

    public XxlJobGroup addJobGroup(String appname) {
        LinkedMultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        f.add("appname", appname);
        return postForXxlContent(baseUrl() + "/jobgroup/save", f, XxlJobGroup.class);
    }

    public XxlJobGroupPage pageJobGroup(String appName) {
        MultiValueMap<String, String> f = new LinkedMultiValueMap<>();
        if (appName != null) f.add("appname", appName);
        return postForXxlContent(baseUrl() + "/jobgroup/pageList", f, XxlJobGroupPage.class);
    }

    // 新增：带内容泛型的解析，返回完整响应（表单提交）
    private <T> XxlRestResponse<T> postForXxlResponse(
            String url, MultiValueMap<String, String> form, Class<T> contentClass) {
        RequestEntity<MultiValueMap<String, String>> req = RequestEntity.post(URI.create(url))
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .accept(MediaType.APPLICATION_JSON)
                .body(form);
        ResponseEntity<String> respEntity = restTemplate.exchange(req, String.class);

        ObjectMapper mapper = JsonUtils.getObjectMapper();
        try {
            JavaType type = mapper.getTypeFactory().constructParametricType(XxlRestResponse.class, contentClass);
            XxlRestResponse<T> resp = mapper.readValue(respEntity.getBody(), type);
            if (resp == null) {
                throw new IllegalStateException("XXL-Job 调用返回空: " + url);
            }
            Integer code = resp.getCode();
            if (code == null || code != 200) {
                throw new IllegalStateException("XXL-Job 调用失败: " + resp.getMsg());
            }
            return resp;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "The given string value cannot be transformed to Json object: " + respEntity.getBody(), e);
        }
    }

    // 新增：直接返回 content 内容（表单提交）
    private <T> T postForXxlContent(String url, MultiValueMap<String, String> form, Class<T> contentClass) {
        XxlRestResponse<T> r = postForXxlResponse(url, form, contentClass);
        return r.getContent();
    }

    private static MultiValueMap<String, String> toForm(XxlJobInfo obj) {
        return JsonUtils.fromString(JsonUtils.toString(obj), MultiValueMap.class);
    }
}
