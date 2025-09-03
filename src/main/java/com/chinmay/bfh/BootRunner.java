package com.chinmay.bfh;

import com.chinmay.bfh.http.GenerateWebhookRequest;
import com.chinmay.bfh.http.GenerateWebhookResponse;
import com.chinmay.bfh.http.SubmitRequest;
import com.chinmay.bfh.util.SqlRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class BootRunner implements ApplicationRunner {

    private static final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private static final String FALLBACK_SUBMIT_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

    private final RestTemplate restTemplate;

    public BootRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Always produce the SQL file & jar artifacts even if remote is down
        String finalQuery = SqlRepository.finalQueryForQuestion1Postgres();
        try {
            Path out = Path.of("target", "final-query.sql");
            Files.createDirectories(out.getParent());
            Files.writeString(out, finalQuery);
        } catch (Exception io) {
            System.err.println("Could not write target/final-query.sql: " + io.getMessage());
        }

        try {
            // 1) Generate webhook + token (with robust retry for 5xx)
            GenerateWebhookRequest payload = new GenerateWebhookRequest(
                    "Chinmay M", "1RF22CS031", "rvit22bcs108.rvitm@rvei.edu.in");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<GenerateWebhookRequest> genReq = new HttpEntity<>(payload, headers);

            GenerateWebhookResponse gen = postJsonWithRetry(GENERATE_URL, genReq, GenerateWebhookResponse.class, 6);
            if (gen == null) throw new IllegalStateException("Empty response from generateWebhook");

            String webhook = (gen.getWebhook() == null || gen.getWebhook().isBlank())
                    ? FALLBACK_SUBMIT_URL : gen.getWebhook();
            String token = gen.getAccessToken();
            if (token == null || token.isBlank()) throw new IllegalStateException("Missing accessToken in response");

            // 2) Submit final query (also retry for 5xx)
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", token); // spec: token directly (no Bearer)
            HttpEntity<SubmitRequest> submitEntity = new HttpEntity<>(new SubmitRequest(finalQuery), submitHeaders);

            ResponseEntity<String> submitResp = postForStringWithRetry(webhook, submitEntity, 6);
            System.out.println("Submission Status: " + submitResp.getStatusCode());
            System.out.println("Response: " + submitResp.getBody());
        } catch (ResourceAccessException net) {
            System.err.println("\nNetwork error: Could not reach BFHL API (connect/read timeout).");
            System.err.println("Try a mobile hotspot OR configure proxy in IntelliJ (Settings → HTTP Proxy) ");
            System.exit(2);
        } catch (HttpServerErrorException srv) {
            System.err.println("\nServer error from BFHL API: " + srv.getStatusCode());
            System.err.println("The service returned 5xx (e.g., 503 Temporarily Unavailable).");
            System.err.println("Your app retried with backoff but the service kept failing.");
            System.exit(3);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            System.exit(0);
        }
    }

    private <T> T postJsonWithRetry(String url, HttpEntity<?> entity, Class<T> type, int maxAttempts) {
        int attempt = 0;
        while (true) {
            try {
                ResponseEntity<T> resp = restTemplate.postForEntity(url, entity, type);
                if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                    return resp.getBody();
                }
                if (!resp.getStatusCode().is5xxServerError()) {
                    throw new IllegalStateException("HTTP " + resp.getStatusCode() + " from " + url);
                }
                // fallthrough to retry on 5xx
                throw new HttpServerErrorException(resp.getStatusCode(), "5xx from server");
            } catch (HttpServerErrorException server5xx) {
                attempt++;
                if (attempt >= maxAttempts) throw server5xx;
                sleepBackoff(attempt);
            } catch (ResourceAccessException io) {
                // connection issues → also retry (up to maxAttempts)
                attempt++;
                if (attempt >= maxAttempts) throw io;
                sleepBackoff(attempt);
            }
        }
    }

    private ResponseEntity<String> postForStringWithRetry(String url, HttpEntity<?> entity, int maxAttempts) {
        int attempt = 0;
        while (true) {
            try {
                return restTemplate.postForEntity(url, entity, String.class);
            } catch (HttpServerErrorException server5xx) {
                attempt++;
                if (attempt >= maxAttempts) throw server5xx;
                sleepBackoff(attempt);
            } catch (ResourceAccessException io) {
                attempt++;
                if (attempt >= maxAttempts) throw io;
                sleepBackoff(attempt);
            }
        }
    }

    private void sleepBackoff(int attempt) {
        long base = 1000L * (1L << Math.min(5, attempt - 1)); // 1s,2s,4s,8s,16s,32s…
        long jitter = ThreadLocalRandom.current().nextLong(250, 750);
        try { Thread.sleep(base + jitter); } catch (InterruptedException ignored) {}
    }
}
