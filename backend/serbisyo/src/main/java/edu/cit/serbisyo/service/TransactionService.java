package edu.cit.serbisyo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.TransactionEntity;
import edu.cit.serbisyo.entity.BookingEntity;
import edu.cit.serbisyo.repository.TransactionRepository;
import edu.cit.serbisyo.repository.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BookingRepository bookingRepository;

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

    // Confirm a cash payment was received
    @Transactional
    public TransactionEntity confirmCashPayment(Long bookingId) {
        // Find pending cash transaction for this booking
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));
        
        // Find the pending transaction
        List<TransactionEntity> transactions = booking.getTransactions();
        TransactionEntity cashTransaction = transactions.stream()
                .filter(t -> "Cash".equals(t.getPaymentMethod()) && "PENDING".equals(t.getStatus()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("No pending cash transaction found"));
        
        // Update transaction status
        cashTransaction.setStatus("COMPLETED");
        cashTransaction.setTransactionDate(LocalDateTime.now());
        
        // Also update booking if needed
        booking.setStatus("COMPLETED");
        bookingRepository.save(booking);
        
        return transactionRepository.save(cashTransaction);
    }
}