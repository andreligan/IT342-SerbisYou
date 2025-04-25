package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/print")
    public String print() {
        return "Transaction Controller is working!";
    }

    // CREATE
    @PostMapping("/postTransaction")
    public TransactionEntity createTransaction(@RequestBody TransactionEntity transaction) {
        return transactionService.createTransaction(transaction);
    }

    // READ ALL
    @GetMapping("/getAll")
    public List<TransactionEntity> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // READ BY ID
    @GetMapping("/getById/{transactionId}")
    public TransactionEntity getTransactionById(@PathVariable Long transactionId) {
        return transactionService.getTransactionById(transactionId);
    }

    // UPDATE
    @PutMapping("/updateTransaction/{transactionId}")
    public TransactionEntity updateTransaction(
            @PathVariable Long transactionId,
            @RequestBody TransactionEntity newTransactionDetails) {
        return transactionService.updateTransaction(transactionId, newTransactionDetails);
    }

    // DELETE
    @DeleteMapping("/delete/{transactionId}")
    public String deleteTransaction(@PathVariable Long transactionId) {
        return transactionService.deleteTransaction(transactionId);
    }

    // CONFIRM CASH PAYMENT
    @PostMapping("/confirm-cash-payment/{bookingId}")
    public ResponseEntity<?> confirmCashPayment(@PathVariable Long bookingId) {
        try {
            TransactionEntity transaction = transactionService.confirmCashPayment(bookingId);
            return ResponseEntity.ok(transaction);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error confirming cash payment: " + e.getMessage());
        }
    }
}
