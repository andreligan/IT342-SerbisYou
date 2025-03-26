package edu.cit.serbisyo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.service.TransactionService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transaction")
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // CREATE
    @PostMapping("/add")
    public TransactionEntity addTransaction(@RequestBody TransactionEntity transaction) {
        return transactionService.addTransaction(transaction);
    }

    // READ
    @GetMapping("/getAll")
    public List<TransactionEntity> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/get/{id}")
    public Optional<TransactionEntity> getTransactionById(@PathVariable int id) {
        return transactionService.getTransactionById(id);
    }

    // UPDATE
    @PutMapping("/update/{id}")
    public TransactionEntity updateTransaction(@PathVariable int id, @RequestBody TransactionEntity newDetails) {
        return transactionService.updateTransaction(id, newDetails);
    }

    // DELETE
    @DeleteMapping("/delete/{id}")
    public String deleteTransaction(@PathVariable int id) {
        return transactionService.deleteTransaction(id);
    }
}