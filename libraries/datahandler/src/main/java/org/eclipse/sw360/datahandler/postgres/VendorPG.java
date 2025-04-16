package org.eclipse.sw360.datahandler.postgres;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.sw360.datahandler.componentsApi.model.VendorAPI;
import org.hibernate.annotations.Cascade;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@Entity
@Table(name = "vendor")
@Transactional
public class VendorPG extends VendorAPI implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "revision")
    private String revision;

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "fullname")
    private String fullname;

    // @ElementCollection
    // @CollectionTable(name = "vendor_permissions", joinColumns = @JoinColumn(name = "vendor_id"))
    // @MapKeyColumn(name = "permission_key")
    // @Column(name = "permission_value")
    @JsonProperty("permissions")
    private Map<String, Boolean> permissions = new HashMap<>();

    @ManyToMany(mappedBy = "vendors")
    private Set<ComponentPG> components = new HashSet<>();

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String getRevision() {
        return revision;
    }

    @Override
    public void setRevision(String revision) {
        this.revision = revision;
    }

    @Override
    public String getShortname() {
        return shortname;
    }

    @Override
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
    public String getFullname() {
        return fullname;
    }

    @Override
    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Override
    public Map<String, Boolean> getPermissions() {
        return permissions;
    }

    @Override
    public void setPermissions(Map<String, Boolean> permissions) {
        this.permissions = permissions;
    }

    public VendorPG(String name) {
        this.name = name;
        this.fullname = name;
        this.shortname = name;
    }

    public VendorPG() {
        super();
    }

    public VendorPG(org.eclipse.sw360.datahandler.componentsApi.model.VendorAPI vendor) {
        this.url = vendor.getUrl();
        this.revision = vendor.getRevision();
        this.shortname = vendor.getShortname();
        this.fullname = vendor.getFullname();
        this.permissions = vendor.getPermissions();
    }
}
