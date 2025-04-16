package org.eclipse.sw360.rest.resourceserver.componentOpenApi;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.sparql.function.library.pi;
import org.apache.thrift.TException;
import org.eclipse.sw360.datahandler.common.SW360Constants;
import org.eclipse.sw360.datahandler.resourcelists.PaginationParameterException;
import org.eclipse.sw360.datahandler.resourcelists.PaginationResult;
import org.eclipse.sw360.datahandler.resourcelists.ResourceClassNotFoundException;
import org.eclipse.sw360.datahandler.thrift.users.User;
// import org.eclipse.sw360.rest.resourceserver.componentsApi.api.ComponentsOpenAPIApi;
// import org.eclipse.sw360.datahandler.componentsApi.model.Component;
import org.eclipse.sw360.datahandler.postgres.Component;
// import org.eclipse.sw360.datahandler.componentsApi.model.Release;
import org.eclipse.sw360.datahandler.postgres.Release;
import org.eclipse.sw360.rest.resourceserver.core.HalResource;
import org.eclipse.sw360.rest.resourceserver.core.MultiStatus;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelperPG;
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

import org.eclipse.sw360.datahandler.componentsApi.model.Vendor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@BasePathAwareController
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@SecurityRequirement(name = "tokenAuth")
@SecurityRequirement(name = "basic")
@Validated
public class ComponentsOpenAPIController
                implements RepresentationModelProcessor<RepositoryLinksResource> {

        public static final String COMPONENTS_URL = "/componentsOpenAPI";

        @NonNull
        private final RestControllerHelper restControllerHelper;

        @NonNull
        private final RestControllerHelperPG restControllerHelperPG;

        private ComponentServicePG componentService = new ComponentServicePG();

        private ReleaseServicePG releaseService = new ReleaseServicePG();

        private final Logger logger = LogManager.getLogger(ComponentsOpenAPIController.class);

        public static String urlDecode(String str) {
                if (str == null) {
                        return null;
                }
                try {
                        return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
                } catch (UnsupportedEncodingException e) {
                        // This exception occurs if the specified encoding is not supported
                        throw new IllegalArgumentException(
                                        "Unsupported encoding: " + e.getMessage());
                }
        }

        public Map<String, String> parseQueryString(String queryString) {
                Map<String, String> parameters = new HashMap<>();
                if (queryString != null && !queryString.isEmpty()) {
                        UriComponentsBuilder builder =
                                        UriComponentsBuilder.newInstance().query(queryString);
                        builder.build().getQueryParams().forEach((key, values) -> parameters
                                        .put(key, urlDecode(values.get(0))));
                }
                return parameters;
        }

        @Override
        public RepositoryLinksResource process(RepositoryLinksResource resource) {
                resource.add(linkTo(ComponentsOpenAPIController.class)
                                .slash("api/componentsOpenAPI").withRel("componentsOpenAPI"));
                return resource;
        }

        private HalResource<Component> createHalComponent(
                        org.eclipse.sw360.datahandler.componentsApi.model.Component sw360Component,
                        User user) throws TException {

                Component component = new Component();
                component.setName(sw360Component.getName());
                component.setId(sw360Component.getId());

                HalResource<Component> halComponent = new HalResource<>(component);
                User componentCreator =
                                restControllerHelper.getUserByEmail(sw360Component.getCreatedBy());

                List<org.eclipse.sw360.datahandler.componentsApi.model.Release> releases =
                                sw360Component.getReleases();

                restControllerHelperPG.addEmbeddedReleasesPG(halComponent, releases);

                if (sw360Component.getVendorNames() != null) {
                        List<String> vendors = sw360Component.getVendorNames();
                        Set<String> vendorNames =
                                        vendors.stream().map(vendor -> vendor.toLowerCase())
                                                        .collect(Collectors.toSet());
                        restControllerHelperPG.addEmbeddedVendorsPG(halComponent, vendorNames);
                        // sw360Component.setVendorNames(null);
                }

                return halComponent;
        }

        private void addEmbeddedDefaultVendor(HalResource<Component> halComponent,
                        Vendor defaultVendor) {
                HalResource<Vendor> halDefaultVendor = new HalResource<>(defaultVendor);
                Link vendorSelfLink =
                                linkTo(UserController.class)
                                                .slash("api" + VendorController.VENDORS_URL + "/"
                                                                + defaultVendor.getId())
                                                .withSelfRel();
                halDefaultVendor.add(vendorSelfLink);
                halComponent.addEmbeddedResource("defaultVendor", halDefaultVendor);
        }

        private CollectionModel getFilteredComponentResources(List<String> fields,
                        boolean allDetails, User sw360User,
                        PaginationResult<org.eclipse.sw360.datahandler.componentsApi.model.Component> paginationResult)

                        throws URISyntaxException {
                List<EntityModel<Component>> componentResources = new ArrayList<>();

                paginationResult.getResources().stream().forEach(c -> {
                        try {
                                EntityModel<Component> embeddedComponentResource =
                                                createHalComponent(c, sw360User);
                                componentResources.add(embeddedComponentResource);
                        } catch (Exception e) {
                                throw new RuntimeException(e);
                        }
                });

                CollectionModel resources;
                if (componentResources.isEmpty()) {
                        resources = restControllerHelper.emptyPageResource(Component.class,
                                        paginationResult);
                } else {
                        resources = restControllerHelper.generatePagesResource(paginationResult,
                                        componentResources);
                }
                return resources;
        }


        @RequestMapping(value = COMPONENTS_URL, method = RequestMethod.GET)
        public ResponseEntity<CollectionModel<EntityModel<Component>>> componentsOpenAPIGet(
                        HttpServletRequest request,
                        org.springframework.data.domain.Pageable pageable,
                        @Parameter(name = "name", description = "Name of the component to filter",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(value = "name",
                                                        required = false) String name,
                        @Parameter(name = "categories",
                                        description = "Categories of the component to filter, as a comma-separated list.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "categories",
                                                        required = false) String categories,
                        @Parameter(name = "type", description = "Type of the component to filter",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(value = "type",
                                                        required = false) String type,
                        @Parameter(name = "languages",
                                        description = "Component languages to filter, as a comma-separated list.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "languages",
                                                        required = false) String languages,
                        @Parameter(name = "softwarePlatforms",
                                        description = "Software Platforms to filter, as a comma-separated list.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "softwarePlatforms",
                                                        required = false) String softwarePlatforms,
                        @Parameter(name = "operatingSystems",
                                        description = "Operating Systems to filter, as a comma-separated list.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "operatingSystems",
                                                        required = false) String operatingSystems,
                        @Parameter(name = "vendors",
                                        description = "Vendors to filter, as a comma-separated list.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "vendors",
                                                        required = false) String vendors,
                        @Parameter(name = "mainLicenses",
                                        description = "Main Licenses to filter, as a comma-separated list.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "mainLicenses",
                                                        required = false) String mainLicenses,
                        @Parameter(name = "createdBy",
                                        description = "Created by user to filter (email).",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "createdBy",
                                                        required = false) String createdBy,
                        @Parameter(name = "createdOn",
                                        description = "Date component was created on (YYYY-MM-DD).",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "createdOn",
                                                        required = false) @DateTimeFormat(
                                                                        iso = DateTimeFormat.ISO.DATE) LocalDate createdOn,
                        @Parameter(name = "fields",
                                        description = "Properties which should be present for each component in the result",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "fields",
                                                        required = false) List<String> fields,
                        @Parameter(name = "allDetails",
                                        description = "Flag to get components with all details.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "allDetails",
                                                        required = false) Boolean allDetails,
                        @Parameter(name = "luceneSearch",
                                        description = "Use lucenesearch to filter the components.",
                                        in = ParameterIn.QUERY) @Valid @RequestParam(
                                                        value = "luceneSearch",
                                                        required = false) Boolean luceneSearch)
                        throws TException, URISyntaxException, PaginationParameterException,
                        ResourceClassNotFoundException {
                String queryString = request.getQueryString();
                Map<String, String> params = restControllerHelper.parseQueryString(queryString);
                User sw360User = restControllerHelper.getSw360UserFromAuthentication();
                List<org.eclipse.sw360.datahandler.componentsApi.model.Component> allComponents =
                                componentService.listOAPIComponents(params);
                PaginationResult<org.eclipse.sw360.datahandler.componentsApi.model.Component> paginationResult =
                                restControllerHelper.createPaginationResult(request, pageable,
                                                allComponents, SW360Constants.TYPE_COMPONENT);

                allDetails = allDetails == null ? false : allDetails;
                CollectionModel resources = getFilteredComponentResources(fields, allDetails,
                                sw360User, paginationResult);

                return new ResponseEntity<>(resources, HttpStatus.OK);
        }

        @Operation(summary = "Get a single component.",
                        description = "Get a single component by its id.", tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL + "/{id}", method = RequestMethod.GET)
        public ResponseEntity<EntityModel<Component>> getComponent(@Parameter(
                        description = "The id of the component to be retrieved.") @PathVariable("id") String id)
                        throws TException {
                User user = restControllerHelper.getSw360UserFromAuthentication();
                org.eclipse.sw360.datahandler.postgres.Component sw360Component =
                                componentService.getComponentById(id);

                HalResource<Component> userHalResource = createHalComponent(sw360Component, user);
                restControllerHelperPG.addEmbeddedDataToComponentOAPI(userHalResource,
                                sw360Component);
                return new ResponseEntity<>(userHalResource, HttpStatus.OK);
        }

        @Operation(summary = "Get recently created components.",
                        description = "Return 5 of the service's most recently created components.",
                        tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL + "/recentComponents", method = RequestMethod.GET)
        public ResponseEntity<CollectionModel<EntityModel<org.eclipse.sw360.datahandler.postgres.Component>>> getRecentComponent()
                        throws TException {
                List<org.eclipse.sw360.datahandler.postgres.Component> recentComponents =
                                componentService.getRecentComponents(5);
                List<EntityModel<org.eclipse.sw360.datahandler.postgres.Component>> componentResources =
                                recentComponents.stream()
                                                .map(component -> EntityModel.of(component))
                                                .toList();
                CollectionModel<EntityModel<org.eclipse.sw360.datahandler.postgres.Component>> resources =
                                CollectionModel.of(componentResources);
                return new ResponseEntity<>(resources, HttpStatus.OK);
        }

        @Operation(summary = "Get subscribed components.",
                        description = "List all of the service's mysubscriptions components.",
                        tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL + "/mySubscriptions", method = RequestMethod.GET)
        public ResponseEntity<CollectionModel<EntityModel<Component>>> getMySubscriptions()
                        throws TException {
                User user = restControllerHelper.getSw360UserFromAuthentication();
                List<Component> subscribedComponents = componentService
                                .getSubscribedComponents(user).stream().map(internalComponent -> {
                                        Component openApiComponent = new Component();
                                        openApiComponent.setName(internalComponent.getName());
                                        return openApiComponent;
                                }).toList();
                List<EntityModel<Component>> componentResources = subscribedComponents.stream()
                                .map(component -> EntityModel.of(component)).toList();
                CollectionModel<EntityModel<Component>> resources =
                                CollectionModel.of(componentResources);
                return new ResponseEntity<>(resources, HttpStatus.OK);
        }

        @Operation(summary = "Get components by external ID.",
                        description = "Get components by external ID.", tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL + "/searchByExternalIds", method = RequestMethod.GET)
        public ResponseEntity<CollectionModel<EntityModel<Component>>> searchByExternalIds(
                        @Parameter(description = "The external IDs of the components to be retrieved.",
                                        example = "component-id-key=1831A3&component-id-key=c77321") HttpServletRequest request)
                        throws TException {
                // Logic to fetch components by external IDs
                String queryString = request.getQueryString();
                Map<String, String> externalIds = parseQueryString(queryString);
                List<Component> components =
                                componentService.getComponentsByExternalIds(externalIds);
                List<EntityModel<Component>> componentResources = components.stream()
                                .map(component -> EntityModel.of(component)).toList();
                CollectionModel<EntityModel<Component>> resources =
                                CollectionModel.of(componentResources);
                return new ResponseEntity<>(resources, HttpStatus.OK);
        }

        @PreAuthorize("hasAuthority('WRITE')")
        @Operation(summary = "Delete existing components.",
                        description = "Delete existing components by ids.", tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL + "/{ids}", method = RequestMethod.DELETE)
        public ResponseEntity<List<MultiStatus>> deleteComponents(@Parameter(
                        description = "The ids of the components to be deleted.") @PathVariable("ids") List<String> idsToDelete)
                        throws TException {
                User user = restControllerHelper.getSw360UserFromAuthentication();
                List<MultiStatus> results = new ArrayList<>();
                for (String id : idsToDelete) {
                        RequestStatus requestStatus = componentService.deleteComponent(id, user);
                        if (requestStatus == RequestStatus.SUCCESS) {
                                results.add(new MultiStatus(id, HttpStatus.OK));
                        } else if (requestStatus == RequestStatus.IN_USE) {
                                results.add(new MultiStatus(id, HttpStatus.CONFLICT));
                        } else if (requestStatus == RequestStatus.SENT_TO_MODERATOR) {
                                results.add(new MultiStatus(id, HttpStatus.ACCEPTED));
                        } else {
                                results.add(new MultiStatus(id, HttpStatus.INTERNAL_SERVER_ERROR));
                        }
                }
                return new ResponseEntity<>(results, HttpStatus.MULTI_STATUS);
        }

        @PreAuthorize("hasAuthority('WRITE')")
        @Operation(summary = "Create a new component.", description = "Create a new component.",
                        tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL, method = RequestMethod.POST)
        public ResponseEntity<EntityModel<Component>> createComponent(@Parameter(
                        description = "The component to be created.") @RequestBody org.eclipse.sw360.datahandler.componentsApi.model.Component component)
                        throws URISyntaxException, TException {


                if (component.getName() == null || component.getName().isEmpty()) {
                        throw new IllegalArgumentException("Component name is required");
                }

                User user = restControllerHelper.getSw360UserFromAuthentication();

                if (component.getReleases() == null) {
                        component.setReleases(new ArrayList<>());
                }

                org.eclipse.sw360.datahandler.componentsApi.model.Component createdComponent =
                                componentService.createComponent(component, user);
                logger.info("Created component: " + createdComponent);

                HalResource<Component> halResource = createHalComponent(createdComponent, user);

                URI location = UriComponentsBuilder.fromPath(COMPONENTS_URL).path("/{id}")
                                .buildAndExpand(createdComponent.getId()).toUri();

                return ResponseEntity.created(location).body(halResource);
        }

        @Operation(summary = "Get all releases of a component.",
                        description = "Get all releases of a component.", tags = {"Components"})
        @RequestMapping(value = COMPONENTS_URL + "/{id}/releases", method = RequestMethod.GET)
        public ResponseEntity<CollectionModel<Release>> getReleaseLinksByComponentId(@Parameter(
                        description = "The id of the component.") @PathVariable("id") String id)
                        throws TException {
                final User sw360User = restControllerHelper.getSw360UserFromAuthentication();
                CollectionModel<Release> resources = CollectionModel.of(new ArrayList<>());
                List<Release> releases = releaseService.getReleasesByComponentId(id, sw360User);
                CollectionModel<Release> releaseResources = CollectionModel.of(releases);

                return new ResponseEntity<>(releaseResources, HttpStatus.OK);
        }

        @Operation(summary = "Update the vulnerability of a component.",
                        description = "Update the vulnerability of a component.",
                        tags = {"Components"})
        @PatchMapping(value = COMPONENTS_URL + "/{id}/vulnerabilities")
        public ResponseEntity<CollectionModel<EntityModel<VulnerabilityDTO>>> patchReleaseVulnerabilityRelation(
                        @Parameter(description = "The id of the component.") @PathVariable("id") String componentId,
                        @Parameter(description = "The vulnerability state to be updated.") @RequestBody VulnerabilityState vulnerabilityState)
                        throws TException {
                User user = restControllerHelper.getSw360UserFromAuthentication();
                return new ResponseEntity<>(CollectionModel.empty(), HttpStatus.OK);
        }



}
