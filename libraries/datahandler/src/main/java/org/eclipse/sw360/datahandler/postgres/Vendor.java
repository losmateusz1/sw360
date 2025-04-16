package org.eclipse.sw360.datahandler.postgres;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.hibernate.annotations.Cascade;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@Entity
@Table
@Transactional
public class Vendor extends org.eclipse.sw360.datahandler.componentsApi.model.Vendor
        implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @Column(name = "revision")
    private String revision;

    @Column(name = "type")
    private String type = "vendor";

    @Column(name = "shortname")
    private String shortname;

    @Column(name = "fullname")
    private String fullname;

    @ElementCollection
    @CollectionTable(name = "vendor_permissions", joinColumns = @JoinColumn(name = "vendor_id"))
    @MapKeyColumn(name = "permission_key")
    @Column(name = "permission_value")
    private Map<String, Boolean> permissions = new HashMap<>();

    @ManyToMany(mappedBy = "vendors")
    private Set<Component> components = new HashSet<>();

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
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
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

    public Vendor(String name) {
        this.name = name;
        this.fullname = name;
        this.shortname = name;
    }

    public Vendor() {
        super();
    }

    public Vendor(org.eclipse.sw360.datahandler.componentsApi.model.Vendor vendor) {
        // this.name = vendor.getName();
        this.url = vendor.getUrl();
        this.revision = vendor.getRevision();
        this.type = vendor.getType();
        this.shortname = vendor.getShortname();
        this.fullname = vendor.getFullname();
        this.permissions = vendor.getPermissions();
    }
}
