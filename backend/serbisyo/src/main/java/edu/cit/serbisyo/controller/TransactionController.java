package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
