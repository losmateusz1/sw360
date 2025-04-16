package org.eclipse.sw360.rest.resourceserver.vendorOpenAPI;

import org.apache.jena.sparql.function.library.pi;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.resourcelists.PaginationParameterException;
import org.eclipse.sw360.datahandler.resourcelists.PaginationResult;
import org.eclipse.sw360.datahandler.resourcelists.ResourceClassNotFoundException;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.componentOpenApi.ComponentServicePG;
// import org.eclipse.sw360.rest.resourceserver.componentsApi.api.ComponentsOpenAPIApi;
// import org.eclipse.sw360.datahandler.componentsApi.model.Component;
import org.eclipse.sw360.datahandler.postgres.Component;
// import org.eclipse.sw360.datahandler.componentsApi.model.Release;
import org.eclipse.sw360.datahandler.postgres.Release;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.MultiStatus;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.releasesOpenAPI.ReleaseServicePG;
import org.eclipse.sw360.rest.resourceserver.user.UserController;
import org.eclipse.sw360.rest.resourceserver.vendor.VendorController;
import org.springdoc.core.converters.models.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.context.request.NativeWebRequest;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import jakarta.annotation.Generated;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import org.eclipse.sw360.datahandler.postgres.Project;
import org.eclipse.sw360.datahandler.postgres.Attachment;
import java.util.Set;
import java.util.UUID;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityState;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityDTO;
import org.eclipse.sw360.datahandler.thrift.components.ReleaseLink;
import org.eclipse.sw360.datahandler.thrift.ImportBomRequestPreparation;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.sw360.datahandler.thrift.vulnerabilities.VulnerabilityService;
import org.eclipse.sw360.datahandler.thrift.attachments.AttachmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.eclipse.sw360.datahandler.postgres.Vendor;

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
            description = "The vendor to be created.") @RequestBody org.eclipse.sw360.datahandler.componentsApi.model.Vendor vendor) {
        // Validate the input vendor
        User user = restControllerHelper.getSw360UserFromAuthentication();
        Vendor internalVendor = new Vendor(vendor);
        // internalVendor.setShortname(vendor.getShortname());
        // internalVendor.setFullname(vendor.getFullname());
        // internalVendor.setUrl(vendor.getUrl());
        Vendor result = vendorService.createVendor(internalVendor, user);
        return ResponseEntity.ok(new HalResource<>(result));
    }

    @PreAuthorize("hasAuthority('WRITE')")
    @RequestMapping(value = VENDORS_URL + "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<?> updateVendor(@Parameter(
            description = "The ID of the vendor to be updated.") @PathVariable("id") String id,
            @RequestBody org.eclipse.sw360.datahandler.componentsApi.model.Vendor vendor) {
        User user = restControllerHelper.getSw360UserFromAuthentication();
        Vendor internalVendor = new Vendor(vendor);
        internalVendor.setId(UUID.fromString(id));
        Vendor result = vendorService.updateVendor(internalVendor, user);
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
