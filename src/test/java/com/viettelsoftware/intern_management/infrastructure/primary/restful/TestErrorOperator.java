package com.viettelsoftware.intern_management.infrastructure.primary.restful;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-errors")
public class TestErrorOperator {

    @GetMapping("/data-integrity")
    public void dataIntegrity() {
        throw new DataIntegrityViolationException("duplicate key", new RuntimeException("users_username_key"));
    }

    @GetMapping("/generic")
    public void generic() {
        throw new IllegalStateException("boom");
    }
}

