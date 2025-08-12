package com.abhilasha.speechtosql.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class STTService {

    @Value("${deepgram.api.key:}")
    private String deepgramKey;


    private final OkHttpClient http = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String transcribe(MultipartFile audio) throws Exception {
        String url = "https://api.deepgram.com/v1/listen?model=nova-2-general&smart_format=true&punctuate=true";

        RequestBody body = RequestBody.create(
                audio.getBytes(),
                MediaType.parse("application/octet-stream")
        );

        Request req = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Token " + deepgramKey)
                .addHeader("Content-Type", "application/octet-stream")
                .post(body)
                .build();

        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful()) {
                String err = res.body() != null ? res.body().string() : "";
                throw new IllegalStateException("Deepgram STT failed: HTTP " + res.code() + " - " + err);
            }
            String json = res.body() != null ? res.body().string() : "{}";
            JsonNode node = mapper.readTree(json);
            JsonNode alt0 = node.path("results").path("channels").get(0)
                    .path("alternatives").get(0).path("transcript");
            return alt0.isMissingNode() ? "" : alt0.asText();
        }
    }
}
