package org.eclipse.sw360.rest.resourceserver.vendorOpenAPI;

import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.UUID;

import org.eclipse.sw360.datahandler.postgres.VendorPG;

@BasePathAwareController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "tokenAuth")
@SecurityRequirement(name = "basic")
@Validated
public class VendorOpenAPIController
        implements RepresentationModelProcessor<RepositoryLinksResource> {

    @NonNull
    private final RestControllerHelper restControllerHelper;

    private final String VENDORS_URL = "/vendorsOpenAPI";

    private final VendorServicePG vendorService = new VendorServicePG();

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(VendorOpenAPIController.class).slash("api/vendorsOpenAPI")
                .withRel("vendorsOpenAPI"));
        return resource;
    }

    @Operation(summary = "Create a new vendor.", description = "Create a new vendor.",
            tags = {"Vendor"})
    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = VENDORS_URL, method = RequestMethod.POST)
    public ResponseEntity<?> createVendor(@Parameter(
            description = "The vendor to be created.") @RequestBody org.eclipse.sw360.datahandler.componentsApi.model.VendorAPI vendor) {
        User user = restControllerHelper.getSw360UserFromAuthentication();
        VendorPG internalVendor = new VendorPG(vendor);

        VendorPG result = vendorService.createVendor(internalVendor, user);
        return ResponseEntity.ok(new HalResource<>(result));
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = VENDORS_URL + "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<?> updateVendor(@Parameter(
            description = "The ID of the vendor to be updated.") @PathVariable("id") String id,
            @RequestBody org.eclipse.sw360.datahandler.componentsApi.model.VendorAPI vendor) {
        User user = restControllerHelper.getSw360UserFromAuthentication();
        VendorPG internalVendor = new VendorPG(vendor);
        internalVendor.setId(UUID.fromString(id));
        VendorPG result = vendorService.updateVendor(internalVendor, user);
        return ResponseEntity.ok(new HalResource<>(result));
    }

    @Operation(summary = "Delete a vendor.", description = "Delete vendor by id.",
            tags = {"Vendor"})
    @RequestMapping(value = VENDORS_URL + "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteVendor(@Parameter(
            description = "The id of the vendor to be deleted.") @PathVariable("id") String id) {
        User user = restControllerHelper.getSw360UserFromAuthentication();
        vendorService.deleteVendor(UUID.fromString(id), user);
        return ResponseEntity.noContent().build();
    }
}
