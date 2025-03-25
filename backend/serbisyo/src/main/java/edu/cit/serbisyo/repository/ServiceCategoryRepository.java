package edu.cit.serbisyo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.cit.serbisyo.entity.ServiceCategoryEntity;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategoryEntity, Integer> {
}