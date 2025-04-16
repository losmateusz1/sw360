package org.eclipse.sw360.datahandler.postgres;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.UUID;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(value = Include.NON_EMPTY, content = Include.NON_NULL)
@Entity
@Table
@Transactional
public class Release extends org.eclipse.sw360.datahandler.componentsApi.model.Release
        implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = true)
    private String version;

    // @Column(nullable = true)
    // private String description;

    @Column(nullable = true)
    private String createdBy;

    @Column(nullable = true)
    private Date createdOn;

    @Column(nullable = true)
    private String modifiedBy;

    @Column(nullable = true)
    private LocalDate modifiedOn;

    @ManyToOne
    @Cascade(CascadeType.ALL)
    @JoinColumn(name = "component_id", referencedColumnName = "id")
    private Component component;

    public Component getComponent() {
        return component;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    // public String getDescription() {
    // return description;
    // }

    // public void setDescription(String description) {
    // this.description = description;
    // }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public LocalDate getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(LocalDate modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String toString() {
        return "Release{" + "id=" + id + ", name='" + name + '\'' + ", version='" + version + '\''
                + ", description='" + ", createdBy='" + createdBy + '\'' + ", createdOn="
                + createdOn + ", modifiedBy='" + modifiedBy + '\'' + ", modifiedOn=" + modifiedOn
                + '}';
    }

    public void setComponent(Component new_Component) {
        this.component = new_Component;
    }

    public Release() {
        super();
    }

    public Release(org.eclipse.sw360.datahandler.componentsApi.model.Release release) {
        this.name = release.getName();
        this.version = release.getVersion();
        this.modifiedBy = release.getModifiedBy();
        this.modifiedOn = release.getModifiedOn();
    }

}
