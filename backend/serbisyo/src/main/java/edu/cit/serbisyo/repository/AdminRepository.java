package edu.cit.serbisyo.repository;

import edu.cit.serbisyo.entity.AdminEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminRepository extends JpaRepository<AdminEntity, Long> {
}
