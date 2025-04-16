package org.eclipse.sw360.rest.resourceserver.componentOpenApi;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.sw360.datahandler.postgres.Vendor;
import org.eclipse.sw360.datahandler.db.ReleaseRepository;
import org.eclipse.sw360.datahandler.db.VendorRepository;
import org.eclipse.sw360.datahandler.postgres.Component;
import org.eclipse.sw360.datahandler.postgres.Release;
import org.eclipse.sw360.datahandler.postgresql.ComponentRepositoryPG;
import org.eclipse.sw360.datahandler.postgresql.ReleaseRepositoryPG;
import org.eclipse.sw360.datahandler.postgresql.VendorRepositoryPG;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.springframework.hateoas.EntityModel;

public class ComponentServicePG {
    private ComponentRepositoryPG componentRepository = new ComponentRepositoryPG();

    private ReleaseRepositoryPG releaseRepository = new ReleaseRepositoryPG();

    private VendorRepositoryPG vendorRepository = new VendorRepositoryPG();

    public List<Component> listComponents(Map<String, String> params) {
        return componentRepository.getComponents(params);
    }

    public List<org.eclipse.sw360.datahandler.componentsApi.model.Component> listOAPIComponents(
            Map<String, String> params) {
        List<Component> components = componentRepository.getComponents(params);
        List<org.eclipse.sw360.datahandler.componentsApi.model.Component> openApiComponents =
                components.stream().map(component -> {
                    org.eclipse.sw360.datahandler.componentsApi.model.Component openApiComponent =
                            new org.eclipse.sw360.datahandler.componentsApi.model.Component();
                    openApiComponent.setName(component.getName());
                    openApiComponent.setId(component.getId());
                    if (component.getReleases() == null) {
                        System.err.println("Component has no releases");
                    } else {
                        System.err.println("Component has releases");
                        System.out.println("Component releases: " + component.getReleases());
                    }

                    openApiComponent.setReleases(component.getReleases());
                    return openApiComponent;
                }).toList();

        System.out.println("Components: " + openApiComponents);
        return openApiComponents;
    }

    public org.eclipse.sw360.datahandler.postgres.Component getComponentById(String id) {
        return componentRepository.getComponentById(id);
    }

    public org.eclipse.sw360.datahandler.componentsApi.model.Component createComponent(
            org.eclipse.sw360.datahandler.componentsApi.model.Component component, User user) {

        if (component.getName() == null || component.getName().isEmpty()) {
            throw new IllegalArgumentException("Component name is required");
        }

        Component internalComponent = new Component(component);
        if (component.getDefaultVendor() != null) {
            Vendor defaultVendor = vendorRepository
                    .getVendorByFullName(component.getDefaultVendor().getFullname());
            internalComponent.setDefaultVendor(defaultVendor);
        }

        if (component.getReleases() == null) {
            internalComponent.setReleases(List.of());
        } else {
            internalComponent.setReleases(component.getReleases());
        }

        org.eclipse.sw360.datahandler.componentsApi.model.Component savedComponent =
                componentRepository.saveComponent(internalComponent);

        return savedComponent;
    }

    public List<EntityModel<Component>> getUsedByResources(String id) {
        return List.of();
    }

    public List<Component> getRecentComponents(int count) {
        return componentRepository.getComponents(Map.of()).stream().limit(count).toList();
    }

    public List<Component> getSubscribedComponents(User user) {
        return List.of();
    }

    public List<Component> getComponentsByExternalIds(Map<String, String> externalIds) {
        return componentRepository.getComponents(externalIds);
    }

    public RequestStatus deleteComponent(String id, User user) {
        Component component = componentRepository.getComponentById(id);

        if (component != null) {
            componentRepository.deleteComponent(component);
            return RequestStatus.SUCCESS;
        } else {
            return RequestStatus.FAILURE;
        }
    }

}
