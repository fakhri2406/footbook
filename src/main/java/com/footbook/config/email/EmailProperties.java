package com.footbook.config.email;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "mail")
@Validated
@Data
public class EmailProperties {
    @NotBlank
    private String from;
}
