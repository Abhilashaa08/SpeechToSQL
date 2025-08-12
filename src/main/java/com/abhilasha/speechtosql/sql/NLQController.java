package com.abhilasha.speechtosql.sql;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class NLQController {

    private final NLQService service;

    public NLQController(NLQService service) { this.service = service; }

    @PostMapping("/nlq")
    public ResponseEntity<?> nlq(@RequestBody Map<String, String> body) {
        try {
            String query = body.getOrDefault("q", "");
            return ResponseEntity.ok(service.query(query));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
