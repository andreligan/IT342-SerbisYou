package edu.cit.serbisyo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.service.AddressService;

@RestController
@RequestMapping(method = RequestMethod.GET, path = "/api/addresses")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/print")
    public String print() {
        return "Address Controller is working!";
    }

    @PostMapping("/postAddress")
    public AddressEntity createAddress(@RequestBody AddressEntity address) {
        return addressService.createAddress(address);
    }

    @GetMapping("/getAll")
    public List<AddressEntity> getAllAddresses() {
        return addressService.getAllAddresses();
    }

    @PutMapping("/updateAddress/{addressId}")
    public AddressEntity updateAddress(@PathVariable Long addressId, @RequestBody AddressEntity updatedAddress) {
        return addressService.updateAddress(addressId, updatedAddress);
    }

    @DeleteMapping("/delete/{addressId}")
    public String deleteAddress(@PathVariable Long addressId) {
        return addressService.deleteAddress(addressId);
    }
}

