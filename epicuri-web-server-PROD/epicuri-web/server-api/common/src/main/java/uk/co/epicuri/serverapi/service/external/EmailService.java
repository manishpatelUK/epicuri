package uk.co.epicuri.serverapi.service.external;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.co.epicuri.serverapi.common.pojo.ControllerUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class EmailService {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    @Value("${email.api.key}")
    private String mailgunApiKey;

    @Value("${email.api.domain}")
    private String mailgunDomain;

    public IMailBuilder newSimpleMailBuilder() {
        return new SimpleMailBuilder();
    }

    public IMailBuilder newHTMLMailBuilder() {
        return new MultipartEmailBuilder();
    }

    private abstract class MailBuilder implements IMailBuilder {
        protected final RestTemplate restTemplate;
        protected final EmailPojo email;

        public MailBuilder() {
            restTemplate = new RestTemplate();
            email = new EmailPojo();
        }

        @Override
        public IMailBuilder from(String name, String emailAddress) {
            email.setFrom(String.format("%s <%s>", name, emailAddress));
            return MailBuilder.this;
        }

        @Override
        public IMailBuilder to(String emailAddress) {
            email.getTo().add(emailAddress);
            return MailBuilder.this;
        }

        @Override
        public IMailBuilder subject(String subject) {
            email.setSubject(subject);
            return MailBuilder.this;
        }

        @Override
        public IMailBuilder body(String body) {
            email.setText(body);
            return MailBuilder.this;
        }

        @Override
        public IMailBuilder from(String name) {
            if(ControllerUtil.EMAIL_REGEX.matcher(name).find()) {
                email.setFrom(name);
            } else {
                email.setFrom(String.format("%s <%s@epicuri.email>", name, name));
            }
            return MailBuilder.this;
        }

        protected boolean sendEmail(RequestEntity<MultiValueMap> requestEntity) {
            try {
                ResponseEntity response = restTemplate.exchange(requestEntity, Object.class);
                return response.getStatusCode() == HttpStatus.OK;
            } catch (Exception ex) {
                ex.printStackTrace();
                LOGGER.error("Could not send email ", ex);
                return false;
            }
        }
    }

    public class SimpleMailBuilder extends MailBuilder {

        @Override
        public IMailBuilder attach(File file) {
            throw new IllegalArgumentException("Not currently supported");
        }

        @Override
        public IMailBuilder attach(InputStream inputStream) {
            throw new IllegalArgumentException("Not currently supported");
        }

        @Override
        public boolean build() {
            LOGGER.debug("Attempt send email: " + email);

            String credentials = "api:"+mailgunApiKey;
            String encoded = Base64Utils.encodeToString(credentials.getBytes());
            RequestEntity<MultiValueMap> requestEntity = RequestEntity
                    .post(URI.create(String.format("https://api.mailgun.net/v3/%s/messages", mailgunDomain)))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(email.toMap(false));

            return sendEmail(requestEntity);
        }
    }

    public class MultipartEmailBuilder extends MailBuilder {

        @Override
        public IMailBuilder attach(File file) {
            email.getAttachments().add(file);
            return this;
        }

        @Override
        public IMailBuilder attach(InputStream inputStream) {
            email.getAttachmentStreams().add(inputStream);
            return this;
        }

        @Override
        public boolean build() {
            LOGGER.debug("Attempt send email: " + email);

            restTemplate.getMessageConverters().add(new FormHttpMessageConverter());
            String credentials = "api:"+mailgunApiKey;
            String encoded = Base64Utils.encodeToString(credentials.getBytes());
            RequestEntity<MultiValueMap> requestEntity = RequestEntity
                    .post(URI.create(String.format("https://api.mailgun.net/v3/%s/messages", mailgunDomain)))
                    .header(HttpHeaders.AUTHORIZATION, "Basic " + encoded)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(email.toMap(true));

            return sendEmail(requestEntity);
        }
    }

    public IMailBuilder createBuilder(String subject, String to, String fromName, String fromEmail, String fileTemplate, String alternativeBody, Map<String,String> replacements) {
        return createBuilder(subject, to, fromName, fromEmail, fileTemplate, alternativeBody, replacements, true);
    }

    public IMailBuilder createBuilder(String subject, String to, String fromName, String fromEmail, String fileTemplate, String alternativeBody, Map<String,String> replacements, boolean html) {
        ClassLoader classLoader = getClass().getClassLoader();
        String contents;
        try {
            contents = IOUtils.toString(classLoader.getResourceAsStream(fileTemplate));
            contents = replaceAll(contents, replacements);
        } catch (Exception e) {
            LOGGER.trace("Couldn't email using template due to: {}",e.getMessage(), e);
            contents = replaceAll(alternativeBody, replacements);
        }

        return html ? htmlEmail(subject, to, fromName, fromEmail, contents) : simpleEmail(subject, to, fromName, fromEmail, contents);
    }

    public IMailBuilder simpleEmail(String subject, String to, String fromName, String fromEmail, String body) {
        return newSimpleMailBuilder().subject(subject).to(to).from(fromName, fromEmail).body(body);
    }

    public IMailBuilder htmlEmail(String subject, String to, String fromName, String fromEmail, String body) {
        return newHTMLMailBuilder().subject(subject).to(to).from(fromName, fromEmail).body(body);
    }

    private String replaceAll(String content, Map<String,String> replacements) {
        for(Map.Entry<String,String> entry : replacements.entrySet()) {
            content = content.replaceAll(Pattern.quote(entry.getKey()), entry.getValue() == null ? "" : entry.getValue());
        }

        return content;
    }

    private class EmailPojo {
        private String from;
        private List<String> to = new ArrayList<>();
        private List<File> attachments = new ArrayList<>();
        private List<InputStream> attachmentStreams = new ArrayList<>();
        private String subject = "";
        private String text = "";

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public List<String> getTo() {
            return to;
        }

        public void setTo(List<String> to) {
            this.to = to;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<File> getAttachments() {
            return attachments;
        }

        public void setAttachments(List<File> attachments) {
            this.attachments = attachments;
        }

        public List<InputStream> getAttachmentStreams() {
            return attachmentStreams;
        }

        public void setAttachmentStreams(List<InputStream> attachmentStreams) {
            this.attachmentStreams = attachmentStreams;
        }

        public MultiValueMap<String,Object> toMap(boolean html) {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.add("from", from);
            to.stream().distinct().forEach(x -> params.add("to", x));
            if(html) {
                params.add("html", text);
            } else {
                params.add("text", text);
            }
            params.add("subject", subject);

            attachments.stream().forEach(x -> params.add("attachment", new FileSystemResource(x)));
            attachmentStreams.stream().forEach(x -> params.add("attachment", new InputStreamResource(x)));

            return params;
        }

        @Override
        public String toString() {
            return ReflectionToStringBuilder.toString(this);
        }
    }
}
