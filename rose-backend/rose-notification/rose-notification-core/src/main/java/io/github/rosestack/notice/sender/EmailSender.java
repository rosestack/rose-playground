package io.github.rosestack.notice.sender;

import io.github.rosestack.notice.NoticeException;
import io.github.rosestack.notice.NoticeRetryableException;
import io.github.rosestack.notice.SendRequest;
import io.github.rosestack.notice.SenderConfiguration;
import io.github.rosestack.notice.spi.AbstractConfigure;
import io.github.rosestack.notice.spi.Sender;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/** 邮件发送渠道实现，支持 HTML、抄送、附件。 channelConfig 需包含 mail.smtp.host/username/password/port/from。 */
public class EmailSender extends AbstractConfigure implements Sender {
    private volatile Session session;

    @Override
    public String getChannelType() {
        return EMAIL;
    }

    public void doConfigure(SenderConfiguration config) {
        Object hostObj = config.getConfig().get("mail.smtp.host");
        Object usernameObj = config.getConfig().get("mail.smtp.username");
        Object passwordObj = config.getConfig().get("mail.smtp.password");
        if (hostObj == null || usernameObj == null || passwordObj == null) {
            throw new NoticeException("SMTP 配置不完整");
        }
        String host = hostObj.toString();
        String username = usernameObj.toString();
        String password = passwordObj.toString();
        Object portObj = config.getConfig().get("mail.smtp.port");
        int port = portObj != null ? Integer.parseInt(String.valueOf(portObj)) : 25;

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", String.valueOf(config.getConfig().getOrDefault("mail.smtp.auth", true)));
        props.put(
                "mail.smtp.starttls.enable",
                String.valueOf(config.getConfig().getOrDefault("mail.smtp.starttls.enable", true)));
        props.put(
                "mail.smtp.ssl.enable", String.valueOf(config.getConfig().getOrDefault("mail.smtp.ssl.enable", false)));
        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    @Override
    public String send(SendRequest request) {
        try {
            doSend(request, session, config.getConfig());
        } catch (MessagingException e) {
            throw new NoticeRetryableException("邮件发送失败", e);
        }

        return request.getRequestId();
    }

    @Override
    public void destroy() {}

    private static void doSend(SendRequest request, Session session, Map<String, Object> config)
            throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        Object fromObj = config.get("mail.smtp.from");
        if (fromObj != null) {
            String from = fromObj.toString();
            message.setFrom(new InternetAddress(from));
        }
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(request.getTarget()));
        List<String> ccList = request.getCc();
        if (ccList != null && !ccList.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(String.join(",", ccList)));
        }
        Object subjObj = config.get("mail.smtp.subject");
        String subject = subjObj != null ? subjObj.toString() : "通知";
        message.setSubject(subject);

        MimeMultipart multipart = new MimeMultipart();
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(request.getTemplateContent(), "text/html; charset=utf-8");
        multipart.addBodyPart(textPart);
        message.setContent(multipart);
        Transport.send(message);
    }
}
