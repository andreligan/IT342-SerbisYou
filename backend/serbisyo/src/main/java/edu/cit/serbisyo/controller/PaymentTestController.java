package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.service.PayMongoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentTestController {

    @Autowired
    private PayMongoService payMongoService;

    @GetMapping("/test-gcash-payment")
    public Mono<ResponseEntity<Map<String, Object>>> testGCashPayment() {
        double testAmount = 100.00; // 100 PHP
        String testDescription = "Test payment for SerbisYou";
        String successUrl = "http://localhost:5173/payment-success";
        String cancelUrl = "http://localhost:5173/payment-cancel";

        return payMongoService.createCheckoutSession(testAmount, testDescription, successUrl, cancelUrl)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().body(Map.of("error", error.getMessage()))));
    }
}