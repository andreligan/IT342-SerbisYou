package edu.cit.serbisyo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.serbisyo.entity.AddressEntity;

@Repository
public interface AddressRepository extends JpaRepository <AddressEntity, Integer> {
    
    public AddressEntity findByProvince(String province);
    
}
