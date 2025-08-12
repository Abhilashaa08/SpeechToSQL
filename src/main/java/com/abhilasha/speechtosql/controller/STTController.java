package com.abhilasha.speechtosql.controller;

import com.abhilasha.speechtosql.service.STTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class STTController {

    private static final Logger log = LoggerFactory.getLogger(STTController.class);

    private final STTService stt;

    public STTController(STTService stt) {
        this.stt = stt;
    }

    /** Quick ping to verify the backend is reachable from the browser */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of("ok", true, "service", "stt", "version", 1);
    }

    /**
     * Accepts audio uploaded as multipart under either 'file' or 'audio'.
     * Adds detailed logging so we can see size/content-type in the server logs.
     */
    @PostMapping(
            value = "/stt",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> stt(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "audio", required = false) MultipartFile audio
    ) {
        try {
            MultipartFile in = (file != null && !file.isEmpty()) ? file : audio;

            if (in == null || in.isEmpty()) {
                log.warn("STT upload is empty (file={}, audio={})", fileInfo(file), fileInfo(audio));
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "No audio received. Ensure you click Stop before Transcribe.",
                        "details", Map.of(
                                "file", fileInfo(file),
                                "audio", fileInfo(audio)
                        )));
            }

            log.info("STT upload received: name='{}' size={} bytes contentType='{}'",
                    safeName(in), in.getSize(), in.getContentType());

            String transcript = stt.transcribe(in);

            log.info("STT transcript: {}", transcript == null ? "(null)" :
                    (transcript.length() > 160 ? transcript.substring(0, 160) + "…" : transcript));

            return ResponseEntity.ok(Map.of("transcript", transcript == null ? "" : transcript));

        } catch (Exception e) {
            log.error("STT failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Debug endpoint: lets you post audio and just get back meta (no Deepgram call).
     * Use from the UI if needed to verify bytes flow through.
     */
    @PostMapping(
            value = "/stt/echo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> echo(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "audio", required = false) MultipartFile audio
    ) {
        MultipartFile in = (file != null && !file.isEmpty()) ? file : audio;
        return ResponseEntity.ok(Map.of(
                "received", in != null && !in.isEmpty(),
                "name", safeName(in),
                "size", in == null ? 0 : in.getSize(),
                "contentType", in == null ? null : in.getContentType()
        ));
    }

    /* ---------- helpers ---------- */

    private static String fileInfo(MultipartFile f) {
        if (f == null) return "(null)";
        return String.format("name='%s' size=%d contentType='%s'", safeName(f), f.getSize(), f.getContentType());
    }

    private static String safeName(MultipartFile f) {
        return (f == null || f.getOriginalFilename() == null) ? "(none)" : f.getOriginalFilename();
    }
}
