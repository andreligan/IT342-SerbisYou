package edu.cit.serbisyo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.serbisyo.entity.ServiceProviderEntity;

@Repository
public interface ServiceProviderRepository extends JpaRepository<ServiceProviderEntity, Long> {
} 