package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayMongoService {

    private final WebClient webClient;
    private final String secretKey;

    public PayMongoService(@Value("${paymongo.secret-key}") String secretKey) {
        this.secretKey = secretKey;
        this.webClient = WebClient.builder()
                .baseUrl("https://api.paymongo.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<Map<String, Object>> createCheckoutSession(double amount, String description, 
                                                         String successUrl, String cancelUrl) {
        // Convert amount to smallest currency unit (centavos)
        int amountInCentavos = (int) (amount * 100);
        
        // Create checkout session payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> attributes = new HashMap<>();
        
        attributes.put("billing", null);
        attributes.put("send_email_receipt", false);
        attributes.put("show_description", true);
        attributes.put("show_line_items", true);
        attributes.put("cancel_url", cancelUrl);
        attributes.put("success_url", successUrl);
        attributes.put("description", description);
        attributes.put("payment_method_types", new String[]{"gcash"});
        
        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("currency", "PHP");
        lineItem.put("amount", amountInCentavos);
        lineItem.put("name", "Service Payment");
        lineItem.put("quantity", 1);
        
        attributes.put("line_items", new Map[]{lineItem});
        
        payload.put("data", Map.of("attributes", attributes));
        
        // Encode credentials for Basic Auth
        String credentials = Base64.getEncoder().encodeToString(
                (secretKey + ":").getBytes(StandardCharsets.UTF_8));
        
        // Make API request to PayMongo
        return webClient.post()
                .uri("/checkout_sessions")
                .header("Authorization", "Basic " + credentials)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {});
    }
    
    public Map<String, Object> validateWebhookEvent(String payload, String signature) {
        // In a production environment, implement proper signature verification
        // using the PayMongo webhook secret and the provided signature
        
        // For now, returning a simple verification result
        return Map.of("verified", true, "data", payload);
    }
}
