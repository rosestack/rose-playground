package io.github.rosestack.notice.sender;

import io.github.rosestack.notice.NoticeException;
import io.github.rosestack.notice.NoticeRetryableException;
import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.SenderConfiguration;
import io.github.rosestack.notice.spi.AbstractConfigure;
import io.github.rosestack.notice.spi.Sender;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * 邮件发送渠道实现，支持 HTML、抄送、附件。
 * channelConfig 需包含 mail.smtp.host/username/password/port/from。
 */
@Slf4j
public class EmailSender extends AbstractConfigure implements Sender {
    private volatile Session session;

    @Override
    public String getChannelType() {
        return EMAIL;
    }

    public void doConfigure(SenderConfiguration config) {
        String host = (String) config.getConfig().get("mail.smtp.host");
        String username = config.getConfig().get("mail.smtp.username").toString();
        String password = config.getConfig().get("mail.smtp.password").toString();
        int port = config.getConfig().get("mail.smtp.port") != null
                ? (int) config.getConfig().get("mail.smtp.port")
                : 25;

        if (host == null || username == null || password == null) {
            throw new NoticeException("SMTP 配置不完整");
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put(
                "mail.smtp.auth",
                String.valueOf(config.getConfig().getOrDefault("mail.smtp.auth", true)));
        props.put(
                "mail.smtp.starttls.enable",
                String.valueOf(
                        config.getConfig().getOrDefault("mail.smtp.starttls.enable", true)));
        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public String send(SendRequest request) {
        String from = config.getConfig().get("mail.smtp.from").toString();

        try {
            doSend(request, session, config.getConfig());
        } catch (MessagingException e) {
            throw new NoticeRetryableException("邮件发送失败", e);
        }

        return request.getRequestId();
    }

    @Override
    public void destroy() {
    }

    private static void doSend(SendRequest request, Session session, Map<String, Object> config) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        String from = config.get("mail.smtp.from").toString();
        if (from != null) {
            message.setFrom(new InternetAddress(from));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTarget()));
        List<String> ccList = request.getCc();
        if (ccList != null && !ccList.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(String.join(",", ccList)));
        }
        String subject = config.get("mail.smtp.subject").toString();
        message.setSubject(subject != null ? subject : "通知");

        Multipart multipart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(request.getTemplateContent(), "text/html; charset=utf-8");
        multipart.addBodyPart(textPart);
        message.setContent(multipart);
        Transport.send(message);
    }
}
