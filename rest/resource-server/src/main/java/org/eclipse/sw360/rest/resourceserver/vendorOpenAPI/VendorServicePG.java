package org.eclipse.sw360.rest.resourceserver.vendorOpenAPI;

import org.eclipse.sw360.datahandler.thrift.users.User;
import java.util.UUID;
import org.eclipse.sw360.datahandler.postgres.Vendor;
import org.eclipse.sw360.datahandler.postgresql.VendorRepositoryPG;

public class VendorServicePG {
    private final VendorRepositoryPG vendorRepository = new VendorRepositoryPG();

    public Vendor createVendor(Vendor vendor, User user) {
        if (vendor.getShortname() == null && vendor.getFullname() == null) {
            throw new IllegalArgumentException("Vendor name is required");
        }

        Vendor savedVendor = new VendorRepositoryPG().saveVendor(vendor);
        return savedVendor;
    }

    public Vendor getVendorByFullName(String fullname) {
        Vendor vendor = vendorRepository.getVendorByFullName(fullname);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor not found");
        }

        return vendor;
    }

    public Vendor updateVendor(Vendor internalVendor, User user) {
        if (internalVendor.getId() == null) {
            throw new IllegalArgumentException("Vendor ID is required");
        }

        Vendor existingVendor = vendorRepository.getVendorById(internalVendor.getId());
        if (existingVendor == null) {
            throw new IllegalArgumentException("Vendor not found");
        }

        existingVendor.setShortname(internalVendor.getShortname());
        existingVendor.setFullname(internalVendor.getFullname());
        existingVendor.setUrl(internalVendor.getUrl());

        return vendorRepository.updateVendor(existingVendor);
    }

    public void deleteVendor(UUID fromString, User user) {
        Vendor vendor = vendorRepository.getVendorById(fromString);
        if (vendor == null) {
            throw new IllegalArgumentException("Vendor not found");
        }

        vendorRepository.deleteVendor(vendor);
    }
}
