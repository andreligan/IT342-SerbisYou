package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.repository.TransactionRepository;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    // CREATE
    public TransactionEntity addTransaction(TransactionEntity transaction) {
        return transactionRepository.save(transaction);
    }

    // READ
    public List<TransactionEntity> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<TransactionEntity> getTransactionById(int id) {
        return transactionRepository.findById(id);
    }

    // UPDATE
    public TransactionEntity updateTransaction(int id, TransactionEntity newDetails) {
        return transactionRepository.findById(id)
            .map(transaction -> {
                transaction.setServiceId(newDetails.getServiceId());
                transaction.setUserId(newDetails.getUserId());
                transaction.setBookingDate(newDetails.getBookingDate());
                transaction.setStatus(newDetails.getStatus());
                transaction.setTotalCost(newDetails.getTotalCost());
                return transactionRepository.save(transaction);
            }).orElseThrow(() -> new RuntimeException("Transaction not found!"));
    }

    // DELETE
    public String deleteTransaction(int id) {
        if (transactionRepository.existsById(id)) {
            transactionRepository.deleteById(id);
            return "Transaction successfully deleted!";
        }
        return "Transaction with ID " + id + " not found.";
    }
}
