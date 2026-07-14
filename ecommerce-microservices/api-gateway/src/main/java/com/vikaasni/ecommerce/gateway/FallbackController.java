package com.vikaasni.ecommerce.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class FallbackController {
    @RequestMapping("/fallback/{service}")
    public ResponseEntity<Map<String, String>> fallback(@PathVariable String service) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", service + " is temporarily unavailable"));
    }
}
