package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long> {
    ServiceEntity findByServiceName(String serviceName);
    
    // New price-related query methods
    List<ServiceEntity> findByPriceGreaterThanEqual(int minimumPrice);
    List<ServiceEntity> findByPriceLessThanEqual(int maximumPrice);
    List<ServiceEntity> findByPriceBetween(int minimumPrice, int maximumPrice);
}