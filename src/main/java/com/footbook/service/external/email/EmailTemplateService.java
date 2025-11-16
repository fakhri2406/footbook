package com.footbook.service.external.email;

import com.footbook.exception.EmailSendException;
import com.footbook.util.ErrorMessages;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmailTemplateService {
    public String render(String templateName, Map<String, String> variables) {
        try {
            String template = loadTemplate(templateName);

            for (Map.Entry<String, String> entry : variables.entrySet()) {
                template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

            return template;
        } catch (IOException e) {
            log.error("Failed to load email template: {}", templateName, e);
            throw new EmailSendException(ErrorMessages.FAILED_TO_LOAD_TEMPLATE, e);
        }
    }

    private String loadTemplate(String templateName) throws IOException {
        String templatePath = "templates/email/" + templateName + ".html";

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (inputStream == null) {
                throw new IOException("Template not found: " + templatePath);
            }

            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        }
    }
}
