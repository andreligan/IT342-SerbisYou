package edu.cit.serbisyo.controller;

import edu.cit.serbisyo.entity.AdminEntity;
import edu.cit.serbisyo.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {
    
    @Autowired
    private final AdminService adminService;
    
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }
    
    @GetMapping("/get")
    public ResponseEntity<List<AdminEntity>> getAllAdmins() {
        List<AdminEntity> admins = adminService.getAllAdmins();
        return new ResponseEntity<>(admins, HttpStatus.OK);
    }
    
    @GetMapping("/getById/{id}")
    public ResponseEntity<AdminEntity> getAdminById(@PathVariable("id") Long adminId) {
        Optional<AdminEntity> admin = adminService.getAdminById(adminId);
        return admin.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @GetMapping("/getByUserId/{userId}")
    public ResponseEntity<AdminEntity> getAdminByUserId(@PathVariable("userId") Long userId) {
        Optional<AdminEntity> admin = adminService.getAdminByUserId(userId);
        return admin.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
    
    @PostMapping("/create")
    public ResponseEntity<AdminEntity> createAdmin(@RequestBody AdminEntity admin) {
        AdminEntity createdAdmin = adminService.createAdmin(admin);
        return new ResponseEntity<>(createdAdmin, HttpStatus.CREATED);
    }
    
    @PutMapping("updateById/{id}")
    public ResponseEntity<AdminEntity> updateAdmin(@PathVariable("id") Long adminId, @RequestBody AdminEntity admin) {
        Optional<AdminEntity> existingAdmin = adminService.getAdminById(adminId);
        if (existingAdmin.isPresent()) {
            admin.setAdminId(adminId);
            return new ResponseEntity<>(adminService.updateAdmin(admin), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("deleteById/{id}")
    public ResponseEntity<HttpStatus> deleteAdmin(@PathVariable("id") Long adminId) {
        try {
            adminService.deleteAdmin(adminId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
