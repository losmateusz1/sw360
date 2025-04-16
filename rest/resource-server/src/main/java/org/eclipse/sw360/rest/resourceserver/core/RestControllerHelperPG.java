/*
 * Copyright Siemens AG, 2017-2018. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.rest.resourceserver.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldIdEnum;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.sw360.datahandler.common.CommonUtils;
import org.eclipse.sw360.datahandler.common.SW360Utils;
import org.eclipse.sw360.datahandler.postgres.VendorPG;
import org.eclipse.sw360.datahandler.postgres.ComponentPG;
import org.eclipse.sw360.datahandler.postgres.ProjectPG;
import org.eclipse.sw360.datahandler.postgres.ReleasePG;
// import org.eclipse.sw360.datahandler.postgres.Component;
// import org.eclipse.sw360.datahandler.postgres.Release;
import org.eclipse.sw360.datahandler.resourcelists.PaginationOptions;
import org.eclipse.sw360.datahandler.resourcelists.PaginationParameterException;
import org.eclipse.sw360.datahandler.resourcelists.PaginationResult;
import org.eclipse.sw360.datahandler.resourcelists.ResourceClassNotFoundException;
import org.eclipse.sw360.datahandler.resourcelists.ResourceComparatorGenerator;
import org.eclipse.sw360.datahandler.resourcelists.ResourceListController;
// import org.eclipse.sw360.datahandler.thrift.Comment;
// import org.eclipse.sw360.datahandler.thrift.ClearingRequestSize;
// import org.eclipse.sw360.datahandler.thrift.ProjectReleaseRelationship;
// import org.eclipse.sw360.datahandler.thrift.Quadratic;
// import org.eclipse.sw360.datahandler.thrift.SW360Exception;
// import org.eclipse.sw360.datahandler.thrift.ReleaseRelationship;
// import org.eclipse.sw360.datahandler.thrift.attachments.Attachment;
// import org.eclipse.sw360.datahandler.thrift.components.*;
// import org.eclipse.sw360.datahandler.thrift.licenses.LicenseType;
// import org.eclipse.sw360.datahandler.thrift.packages.Package;
// import org.eclipse.sw360.datahandler.thrift.licenses.License;
// import org.eclipse.sw360.datahandler.thrift.licenses.Obligation;
// import org.eclipse.sw360.datahandler.thrift.moderation.ModerationRequest;
// import org.eclipse.sw360.datahandler.thrift.projects.ClearingRequest;
// import org.eclipse.sw360.datahandler.thrift.projects.Project;
// import org.eclipse.sw360.datahandler.thrift.projects.ProjectService;
// import org.eclipse.sw360.datahandler.thrift.projects.ProjectDTO;
// import org.eclipse.sw360.datahandler.thrift.spdx.documentcreationinformation.*;
// import org.eclipse.sw360.datahandler.thrift.spdx.spdxdocument.SPDXDocument;
// import org.eclipse.sw360.datahandler.thrift.spdx.spdxpackageinfo.PackageInformation;
// import org.eclipse.sw360.datahandler.thrift.users.RequestedAction;
import org.eclipse.sw360.datahandler.thrift.users.User;
// import org.eclipse.sw360.datahandler.thrift.vendors.Vendor;
// import org.eclipse.sw360.datahandler.thrift.vulnerabilities.*;
import org.eclipse.sw360.rest.resourceserver.attachment.AttachmentController;
import org.eclipse.sw360.rest.resourceserver.clearingrequest.Sw360ClearingRequestService;
import org.eclipse.sw360.rest.resourceserver.component.ComponentController;
import org.eclipse.sw360.rest.resourceserver.license.LicenseController;
import org.eclipse.sw360.rest.resourceserver.license.Sw360LicenseService;
import org.eclipse.sw360.rest.resourceserver.moderationrequest.EmbeddedModerationRequest;
import org.eclipse.sw360.rest.resourceserver.moderationrequest.ModerationRequestController;
import org.eclipse.sw360.rest.resourceserver.moderationrequest.Sw360ModerationRequestService;
import org.eclipse.sw360.rest.resourceserver.obligation.Sw360ObligationService;
import org.eclipse.sw360.rest.resourceserver.project.EmbeddedProject;
import org.jetbrains.annotations.NotNull;
import org.eclipse.sw360.rest.resourceserver.project.EmbeddedProjectDTO;
import org.springframework.security.access.AccessDeniedException;
import org.eclipse.sw360.rest.resourceserver.project.ProjectController;
import org.eclipse.sw360.rest.resourceserver.obligation.ObligationController;
import org.eclipse.sw360.rest.resourceserver.packages.PackageController;
import org.eclipse.sw360.rest.resourceserver.packages.SW360PackageService;
import org.eclipse.sw360.rest.resourceserver.project.Sw360ProjectService;
import org.eclipse.sw360.rest.resourceserver.release.ReleaseController;
import org.eclipse.sw360.rest.resourceserver.release.Sw360ReleaseService;
import org.eclipse.sw360.rest.resourceserver.releasesOpenAPI.ReleaseOpenAPIController;
import org.eclipse.sw360.rest.resourceserver.releasesOpenAPI.ReleaseServicePG;
import org.eclipse.sw360.rest.resourceserver.user.Sw360UserService;
import org.eclipse.sw360.rest.resourceserver.user.UserController;
import org.eclipse.sw360.rest.resourceserver.vendor.Sw360VendorService;
import org.eclipse.sw360.rest.resourceserver.vendor.VendorController;
import org.eclipse.sw360.rest.resourceserver.vendorOpenAPI.VendorServicePG;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.core.EmbeddedWrapper;
import org.springframework.hateoas.server.core.EmbeddedWrappers;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import static org.eclipse.sw360.datahandler.permissions.PermissionUtils.makePermission;

import jakarta.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.eclipse.sw360.datahandler.common.CommonUtils.isNullEmptyOrWhitespace;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import org.eclipse.sw360.datahandler.componentsApi.model.ComponentAPI;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RestControllerHelperPG<T> {

        @NonNull
        private final Sw360UserService userService;

        @NonNull
        private final Sw360VendorService vendorService;

        @NonNull
        private final VendorServicePG vendorServicePG = new VendorServicePG();

        @NonNull
        private final Sw360LicenseService licenseService;

        @NonNull
        private final Sw360ObligationService obligationService;

        @NonNull
        private final Sw360ClearingRequestService clearingRequestService;

        @NonNull
        private final ResourceComparatorGenerator<T> resourceComparatorGenerator =
                        new ResourceComparatorGenerator<>();

        @NonNull
        private final ResourceListController<T> resourceListController =
                        new ResourceListController<>();

        private static final Logger LOGGER = LogManager.getLogger(RestControllerHelperPG.class);

        private static final String PAGINATION_KEY_FIRST = "first";
        private static final String PAGINATION_KEY_PREVIOUS = "previous";
        private static final String PAGINATION_KEY_NEXT = "next";
        private static final String PAGINATION_KEY_LAST = "last";
        private static final String PAGINATION_PARAM_PAGE = "page";
        private static final double MIN_CVSS = 0;
        private static final double MAX_CVSS = 10;
        public static final String PAGINATION_PARAM_PAGE_ENTRIES = "page_entries";
        private static final String JWT_SUBJECT = "sub";

        @NonNull
        private final com.fasterxml.jackson.databind.Module sw360Module;

        public org.eclipse.sw360.datahandler.postgres.VendorPG convertToEmbeddedVendorPG(
                        org.eclipse.sw360.datahandler.postgres.VendorPG vendor) {
                org.eclipse.sw360.datahandler.postgres.VendorPG embeddedVendor =
                                new org.eclipse.sw360.datahandler.postgres.VendorPG();
                // embeddedVendor.setId(vendor.getId());
                embeddedVendor.setShortname(vendor.getShortname());
                embeddedVendor.setFullname(vendor.getFullname());
                embeddedVendor.setUrl(vendor.getUrl());
                return embeddedVendor;
        }

        public org.eclipse.sw360.datahandler.postgres.VendorPG convertToEmbeddedVendorPG(
                        String fullName) {
                org.eclipse.sw360.datahandler.postgres.VendorPG embeddedVendor =
                                new org.eclipse.sw360.datahandler.postgres.VendorPG();
                embeddedVendor.setFullname(fullName);
                // embeddedVendor.setType(null);
                return embeddedVendor;
        }

        public HalResource<org.eclipse.sw360.datahandler.postgres.VendorPG> addEmbeddedVendorPG(
                        String vendorFullName) {
                org.eclipse.sw360.datahandler.postgres.VendorPG embeddedVendor =
                                convertToEmbeddedVendorPG(vendorFullName);
                HalResource<org.eclipse.sw360.datahandler.postgres.VendorPG> halVendor =
                                new HalResource<>(embeddedVendor);
                // try {
                org.eclipse.sw360.datahandler.postgres.VendorPG vendorByFullName =
                                vendorServicePG.getVendorByFullName(vendorFullName);
                System.out.println("vendorByFullName: " + vendorByFullName);
                if (vendorByFullName != null) {
                        Link vendorSelfLink = linkTo(UserController.class)
                                        .slash("api" + VendorController.VENDORS_URL + "/"
                                                        + vendorByFullName.getId())
                                        .withSelfRel();
                        halVendor.add(vendorSelfLink);
                }
                return halVendor;
                // } catch (Exception e) {
                // LOGGER.error("cannot create self link for vendor with full name: "
                // + vendorFullName);
                // return null;
                // }
        }

        public void addEmbeddedVendorsPG(
                        HalResource<org.eclipse.sw360.datahandler.postgres.ComponentPG> halComponent,
                        Set<String> vendorFullnames) {
                for (String vendorFullName : vendorFullnames) {
                        HalResource<org.eclipse.sw360.datahandler.postgres.VendorPG> vendorHalResource =
                                        addEmbeddedVendorPG(vendorFullName);
                        halComponent.addEmbeddedResource("vendors", vendorHalResource);
                }
        }

        public void addEmbeddedReleasesPG(HalResource halResource, List<String> releases,
                        ReleaseServicePG sw360ReleaseService, User user) throws TException {
                for (String releaseId : releases) {
                        final ReleasePG release =
                                        sw360ReleaseService.getReleaseForUserById(releaseId, user);
                        addEmbeddedReleasePG(halResource, release);
                }
        }

        public void addEmbeddedReleasesPG(HalResource halResource,
                        List<org.eclipse.sw360.datahandler.componentsApi.model.ReleaseAPI> releases) {
                for (org.eclipse.sw360.datahandler.componentsApi.model.ReleaseAPI release : releases) {
                        addEmbeddedReleasePG(halResource, release);
                }
        }

        public void addEmbeddedVulnerabilitiesPG(HalResource halResource,
                        List<org.eclipse.sw360.datahandler.postgres.VulnerabilityPG> vulnerabilities) {
                for (org.eclipse.sw360.datahandler.postgres.VulnerabilityPG vulnerability : vulnerabilities) {
                        HalResource<org.eclipse.sw360.datahandler.postgres.VulnerabilityPG> halVulnerability =
                                        new HalResource<>(vulnerability);
                        Link vulnerabilityLink = linkTo(ReleaseOpenAPIController.class)
                                        .slash("api/vulnerabilities/" + vulnerability.getId())
                                        .withSelfRel();
                        halVulnerability.add(vulnerabilityLink);
                        halResource.addEmbeddedResource("vulnerabilities", halVulnerability);
                }
        }

        public void addEmbeddedComponentPG(HalResource halResource,
                        org.eclipse.sw360.datahandler.postgres.ComponentPG component) {
                ComponentPG embeddedComponent = convertToEmbeddedComponent(component);
                HalResource<ComponentPG> halComponent = new HalResource<>(embeddedComponent);
                Link componentLink = linkTo(ComponentController.class)
                                .slash("api/componentsOpenAPI/" + component.getId()).withSelfRel();
                halComponent.add(componentLink);
                halResource.addEmbeddedResource("components", halComponent);
        }

        public void addEmbeddedReleasePG(HalResource halResource,
                        org.eclipse.sw360.datahandler.componentsApi.model.ReleaseAPI release) {
                org.eclipse.sw360.datahandler.postgres.ReleasePG embeddedRelease =
                                convertToEmbeddedReleasePG(release);
                HalResource<org.eclipse.sw360.datahandler.postgres.ReleasePG> halRelease =
                                new HalResource<>(embeddedRelease);
                Link releaseLink = linkTo(ReleaseOpenAPIController.class)
                                .slash("api/releases/" + release.getId()).withSelfRel();
                halRelease.add(releaseLink);
                halResource.addEmbeddedResource("releases", halRelease);
        }

        public ComponentPG convertToEmbeddedComponent(ComponentPG component, List<String> fields) {
                return this.convertToEmbeddedComponent(component);
        }

        public ComponentPG convertToEmbeddedComponent(ComponentPG component) {
                ComponentPG embeddedComponent = new ComponentPG();
                embeddedComponent.setId(component.getId());
                embeddedComponent.setName(component.getName());
                if (CommonUtils.isNotNullEmptyOrWhitespace(component.getDefaultVendorId())) {
                        try {
                                VendorPG defaultVendor = vendorServicePG
                                                .getVendorById(component.getDefaultVendorId());
                                embeddedComponent.setDefaultVendor(defaultVendor);
                        } catch (RuntimeException e) {
                                LOGGER.error("Failed to retrieve default vendor '{}' from SW360 database.",
                                                component.getDefaultVendorId(), e);
                        }
                }
                return embeddedComponent;
        }

        public org.eclipse.sw360.datahandler.postgres.ReleasePG convertToEmbeddedReleasePG(
                        org.eclipse.sw360.datahandler.componentsApi.model.ReleaseAPI release) {
                org.eclipse.sw360.datahandler.postgres.ReleasePG embeddedRelease =
                                new org.eclipse.sw360.datahandler.postgres.ReleasePG();
                embeddedRelease.setId(release.getId());
                embeddedRelease.setName(release.getName());
                embeddedRelease.setVersion(release.getVersion());
                // embeddedRelease.setClearingState(release.getClearingState());
                // embeddedRelease.setType(null);
                return embeddedRelease;
        }

        public void addEmbeddedDataToComponentOAPI(HalResource halResource,
                        ComponentAPI sw360Component) {
                addEmbeddedModifiedByToComponent(halResource, sw360Component);
                addEmbeddedComponentOwnerToComponent(halResource, sw360Component);
                addEmbeddedSubcribeToHalResourceComponent(halResource, sw360Component);
        }

        public void addEmbeddedModifiedByToComponent(HalResource halResource,
                        ComponentAPI sw360Component) {
                if (sw360Component.getModifiedBy() != null) {
                        User componentModify = getUserByEmail(sw360Component.getModifiedBy());
                        if (null != componentModify)
                                addEmbeddedUser(halResource, componentModify, "modifiedBy");
                }
        }

        public void addEmbeddedComponentOwnerToComponent(HalResource halResource,
                        ComponentAPI sw360Component) {
                if (sw360Component.getComponentOwner() != null) {
                        User componentOwner = getUserByEmail(sw360Component.getComponentOwner());
                        if (null != componentOwner) {
                                addEmbeddedUser(halResource, componentOwner, "componentOwner");
                                sw360Component.setComponentOwner(null);
                        }
                }
        }

        public void addEmbeddedSubcribeToHalResourceComponent(HalResource halResource,
                        ComponentAPI sw360Component) {
                if (!CommonUtils.isNullOrEmptyCollection(sw360Component.getSubscribers())) {
                        List<String> subscribers = sw360Component.getSubscribers();
                        for (String subscribersEmail : subscribers) {
                                User sw360User = getUserByEmail(subscribersEmail);
                                if (null != sw360User) {
                                        addEmbeddedUser(halResource, sw360User, "subscribers");
                                        sw360Component.setSubscribers(null);
                                }
                        }
                }
        }

        public User getUserByEmail(String emailId) {
                User sw360User;
                try {
                        sw360User = userService.getUserByEmail(emailId);
                } catch (RuntimeException e) {
                        sw360User = new User();
                        sw360User.setId(emailId).setEmail(emailId);
                        LOGGER.debug("Could not get user object from backend with email: "
                                        + emailId);
                }
                return sw360User;
        }

        public void addEmbeddedUser(HalResource halResource, User user, String relation) {
                User embeddedUser = convertToEmbeddedUser(user);
                EntityModel<User> embeddedUserResource = EntityModel.of(embeddedUser);
                try {
                        Link userLink = linkTo(UserController.class)
                                        .slash("api/users/byid/"
                                                        + URLEncoder.encode(user.getId(), "UTF-8"))
                                        .withSelfRel();
                        embeddedUserResource.add(userLink);
                        halResource.addEmbeddedResource(relation, embeddedUserResource);
                } catch (UnsupportedEncodingException e) {
                        LOGGER.error("cannot create embedded user with email: " + user.getEmail(),
                                        e);
                }
        }

        public User convertToEmbeddedUser(User user) {
                User embeddedUser = new User();
                embeddedUser.setId(user.getId());
                embeddedUser.setFullname(user.getFullname());
                embeddedUser.setEmail(user.getEmail());
                embeddedUser.setType(null);
                return embeddedUser;
        }

        public void addEmbeddedComponents(HalResource<ProjectPG> halProject,
                        List<ComponentPG> components) {
                for (ComponentPG component : components) {
                        ComponentPG embeddedComponent = convertToEmbeddedComponent(component);
                        HalResource<ComponentPG> halComponent =
                                        new HalResource<>(embeddedComponent);
                        Link componentLink = linkTo(ComponentController.class)
                                        .slash("api/componentsOpenAPI/" + component.getId())
                                        .withSelfRel();
                        halComponent.add(componentLink);
                        halProject.addEmbeddedResource("components", halComponent);
                }
        }
}
