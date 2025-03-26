package edu.cit.serbisyo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AddressEntity {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    
    private int addressId;
    
    private String province;
    
    private String city;
    
    private String barangay;
    
    private String streetName;
    
    
    public AddressEntity() {
        super();
    }
    
    public AddressEntity(int addressId, String province, String city, String barangay, String streetName) {
        this.addressId = addressId;
        this.province = province;
        this.city = city;
        this.barangay = barangay;
        this.streetName = streetName;
    }
    
    public int getAddressId() {
        return addressId;
    }
    
    public void setAddressId(int addressId) {
        this.addressId = addressId;
    }
    
    public String getProvince() {
        return province;
    }
    
    public void setProvince(String province) {
        this.province = province;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getBarangay() {
        return barangay;
    }
    
    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }
    
    public String getStreetName() {
        return streetName;
    }
    
    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }
    
}