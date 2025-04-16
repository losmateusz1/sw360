package org.eclipse.sw360.datahandler.postgres;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "licenses")
public class License extends org.eclipse.sw360.datahandler.componentsApi.model.License
        implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "revision")
    private String revision;

    @Column(name = "type")
    private String type = "license";

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "fullname")
    private String fullname;

    @Column(name = "license_type")
    private String licenseType;

    @Column(name = "license_type_database_id")
    private String licenseTypeDatabaseId;

    @Column(name = "external_license_link")
    private String externalLicenseLink;

    @Column(name = "note")
    private String note;

    @ElementCollection
    @CollectionTable(name = "license_external_ids", joinColumns = @JoinColumn(name = "license_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> externalIds = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "license_additional_data",
            joinColumns = @JoinColumn(name = "license_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, String> additionalData = new HashMap<>();

    @Column(name = "review_date")
    private String reviewdate;

    @Column(name = "osi_approved")
    private Integer osIApproved;

    @Column(name = "fsf_libre")
    private Integer fsFLibre;

    @ElementCollection
    @CollectionTable(name = "license_obligations", joinColumns = @JoinColumn(name = "license_id"))
    @Column(name = "obligation")
    private List<String> obligations = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "license_obligation_database_ids",
            joinColumns = @JoinColumn(name = "license_id"))
    @Column(name = "obligation_database_id")
    private List<String> obligationDatabaseIds = new ArrayList<>();

    @Column(name = "obligation_list_id")
    private String obligationListId;

    @Column(name = "text")
    private String text;

    @Column(name = "checked")
    private Boolean checked;

    @Column(name = "document_state")
    private String documentState;

    @ElementCollection
    @CollectionTable(name = "license_permissions", joinColumns = @JoinColumn(name = "license_id"))
    @MapKeyColumn(name = "key")
    @Column(name = "value")
    private Map<String, Boolean> permissions = new HashMap<>();

    // Getters and setters for all fields
    // ...existing code...
}
