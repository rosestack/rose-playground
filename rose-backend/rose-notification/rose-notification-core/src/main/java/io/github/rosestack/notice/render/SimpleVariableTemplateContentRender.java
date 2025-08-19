package io.github.rosestack.notice.render;

import io.github.rosestack.notice.NoticeException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleVariableTemplateContentRender extends AbstractTemplateContentRender {
    @Override
    public String doRender(String content, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return content;
        }
        // 简单变量替换，实际可用模板引擎
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            content = content.replace("${" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return content;
    }

    @Override
    public void validate(String content, Map<String, Object> variables) {
        Set<String> requiredVars = extractTemplateVariables(content);
        Set<String> providedVars = variables != null ? variables.keySet() : new HashSet<>();
        Set<String> missing = new HashSet<>(requiredVars);
        missing.removeAll(providedVars);
        if (!missing.isEmpty()) {
            throw new NoticeException("通知模版缺少变量: " + missing);
        }
    }

    private Set<String> extractTemplateVariables(String content) {
        Set<String> vars = new HashSet<>();
        // 支持字母、数字、下划线、点、连字符
        Matcher matcher = Pattern.compile("\\$\\{([\\w.\\-]+)}").matcher(content);
        while (matcher.find()) {
            vars.add(matcher.group(1));
        }
        return vars;
    }
}
