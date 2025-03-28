package edu.cit.serbisyo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import edu.cit.serbisyo.entity.TransactionEntity;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
}