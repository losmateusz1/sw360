package org.eclipse.sw360.rest.resourceserver.licenseOpenAPI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import org.eclipse.sw360.rest.resourceserver.componentOpenApi.ComponentsOpenAPIController;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.server.RepresentationModelProcessor;

public class LicenseOpenAPIController
        implements RepresentationModelProcessor<RepositoryLinksResource> {

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        resource.add(linkTo(LicenseOpenAPIController.class).slash("api/licensesOpenAPI")
                .withRel("licensesOpenAPI"));
        return resource;
    }

}
