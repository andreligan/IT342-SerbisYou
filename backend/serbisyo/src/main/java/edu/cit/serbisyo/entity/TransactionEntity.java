package edu.cit.serbisyo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int transactionId;

    private int serviceId;
    private int userId;
    private LocalDate bookingDate;
    private String status;
    private double totalCost;

    public TransactionEntity() {
        super();
    }

    public TransactionEntity(int transactionId, int serviceId, int userId, LocalDate bookingDate, String status, double totalCost) {
        this.transactionId = transactionId;
        this.serviceId = serviceId;
        this.userId = userId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalCost = totalCost;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
}