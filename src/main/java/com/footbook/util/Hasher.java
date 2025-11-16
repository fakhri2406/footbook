package com.footbook.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Hasher {
    private final BCryptPasswordEncoder passwordEncoder;

    public Hasher() {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    public String hash(String password) {
        return passwordEncoder.encode(password);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
