package edu.cit.serbisyo.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class BookingEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int bookingId;
    
    private int customerId;
    private int serviceId;
    private LocalDate bookingDate;
    private String status;
    private BigDecimal totalCost;

    public BookingEntity() {}

    public BookingEntity(int customerId, int serviceId, LocalDate bookingDate, String status, BigDecimal totalCost) {
        this.customerId = customerId;
        this.serviceId = serviceId;
        this.bookingDate = bookingDate;
        this.status = status;
        this.totalCost = totalCost;
    }

    public int getBookingId() { return bookingId; }
    public int getCustomerId() { return customerId; }
    public int getServiceId() { return serviceId; }
    public LocalDate getBookingDate() { return bookingDate; }
    public String getStatus() { return status; }
    public BigDecimal getTotalCost() { return totalCost; }

    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    public void setBookingDate(LocalDate bookingDate) { this.bookingDate = bookingDate; }
    public void setStatus(String status) { this.status = status; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
}
