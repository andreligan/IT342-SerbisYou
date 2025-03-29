package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.repository.TransactionRepository;

import java.util.List;
import java.util.NoSuchElementException;
@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public TransactionService() {
        super();
    }

    // CREATE a new transaction
    public TransactionEntity createTransaction(TransactionEntity transaction) {
        return transactionRepository.save(transaction);
    }

    // READ all transactions
    public List<TransactionEntity> getAllTransactions() {
        return transactionRepository.findAll();
    }

    // READ a transaction by ID
    public TransactionEntity getTransactionById(Long transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Transaction with ID " + transactionId + " not found"));
    }

    // UPDATE an existing transaction
    public TransactionEntity updateTransaction(Long transactionId, TransactionEntity newTransactionDetails) {
        TransactionEntity existingTransaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NoSuchElementException("Transaction with ID " + transactionId + " not found"));

        existingTransaction.setBooking(newTransactionDetails.getBooking());
        existingTransaction.setPaymentMethod(newTransactionDetails.getPaymentMethod());
        existingTransaction.setAmount(newTransactionDetails.getAmount());
        existingTransaction.setStatus(newTransactionDetails.getStatus());

        return transactionRepository.save(existingTransaction);
    }

    // DELETE a transaction
    public String deleteTransaction(Long transactionId) {
        if (transactionRepository.existsById(transactionId)) {
            transactionRepository.deleteById(transactionId);
            return "Transaction with ID " + transactionId + " has been deleted successfully.";
        } else {
            return "Transaction with ID " + transactionId + " not found.";
        }
    }
}