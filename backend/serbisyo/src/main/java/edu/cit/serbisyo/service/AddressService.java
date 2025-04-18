package edu.cit.serbisyo.service;

import edu.cit.serbisyo.entity.AddressEntity;
import edu.cit.serbisyo.repository.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<AddressEntity> getAllAddresses() {
        return addressRepository.findAll();
    }

    public AddressEntity createAddress(AddressEntity address) {
        return addressRepository.save(address);
    }

    public AddressEntity updateAddress(Long addressId, AddressEntity updatedAddress) {
        AddressEntity existingAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new RuntimeException("Address not found"));

        existingAddress.setProvince(updatedAddress.getProvince());
        existingAddress.setCity(updatedAddress.getCity());
        existingAddress.setBarangay(updatedAddress.getBarangay());
        existingAddress.setStreetName(updatedAddress.getStreetName());
        existingAddress.setZipCode(updatedAddress.getZipCode());
        existingAddress.setMain(updatedAddress.isMain());

        return addressRepository.save(existingAddress);
    }

    public String deleteAddress(Long addressId) {
        if (addressRepository.existsById(addressId)) {
            addressRepository.deleteById(addressId);
            return "Address successfully deleted.";
        }
        return "Address not found.";
    }
}
