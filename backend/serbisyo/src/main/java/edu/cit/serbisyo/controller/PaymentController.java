package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import edu.cit.serbisyo.service.PayMongoService;
import edu.cit.serbisyo.service.TransactionService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PaymentController {

    private final PayMongoService payMongoService;
    private final TransactionService transactionService;

    @Autowired
    public PaymentController(PayMongoService payMongoService, TransactionService transactionService) {
        this.payMongoService = payMongoService;
        this.transactionService = transactionService;
    }

    @PostMapping("/create-gcash-checkout")
    public Mono<ResponseEntity<Map<String, Object>>> createGCashCheckout(@RequestBody Map<String, Object> request) {
        double amount = Double.parseDouble(request.get("amount").toString());
        String description = request.get("description").toString();
        String successUrl = request.get("successUrl").toString();
        String cancelUrl = request.get("cancelUrl").toString();
        
        return payMongoService.createCheckoutSession(amount, description, successUrl, cancelUrl)
                .map(response -> {
                    // Extract the checkout URL from the response
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                    String checkoutUrl = (String) attributes.get("checkout_url");
                    
                    // You can save transaction information to your database here
                    // transactionService.createTransaction(...);
                    
                    return ResponseEntity.ok(Map.of(
                        "checkout_url", checkoutUrl,
                        "session_id", data.get("id")
                    ));
                })
                .onErrorResume(error -> {
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of("error", error.getMessage())));
                });
    }
    
    /**
     * Test endpoint to quickly verify PayMongo integration
     */
    @GetMapping("/test-gcash-payment")
    public Mono<ResponseEntity<Map<String, Object>>> testGCashPayment() {
        double testAmount = 100.00; // 100 PHP
        String testDescription = "Test payment for SerbisYou";
        String successUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";
        
        return payMongoService.createCheckoutSession(testAmount, testDescription, successUrl, cancelUrl)
                .map(response -> {
                    // Extract the checkout URL from the response
                    Map<String, Object> data = (Map<String, Object>) response.get("data");
                    Map<String, Object> attributes = (Map<String, Object>) data.get("attributes");
                    String checkoutUrl = (String) attributes.get("checkout_url");
                    
                    return ResponseEntity.ok(Map.of(
                        "checkout_url", checkoutUrl,
                        "session_id", data.get("id"),
                        "test_mode", true
                    ));
                })
                .onErrorResume(error -> {
                    return Mono.just(ResponseEntity
                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Map.of(
                                "error", error.getMessage(),
                                "error_details", error.toString()
                            )));
                });
    }
    
    @PostMapping("/paymongo-webhook")
    public ResponseEntity<Map<String, Object>> handlePayMongoWebhook(
            @RequestBody String payload,
            @RequestHeader("Paymongo-Signature") String signature) {
        
        try {
            // Validate webhook signature
            Map<String, Object> validationResult = payMongoService.validateWebhookEvent(payload, signature);
            
            // If webhook is verified, process the payment
            if ((Boolean) validationResult.get("verified")) {
                // Process successful payment
                // Update transaction status in your database
                // transactionService.updateTransactionStatus(...);
                
                return ResponseEntity.ok(Map.of("status", "success"));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid signature"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
