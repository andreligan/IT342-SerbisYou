package edu.cit.serbisyo.service;

import java.util.List;
import java.util.NoSuchElementException;
import javax.naming.NameNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.repository.AddressRepository;


@Service
public class AddressService {
    @Autowired
    AddressRepository arepo;

    public AddressService() {
        super();
    }

    // CREATE
    public AddressEntity postAddress(AddressEntity addresses) {
        return arepo.save(addresses);
        
    }

    // READ
    public List<AddressEntity> getAllAddresses() {
        return arepo.findAll();
    }

    // UPDATE
@SuppressWarnings("finally")
public AddressEntity putAddressDetails(int addressId, AddressEntity newAddressDetails) {
    AddressEntity address = new AddressEntity();

    try {
        address = arepo.findById(addressId).orElseThrow(() -> new NoSuchElementException("Address not found"));

        address.setProvince(newAddressDetails.getProvince());
        address.setCity(newAddressDetails.getCity());
        address.setBarangay(newAddressDetails.getBarangay());
        address.setStreetName(newAddressDetails.getStreetName());

    } catch(NoSuchElementException nex) {
        throw new NameNotFoundException("Address " + addressId + " not found.");
    } finally {
        return arepo.save(address);
    }
}

    // DELETE
    public String deleteAddressDetails(int addressId) {
        String msg = "";
        if(arepo.findById(addressId).isPresent()) {
            arepo.deleteById(addressId);
            msg = "Address Record sucessfully deleted.";
        } else {
            return "User Authentication ID " + addressId + " not found.";
        }
        return msg;
    }
}
