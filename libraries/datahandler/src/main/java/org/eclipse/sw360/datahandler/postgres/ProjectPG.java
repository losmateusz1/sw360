package org.eclipse.sw360.datahandler.postgres;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import org.eclipse.sw360.datahandler.componentsApi.model.ComponentAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@Entity
@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@Table(name = "project")
public class ProjectPG extends ProjectAPI implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "revision", nullable = true)
    private String revision;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "version", nullable = true)
    private String version;

    @Column(name = "domain", nullable = true)
    private String domain;

    @Column(name = "attachments", nullable = true)
    private List<String> attachments = new ArrayList<>();

    @Column(name = "created_on", nullable = true)
    private String createdOn;

    @Column(name = "business_unit", nullable = true)
    private String businessUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = true)
    private StateEnum state;

    @Enumerated(EnumType.STRING)
    @Column(name = "project_type", nullable = true)
    private ProjectTypeEnum projectType;

    @Column(name = "tag", nullable = true)
    private String tag;

    @Enumerated(EnumType.STRING)
    @Column(name = "clearing_state", nullable = true)
    private ClearingStateEnum clearingState;

    @Column(name = "created_by", nullable = true)
    private String createdBy;

    @Column(name = "project_responsible", nullable = true)
    private String projectResponsible;

    @Column(name = "lead_architect", nullable = true)
    private String leadArchitect;

    @Column(name = "moderators", nullable = true)
    private List<String> moderators = new ArrayList<>();

    @Column(name = "contributors", nullable = true)
    private List<String> contributors = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = true)
    private VisibilityEnum visibility;

    @Column(name = "roles", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, List<String>> roles = new HashMap<>();

    @Column(name = "security_responsibles", nullable = true)
    private List<String> securityResponsibles = new ArrayList<>();

    @Column(name = "project_owner", nullable = true)
    private String projectOwner;

    @Column(name = "owner_accounting_unit", nullable = true)
    private String ownerAccountingUnit;

    @Column(name = "owner_group", nullable = true)
    private String ownerGroup;

    @Column(name = "owner_country", nullable = true)
    private String ownerCountry;

    @Column(name = "linked_projects", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> linkedProjects = new HashMap<>();

    @Column(name = "release_id_to_usage", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> releaseIdToUsage = new HashMap<>();

    @Column(name = "package_ids", nullable = true)
    private List<String> packageIds = new ArrayList<>();

    @Column(name = "clearing_team", nullable = true)
    private String clearingTeam;

    @Column(name = "preevaluation_deadline", nullable = true)
    private String preevaluationDeadline;

    @Column(name = "system_test_start", nullable = true)
    private String systemTestStart;

    @Column(name = "system_test_end", nullable = true)
    private String systemTestEnd;

    @Column(name = "delivery_start", nullable = true)
    private String deliveryStart;

    @Column(name = "phase_out_since", nullable = true)
    private String phaseOutSince;

    @Column(name = "enable_svm", nullable = true)
    private Boolean enableSvm;

    @Column(name = "external_ids", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> externalIds = new HashMap<>();

    @Column(name = "additional_data", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> additionalData = new HashMap<>();

    @Column(name = "consider_releases_from_external_list", nullable = true)
    private Boolean considerReleasesFromExternalList;

    @Column(name = "license_info_header_text", nullable = true)
    private String licenseInfoHeaderText;

    @Column(name = "enable_vulnerabilities_display", nullable = true)
    private Boolean enableVulnerabilitiesDisplay;

    @Column(name = "obligations_text", nullable = true)
    private String obligationsText;

    @Column(name = "clearing_summary", nullable = true)
    private String clearingSummary;

    @Column(name = "special_risks_oss", nullable = true)
    private String specialRisksOSS;

    @Column(name = "general_risks_3rd_party", nullable = true)
    private String generalRisks3rdParty;

    @Column(name = "special_risks_3rd_party", nullable = true)
    private String specialRisks3rdParty;

    @Column(name = "delivery_channels", nullable = true)
    private String deliveryChannels;

    @Column(name = "remarks_additional_requirements", nullable = true)
    private String remarksAdditionalRequirements;

    @Column(name = "document_state", nullable = true)
    private String documentState;

    @Column(name = "clearing_request_id", nullable = true)
    private String clearingRequestId;

    @Column(name = "release_clearing_state_summary", nullable = true)
    private String releaseClearingStateSummary;

    @Column(name = "linked_obligation_id", nullable = true)
    private String linkedObligationId;

    @Column(name = "permissions", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Boolean> permissions = new HashMap<>();

    @Column(name = "external_urls", nullable = true)
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> externalUrls = new HashMap<>();

    @Column(name = "vendor", nullable = true)
    private String vendor;

    @Column(name = "vendor_id", nullable = true)
    private String vendorId;

    @Column(name = "modified_by", nullable = true)
    private String modifiedBy;

    @Column(name = "modified_on", nullable = true)
    private String modifiedOn;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "project_components", joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "component_id"))
    private List<ComponentPG> components = new ArrayList<>();

    // Getters and setters for all fields

    public ProjectPG() {
        super();
    }

    public ProjectPG(org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI project) {
        this.revision = project.getRevision();
        this.name = project.getName();
        this.description = project.getDescription();
        this.version = project.getVersion();
        this.domain = project.getDomain();
        this.createdOn = project.getCreatedOn();
        this.businessUnit = project.getBusinessUnit();
        this.state = project.getState() != null ? StateEnum.valueOf(project.getState().name())
                : StateEnum.UNKNOWN;
        this.projectType = project.getProjectType() != null
                ? ProjectTypeEnum.valueOf(project.getProjectType().name())
                : ProjectTypeEnum.INTERNAL;
        this.tag = project.getTag();
        this.clearingState = project.getClearingState() != null
                ? ClearingStateEnum.valueOf(project.getClearingState().name())
                : ClearingStateEnum.OPEN;
        // this.createdBy = project.getCreatedBy();
        this.projectResponsible = project.getProjectResponsible();
        this.leadArchitect = project.getLeadArchitect();
        this.moderators = new ArrayList<>(project.getModerators());
        this.contributors = new ArrayList<>(project.getContributors());
        this.visibility = project.getVisibility() != null
                ? VisibilityEnum.valueOf(project.getVisibility().name())
                : VisibilityEnum.RESTRICTED;
        this.roles.putAll(project.getRoles());
        this.securityResponsibles = new ArrayList<>(project.getSecurityResponsibles());
        this.projectOwner = project.getProjectOwner();
        this.ownerAccountingUnit = project.getOwnerAccountingUnit();
        this.ownerGroup = project.getOwnerGroup();
        this.ownerCountry = project.getOwnerCountry();
        this.releaseIdToUsage.putAll(project.getReleaseIdToUsage());
        this.packageIds.addAll(project.getPackageIds());
        this.clearingTeam = project.getClearingTeam();
        this.preevaluationDeadline = project.getPreevaluationDeadline();
        this.systemTestStart = project.getSystemTestStart();
        this.systemTestEnd = project.getSystemTestEnd();
        this.deliveryStart = project.getDeliveryStart();
        this.phaseOutSince = project.getPhaseOutSince();
        this.externalIds.putAll(project.getExternalIds());
        this.additionalData.putAll(project.getAdditionalData());
        this.components = project.getComponents().stream().map(ComponentPG::new).toList();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public StateEnum getState() {
        return state;
    }

    @Override
    public void setState(StateEnum state) {
        this.state = state;
    }

    @Override
    public ProjectTypeEnum getProjectType() {
        return projectType;
    }

    @Override
    public void setProjectType(ProjectTypeEnum projectType) {
        this.projectType = projectType;
    }

    // @Override
    // public Set<String> getAttachments() {
    // return attachments;
    // }

    // public void setAttachments(List<String> attachments) {
    // this.attachments = attachments;
    // }

    // @Override
    // public User setCreatedBy(User createdBy) {
    // this.createdBy = createdBy;
    // return createdBy;
    // }

    @Override
    public List<String> getModerators() {
        return moderators;
    }

    @Override
    public void setModerators(List<String> moderators) {
        this.moderators = moderators;
    }

    @Override
    public List<String> getContributors() {
        return contributors;
    }

    @Override
    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    @Override
    public Map<String, List<String>> getRoles() {
        return roles;
    }

    @Override
    public void setRoles(Map<String, List<String>> roles) {
        this.roles = roles;
    }

    @Override
    public List<String> getSecurityResponsibles() {
        return securityResponsibles;
    }

    @Override
    public void setSecurityResponsibles(List<String> securityResponsibles) {
        this.securityResponsibles = securityResponsibles;
    }

    @Override
    public Map<String, String> getLinkedProjects() {
        return linkedProjects;
    }

    @Override
    public void setLinkedProjects(Map<String, String> linkedProjects) {
        this.linkedProjects = linkedProjects;
    }

    @Override
    public Map<String, String> getReleaseIdToUsage() {
        return releaseIdToUsage;
    }

    @Override
    public void setReleaseIdToUsage(Map<String, String> releaseIdToUsage) {
        this.releaseIdToUsage = releaseIdToUsage;
    }

    @Override
    public List<String> getPackageIds() {
        return packageIds;
    }

    @Override
    public void setPackageIds(List<String> packageIds) {
        this.packageIds = packageIds;
    }

    @Override
    public Map<String, String> getExternalIds() {
        return externalIds;
    }

    @Override
    public void setExternalIds(Map<String, String> externalIds) {
        this.externalIds = externalIds;
    }

    @Override
    public Map<String, String> getAdditionalData() {
        return additionalData;
    }

    @Override
    public void setAdditionalData(Map<String, String> additionalData) {
        this.additionalData = additionalData;
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    @Override
    public Map<String, String> getExternalUrls() {
        return externalUrls;
    }

    @Override
    public void setExternalUrls(Map<String, String> externalUrls) {
        this.externalUrls = externalUrls;
    }

    public List<ComponentAPI> getComponents() {
        return components.stream().map(component -> {
            ComponentAPI componentAPI = new ComponentAPI();
            componentAPI.setId(component.getId());
            componentAPI.setName(component.getName());
            // componentAPI.
            return componentAPI;
        }).toList();
    }

    public void setComponents(List<ComponentAPI> components) {
        this.components = components.stream().map(ComponentPG::new).toList();
    }
}
