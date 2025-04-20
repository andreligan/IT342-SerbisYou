package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByCustomerCustomerId(Long customerId);
    List<BookingEntity> findByServiceServiceId(Long serviceId);
}