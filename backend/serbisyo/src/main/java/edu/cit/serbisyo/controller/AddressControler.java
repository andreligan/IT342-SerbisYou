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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.entity.UserAuthEntity;
import edu.cit.serbisyo.service.AddressService;
import edu.cit.serbisyo.service.UserAuthService;

@RestController
@RequestMapping(method=RequestMethod.GET, path="/api/addresses")
public class AddressControler {

    @Autowired
    AddressService aserv;

    @GetMapping("/print")
    public String print() {
        return "Wow, it works!";
    }
    
    // CREATE
    @PostMapping("/postAddress")
    public AddressEntity postAddress(@RequestBody AddressEntity address) {
        return aserv.postAddress(address);
    }
    
      // READ
    @GetMapping("/getAllAddresses")
    public List<AddressEntity> getAllAddresses() {
        return aserv.getAllAddresses();
    }

    // UPDATE
    @PutMapping("/putAddressDetails")
    public AddressEntity putAddressDetails(@RequestParam int addressId, @RequestBody AddressEntity newAddressDetails) {
        return aserv. putAddressDetails(addressId, newAddressDetails);
    }

    // DELETE
    @DeleteMapping("/deleteAddressDetails/{addressId}")
    public String deleteAddressDetails(@PathVariable int addressId) {
        return aserv.deleteAddressDetails(addressId);
    }
    
}
