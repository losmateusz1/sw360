package org.eclipse.sw360.datahandler.postgres;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@Entity
@Table
@Transactional
public class Component extends org.eclipse.sw360.datahandler.componentsApi.model.Component
        implements Serializable {
    // public class Component implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
    @Column(nullable = true)
    private String revision;
    @Column(nullable = true)
    private String description;
    @Column(nullable = true)
    private String componentType;
    @Column(nullable = true)
    private String createdBy;
    @Column(nullable = true)
    private String ownerAccountingUnit;
    @Column(nullable = true)
    private String ownerGroup;
    @Column(nullable = true)
    private String ownerCountry;
    @Column(nullable = true)
    private String visibility;
    @Column(nullable = true)
    private String businessUnit;
    @Column(nullable = true)
    private String cdxComponentType;
    @Column(nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> externalIds;
    @Column(nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> additionalData;
    @Column(nullable = true)
    private List<String> mainLicenseIds;
    @Column(nullable = true)
    private String defaultVendorId;
    @Column(nullable = true)
    private List<String> categories;
    @Column(nullable = true)
    // @Temporal(TemporalType.DATE)
    private Date createdOn;
    @Column(nullable = true)
    private String componentOwner;
    @Column(nullable = true)
    private String modifiedBy;
    @Column(nullable = true)
    // @Temporal(TemporalType.DATE)
    private Date modifiedOn;
    @Column(nullable = true)
    private String homepage;
    @Column(nullable = true)
    private String mailinglist;
    @Column(nullable = true)
    private String wiki;
    @Column(nullable = true)
    private String blog;
    @Column(nullable = true)
    private String wikipedia;
    @Column(nullable = true)
    private String openHub;
    @Column(nullable = true)
    private String vcs;
    // @Enumerated(EnumType.STRING)
    // @Column(nullable = true)
    // private DocumentState documentState;
    @ElementCollection
    @CollectionTable(name = "permissions", joinColumns = @JoinColumn(name = "component_id"))
    @MapKeyColumn(name = "action")
    @Column(name = "allowed")
    private Map<String, Boolean> permissions;
    @ElementCollection
    @CollectionTable(name = "languages", joinColumns = @JoinColumn(name = "component_id"))
    @Column(name = "language")
    private Set<String> languages;
    @ElementCollection
    @CollectionTable(name = "software_platforms", joinColumns = @JoinColumn(name = "component_id"))
    @Column(name = "platform")
    private Set<String> softwarePlatforms;
    @ElementCollection
    @CollectionTable(name = "operating_systems", joinColumns = @JoinColumn(name = "component_id"))
    @Column(name = "os")
    private Set<String> operatingSystems;
    @ElementCollection
    @CollectionTable(name = "vendor_names", joinColumns = @JoinColumn(name = "component_id"))
    @Column(name = "vendor_name")
    private Set<String> vendorNames;

    @OneToMany(mappedBy = "component", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Release> releases;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinTable(name = "component_vendors", joinColumns = @JoinColumn(name = "component_id"),
            inverseJoinColumns = @JoinColumn(name = "vendor_id"))
    private Set<Vendor> vendors = new HashSet<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }

    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    public Component(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public Set<Vendor> setVendors() {
        return vendors;
    }

    @Override
    public List<String> getVendorNames() {
        return vendors.stream().map(vendor -> vendor.getFullname()).collect(Collectors.toList());
    }

    public Component(UUID id, String name, String revision, String description,
            String componentType, String createdBy, String ownerAccountingUnit, String ownerGroup,
            String ownerCountry, String visibility, String businessUnit, String cdxComponentType,
            Map<String, String> externalIds, Map<String, String> additionalData,
            List<String> mainLicenseIds, String defaultVendorId, List<String> categories,
            Date createdOn, String componentOwner, String modifiedBy, Date modifiedOn,
            String homepage, String mailinglist, String wiki, String blog, String wikipedia,
            String openHub, String vcs, Map<String, Boolean> permissions, Set<String> languages,
            Set<String> softwarePlatforms, Set<String> operatingSystems, Set<String> vendorNames) {
        this.id = id;
        this.name = name;
        this.revision = revision;
        this.description = description;
        this.componentType = componentType;
        this.createdBy = createdBy;
        this.ownerAccountingUnit = ownerAccountingUnit;
        this.ownerGroup = ownerGroup;
        this.ownerCountry = ownerCountry;
        this.visibility = visibility;
        this.businessUnit = businessUnit;
        this.cdxComponentType = cdxComponentType;
        this.externalIds = externalIds;
        this.additionalData = additionalData;
        this.mainLicenseIds = mainLicenseIds;
        this.defaultVendorId = defaultVendorId;
        this.categories = categories;
        this.createdOn = createdOn;
        this.componentOwner = componentOwner;
        this.modifiedBy = modifiedBy;
        this.modifiedOn = modifiedOn;
        this.homepage = homepage;
        this.mailinglist = mailinglist;
        this.wiki = wiki;
        this.blog = blog;
        this.wikipedia = wikipedia;
        this.openHub = openHub;
        this.vcs = vcs;
        this.permissions = permissions;
        this.languages = languages;
        this.softwarePlatforms = softwarePlatforms;
        this.operatingSystems = operatingSystems;
        this.vendorNames = vendorNames;
    }

    public Component() {
        // this.id = UUID.randomUUID();
        this.createdOn = new Date();
        this.modifiedOn = new Date();
        // this.createdBy = "system";
        // this.modifiedBy = "system";
        // this.documentState = DocumentState.DRAFT;
        // this.permissions = Map.of("read", true, "write", false);
    }

    public Component(org.eclipse.sw360.datahandler.componentsApi.model.Component component) {
        // this.createdOn = component.getCreatedOn();
        // this.modifiedOn = component.getModifiedOn();
        this.createdBy = component.getCreatedBy();
        this.modifiedBy = component.getModifiedBy();
        this.name = component.getName();
        this.revision = component.getRevision();
        this.description = component.getDescription();
        this.additionalData = component.getAdditionalData();
        // this.componentType = component.getComponentType();
        // this.createdOn = component.getCreatedOn();
        // this.modifiedOn = component.getModifiedOn();
        this.ownerAccountingUnit = component.getOwnerAccountingUnit();
        this.ownerGroup = component.getOwnerGroup();
        this.ownerCountry = component.getOwnerCountry();
        // this.visibility = component.getVisibility();
        this.businessUnit = component.getBusinessUnit();
        // this.cdxComponentType = component.getCdxComponentType();
        this.externalIds = component.getExternalIds();
        this.mainLicenseIds = component.getMainLicenseIds();
        this.defaultVendorId = component.getDefaultVendorId();
        this.categories = component.getCategories();
        this.componentOwner = component.getComponentOwner();
        this.homepage = component.getHomepage();
        this.mailinglist = component.getMailinglist();
        this.wiki = component.getWiki();
        this.blog = component.getBlog();
        this.wikipedia = component.getWikipedia();
        this.openHub = component.getOpenHub();
        this.vcs = component.getVcs();
        // this.documentState = component.getDocumentState();
        this.permissions = component.getPermissions();
        this.languages = component.getLanguages().stream().map(language -> language)
                .collect(Collectors.toSet());
        this.softwarePlatforms = component.getSoftwarePlatforms().stream()
                .map(softwarePlatform -> softwarePlatform).collect(Collectors.toSet());
        this.operatingSystems = component.getOperatingSystems().stream()
                .map(operatingSystem -> operatingSystem).collect(Collectors.toSet());
        this.vendorNames = component.getVendorNames().stream().map(vendorName -> vendorName)
                .collect(Collectors.toSet());


    }

    @Override
    public String toString() {
        return "Component{" + "id=" + id + ", name='" + name + '\'' + ", revision='" + revision
                + '\'' + ", description='" + description + '\'' + ", componentType='"
                + componentType + '\'' + ", createdBy='" + createdBy + '\''
                + ", ownerAccountingUnit='" + ownerAccountingUnit + '\'' + ", ownerGroup='"
                + ownerGroup + '\'' + ", ownerCountry='" + ownerCountry + '\'' + ", visibility='"
                + visibility + '\'' + ", businessUnit='" + businessUnit + '\''
                + ", cdxComponentType='" + cdxComponentType + '\'' + ", externalIds='" + externalIds
                + '\'' + ", additionalData='" + additionalData + '\'' + ", mainLicenseIds='"
                + mainLicenseIds + '\'' + ", defaultVendorId='" + defaultVendorId + '\''
                + ", categories='" + categories + '\'' + ", createdOn='" + createdOn + '\''
                + ", componentOwner='" + componentOwner + '\'' + ", modifiedBy='" + modifiedBy
                + '\'' + ", modifiedOn='" + modifiedOn + '\'' + ", homepage='" + homepage + '\''
                + ", mailinglist='" + mailinglist + '\'' + ", wiki='" + wiki + '\'' + ", blog='"
                + blog + '\'' + ", wikipedia='" + wikipedia + '\'' + ", openHub='" + openHub + '\''
                + ", vcs='" + vcs + '\'' + ", permissions='" + permissions + '\'' + ", languages='"
                + languages + '\'' + ", softwarePlatforms='" + softwarePlatforms + '\''
                + ", operatingSystems='" + operatingSystems + '\'' + ", vendorNames='" + vendorNames
                + '\'' + '}';
    }

    public String getRevision() {
        return revision;
    }

    @Override
    @Valid
    @Schema(name = "releases", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    public List<org.eclipse.sw360.datahandler.componentsApi.model.Release> getReleases() {
        if (releases == null) {
            return List.of();
        } else if (releases.isEmpty()) {
            return List.of();
        } else {
            return releases.stream().map(release -> {
                org.eclipse.sw360.datahandler.componentsApi.model.Release releaseModel =
                        new org.eclipse.sw360.datahandler.componentsApi.model.Release();
                releaseModel.setId(release.getId());
                releaseModel.setName(release.getName());
                releaseModel.setVersion(release.getVersion());
                return releaseModel;
            }).collect(Collectors.toList());
        }
    }

    @Override
    public void setReleases(
            List<org.eclipse.sw360.datahandler.componentsApi.model.Release> releases) {
        this.releases = releases.stream().map(release -> {
            Release releaseModel = new Release(release);
            // releaseModel.setId(release.getId());
            // releaseModel.setName(release.getName());
            // releaseModel.setVersion(release.getVersion());
            return releaseModel;
        }).collect(Collectors.toList());
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // public String getComponentType() {
    // return componentType;
    // }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getOwnerAccountingUnit() {
        return ownerAccountingUnit;
    }

    public void setOwnerAccountingUnit(String ownerAccountingUnit) {
        this.ownerAccountingUnit = ownerAccountingUnit;
    }

    public String getOwnerGroup() {
        return ownerGroup;
    }

    public void setOwnerGroup(String ownerGroup) {
        this.ownerGroup = ownerGroup;
    }

    public String getOwnerCountry() {
        return ownerCountry;
    }

    public void setOwnerCountry(String ownerCountry) {
        this.ownerCountry = ownerCountry;
    }

    // public String getVisibility() {
    // return visibility;
    // }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getBusinessUnit() {
        return businessUnit;
    }

    public void setBusinessUnit(String businessUnit) {
        this.businessUnit = businessUnit;
    }

    // public String getCdxComponentType() {
    // return cdxComponentType;
    // }

    public void setCdxComponentType(String cdxComponentType) {
        this.cdxComponentType = cdxComponentType;
    }

    // public String getExternalIds() {
    // return externalIds;
    // }

    public void setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
    }

    // public String getMainLicenseIds() {
    // return mainLicenseIds;
    // }

    public void setMainLicenseIds(List<String> mainLicenseIds) {
        this.mainLicenseIds = mainLicenseIds;
    }

    public void setDefaultVendor(Vendor defaultVendor) {
        this.vendors.add(defaultVendor);
    }

    public String getDefaultVendorId() {
        return defaultVendorId;
    }

    public void setDefaultVendorId(String defaultVendorId) {
        this.defaultVendorId = defaultVendorId;
    }

    // public String getCategories() {
    // return categories;
    // }

    // public void setCategories(String categories) {
    // this.categories = categories;
    // }

    // public Date getCreatedOn() {
    // return createdOn;
    // }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getComponentOwner() {
        return componentOwner;
    }

    public void setComponentOwner(String componentOwner) {
        this.componentOwner = componentOwner;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    // public Date getModifiedOn() {
    // return modifiedOn;
    // }

    // public void setModifiedOn(Date modifiedOn) {
    // this.modifiedOn = modifiedOn;
    // }

    // public String getHomepage() {
    // return homepage;
    // }

    // public void setHomepage(String homepage) {
    // this.homepage = homepage;
    // }

    // public String getMailinglist() {
    // return mailinglist;
    // }

    // public void setMailinglist(String mailinglist) {
    // this.mailinglist = mailinglist;
    // }

    // public String getWiki() {
    // return wiki;
    // }

    // public void setWiki(String wiki) {
    // this.wiki = wiki;
    // }

    // public String getBlog() {
    // return blog;
    // }

    // public void setBlog(String blog) {
    // this.blog = blog;
    // }

    // public String getWikipedia() {
    // return wikipedia;
    // }

    // public void setWikipedia(String wikipedia) {
    // this.wikipedia = wikipedia;
    // }

    // public String getOpenHub() {
    // return openHub;
    // }

    // public void setOpenHub(String openHub) {
    // this.openHub = openHub;
    // }

    // public String getVcs() {
    // return vcs;
    // }

    // public void setVcs(String vcs) {
    // this.vcs = vcs;
    // }

    // public DocumentState getDocumentState() {
    // return documentState;
    // }

    // public void setDocumentState(DocumentState documentState) {
    // this.documentState = documentState;
    // }

    // public Map<String, Boolean> getPermissions() {
    // return permissions;
    // }

    // public void setPermissions(Map<String, Boolean> permissions) {
    // this.permissions = permissions;
    // }

    // public Set<String> getLanguages() {
    // return languages;
    // }

    // public void setLanguages(Set<String> languages) {
    // this.languages = languages;
    // }

    // public Set<String> getSoftwarePlatforms() {
    // return softwarePlatforms;
    // }

    // public void setSoftwarePlatforms(Set<String> softwarePlatforms) {
    // this.softwarePlatforms = softwarePlatforms;
    // }

    // public Set<String> getOperatingSystems() {
    // return operatingSystems;
    // }

    // public void setOperatingSystems(Set<String> operatingSystems) {
    // this.operatingSystems = operatingSystems;
    // }

    // public Set<String> getVendorNames() {
    // return vendorNames;
    // }

    // public void setVendorNames(Set<String> vendorNames) {
    // this.vendorNames = vendorNames;
    // }

    // public List<org.eclipse.sw360.datahandler.componentsApi.model.Release>
    // getDBReleases() {
    // return releases.stream().map(release -> {
    // org.eclipse.sw360.datahandler.componentsApi.model.Release releaseModel =
    // new org.eclipse.sw360.datahandler.componentsApi.model.Release();
    // releaseModel.setId(release.getId());
    // releaseModel.setName(release.getName());
    // releaseModel.setVersion(release.getVersion());
    // return releaseModel;
    // }).collect(Collectors.toList());
    // }

}
