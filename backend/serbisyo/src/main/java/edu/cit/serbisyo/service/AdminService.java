package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.AdminEntity;
import edu.cit.serbisyo.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    
    @Autowired
    private final AdminRepository adminRepository;
    
    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }
    
    public List<AdminEntity> getAllAdmins() {
        return adminRepository.findAll();
    }
    
    public Optional<AdminEntity> getAdminById(Long adminId) {
        return adminRepository.findById(adminId);
    }
    
    public AdminEntity createAdmin(AdminEntity admin) {
        return adminRepository.save(admin);
    }
    
    public AdminEntity updateAdmin(AdminEntity admin) {
        return adminRepository.save(admin);
    }
    
    public void deleteAdmin(Long adminId) {
        adminRepository.deleteById(adminId);
    }
}
