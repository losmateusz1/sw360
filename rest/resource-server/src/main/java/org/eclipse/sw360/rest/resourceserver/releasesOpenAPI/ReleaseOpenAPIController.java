package org.eclipse.sw360.rest.resourceserver.releasesOpenAPI;

import org.apache.logging.log4j.core.config.builder.api.Component;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.resourcelists.PaginationParameterException;
import org.eclipse.sw360.datahandler.resourcelists.PaginationResult;
import org.eclipse.sw360.datahandler.resourcelists.ResourceClassNotFoundException;
import org.eclipse.sw360.datahandler.thrift.components.ReleaseLink;
import org.eclipse.sw360.datahandler.thrift.spdx.documentcreationinformation.DocumentCreationInformation;
import org.eclipse.sw360.datahandler.thrift.spdx.spdxdocument.SPDXDocument;
import org.eclipse.sw360.datahandler.thrift.spdx.spdxpackageinfo.PackageInformation;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.component.ComponentController;
import org.eclipse.sw360.rest.resourceserver.componentOpenApi.ComponentServicePG;
import org.eclipse.sw360.rest.resourceserver.componentOpenApi.ComponentsOpenAPIController;
// import org.eclipse.sw360.datahandler.componentsApi.model.Release;
import org.eclipse.sw360.datahandler.postgres.Release;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.release.ReleaseController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import java.net.URISyntaxException;
import java.util.List;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.BasePathAwareController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@BasePathAwareController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "tokenAuth")
@SecurityRequirement(name = "basic")
@Validated
public class ReleaseOpenAPIController
                implements RepresentationModelProcessor<RepositoryLinksResource> {

        @NonNull
        private final RestControllerHelper restControllerHelper;

        private ReleaseServicePG releaseService = new ReleaseServicePG();

        private ComponentServicePG componentService = new ComponentServicePG();

        private final String RELEASES_URL = "/releasesOpenAPI";

        @Override
        public RepositoryLinksResource process(RepositoryLinksResource resource) {
                resource.add(linkTo(ComponentsOpenAPIController.class).slash("api/releasesOpenAPI")
                                .withRel("releasesOpenAPI"));
                return resource;
        }

        @Operation(summary = "Get all releases.", description = "Get all releases.",
                        tags = {"Releases"})
        @GetMapping(value = RELEASES_URL + "/all")
        public ResponseEntity<PaginationResult<Release>> getAllReleases(
                        @Parameter(description = "The page number to retrieve.") @RequestParam(
                                        defaultValue = "0") int page,
                        @Parameter(description = "The number of items per page.") @RequestParam(
                                        defaultValue = "10") int size)
                        throws PaginationParameterException, ResourceClassNotFoundException,
                        TException {
                User sw360User = restControllerHelper.getSw360UserFromAuthentication();

                PaginationResult<Release> releases =
                                releaseService.getAllReleases(sw360User, page, size);

                return new ResponseEntity<>(releases, HttpStatus.OK);
        }

        @PreAuthorize("hasAuthority('WRITE')")
        @Operation(summary = "Create a release.", description = "Create a new release.",
                        tags = {"Releases"})
        @RequestMapping(value = RELEASES_URL, method = RequestMethod.POST)
        public ResponseEntity<EntityModel<Release>> createRelease(@Parameter(
                        description = "The release to be created.") @RequestBody Release release)
                        throws URISyntaxException, TException {
                User sw360User = restControllerHelper.getSw360UserFromAuthentication();

                if (release.getComponentId() == null) {
                        throw new IllegalArgumentException("Component ID is required");
                }

                System.out.println("Creating release: " + release);
                Release sw360Release = releaseService.createRelease(release, sw360User);

                URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                                .buildAndExpand(sw360Release.getId()).toUri();

                return ResponseEntity.created(location)
                                .body(EntityModel.of(sw360Release)
                                                .add(linkTo(ReleaseOpenAPIController.class)
                                                                .slash("api/releasesOpenAPI")
                                                                .withRel("releasesOpenAPI")));
        }

        private HalResource<Release> createHalReleaseResource(Release release, boolean verbose)
                        throws TException {
                HalResource<Release> halRelease = new HalResource<>(release);
                Link componentLink = linkTo(ReleaseController.class)
                                .slash("api" + ComponentController.COMPONENTS_URL + "/"
                                                + release.getComponent().getId())
                                .withRel("component");
                halRelease.add(componentLink);

                // if (verbose) {
                // if (release.getModerators() != null) {
                // Set<String> moderators = release.getModerators();
                // restControllerHelper.addEmbeddedModerators(halRelease, moderators);
                // release.setModerators(null);
                // }
                // if (release.getAttachments() != null) {
                // Set<Attachment> attachments = release.getAttachments();
                // restControllerHelper.addEmbeddedAttachments(halRelease,
                // attachments);
                // release.setAttachments(null);
                // }
                // if (release.getVendor() != null) {
                // Vendor vendor = release.getVendor();
                // HalResource<Vendor> vendorHalResource =
                // restControllerHelper.addEmbeddedVendor(vendor);
                // halRelease.addEmbeddedResource("sw360:vendors", vendorHalResource);
                // release.setVendor(null);
                // }
                // if (release.getMainLicenseIds() != null) {
                // restControllerHelper.addEmbeddedLicenses(halRelease,
                // release.getMainLicenseIds());
                // }
                // if (release.getOtherLicenseIds() != null) {
                // restControllerHelper.addEmbeddedOtherLicenses(halRelease,
                // release.getOtherLicenseIds());
                // }
                // Set<String> packageIds = release.getPackageIds();

                // if (packageIds != null) {
                // restControllerHelper.addEmbeddedPackages(halRelease, packageIds,
                // packageService);
                // release.setPackageIds(null);
                // }
                // }
                return halRelease;
        }

        @Operation(summary = "Get a release by ID.", description = "Get a single release by ID.",
                        tags = {"Releases"})
        @GetMapping(value = RELEASES_URL + "/{id}")
        public ResponseEntity<EntityModel<Release>> getRelease(@Parameter(
                        description = "The ID of the release to be retrieved.") @PathVariable("id") String id)
                        throws TException {
                User sw360User = restControllerHelper.getSw360UserFromAuthentication();
                Release sw360Release = releaseService.getReleaseForUserById(id, sw360User);
                HalResource<Release> halRelease = createHalReleaseResource(sw360Release, true);
                // restControllerHelper.addEmbeddedDataToHalResourceRelease(halRelease,
                // sw360Release);
                // List<ReleaseLink> linkedReleaseRelations =
                // releaseService.getLinkedReleaseRelations(sw360Release, sw360User);

                // String spdxId = sw360Release.getSpdxId();
                // if (CommonUtils.isNotNullEmptyOrWhitespace(spdxId)
                // && SW360Utils.readConfig(SPDX_DOCUMENT_ENABLED, false)) {
                // SPDXDocument spdxDocument =
                // releaseService.getSPDXDocumentById(spdxId, sw360User);
                // sw360SPDXDocumentService.sortSectionForSPDXDocument(spdxDocument);
                // restControllerHelper.addEmbeddedSpdxDocument(halRelease, spdxDocument);
                // String spdxDocumentCreationInfoId =
                // spdxDocument.getSpdxDocumentCreationInfoId();
                // if (CommonUtils.isNotNullEmptyOrWhitespace(spdxDocumentCreationInfoId)) {
                // DocumentCreationInformation documentCreationInformation =
                // releaseService.getDocumentCreationInformationById(
                // spdxDocumentCreationInfoId,
                // sw360User);
                // sw360SPDXDocumentService.sortSectionForDocumentCreation(
                // documentCreationInformation);
                // restControllerHelper.addEmbeddedDocumentCreationInformation(
                // halRelease, documentCreationInformation);
                // }
                // String spdxPackageInfoId = spdxDocument.getSpdxPackageInfoIds().stream()
                // .findFirst().get();
                // if (CommonUtils.isNotNullEmptyOrWhitespace(spdxPackageInfoId)) {
                // PackageInformation packageInformation =
                // releaseService.getPackageInformationById(
                // spdxPackageInfoId, sw360User);
                // sw360SPDXDocumentService.sortSectionForPackageInformation(
                // packageInformation);
                // restControllerHelper.addEmbeddedPackageInformation(halRelease,
                // packageInformation);
                // }
                // }
                // if (linkedReleaseRelations != null) {
                // restControllerHelper.addEmbeddedReleaseLinks(halRelease,
                // linkedReleaseRelations);
                // }
                return new ResponseEntity<>(halRelease, HttpStatus.OK);
        }

        @Operation(summary = "List all of the service's releases.",
                        description = "List all of the service's releases.", tags = {"Releases"})
        @GetMapping(value = RELEASES_URL)
        public ResponseEntity<CollectionModel<HalResource<Release>>> getReleasesForUser(
                        @Parameter(description = "The page number to retrieve.") @RequestParam(
                                        defaultValue = "0") int page,
                        @Parameter(description = "The number of items per page.") @RequestParam(
                                        defaultValue = "10") int size)
                        throws PaginationParameterException, ResourceClassNotFoundException,
                        TException {
                User sw360User = restControllerHelper.getSw360UserFromAuthentication();
                PaginationResult<Release> releases =
                                releaseService.getReleasesForUser(sw360User, page, size);

                List<HalResource<Release>> halReleases =
                                releases.getResources().stream().map(release -> {
                                        try {
                                                return createHalReleaseResource(release, false);
                                        } catch (TException e) {
                                                throw new RuntimeException(e);
                                        }
                                }).toList();

                CollectionModel<HalResource<Release>> collectionModel =
                                CollectionModel.of(halReleases);
                collectionModel.add(linkTo(ReleaseOpenAPIController.class)
                                .slash("api/releasesOpenAPI").withRel("releasesOpenAPI"));

                return new ResponseEntity<>(collectionModel, HttpStatus.OK);
        }
}
