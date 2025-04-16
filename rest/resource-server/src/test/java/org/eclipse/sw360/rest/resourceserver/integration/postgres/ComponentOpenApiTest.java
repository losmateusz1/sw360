package org.eclipse.sw360.rest.resourceserver.integration.postgres;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ReleaseAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI.ClearingStateEnum;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI.ProjectTypeEnum;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI.StateEnum;
import org.eclipse.sw360.datahandler.db.ProjectRepository;
import org.eclipse.sw360.datahandler.postgres.ComponentPG;
// import org.eclipse.sw360.datahandler.postgres.ComponentRelations;
import org.eclipse.sw360.datahandler.postgres.ProjectPG;
import org.eclipse.sw360.datahandler.postgres.ReleasePG;
import org.eclipse.sw360.datahandler.postgres.VendorPG;
import org.eclipse.sw360.datahandler.postgres.VulnerabilityPG;
import org.eclipse.sw360.datahandler.postgres.VulnerabilityReleaseRelationPG;
import org.eclipse.sw360.datahandler.postgresql.ProjectRepositoryPG;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.components.Release;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.Sw360ResourceServer;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.security.basic.Sw360CustomUserDetailsService;
import org.eclipse.sw360.rest.resourceserver.security.basic.Sw360GrantedAuthority;
import org.eclipse.sw360.rest.resourceserver.vendorOpenAPI.VendorServicePG;
import org.eclipse.sw360.rest.resourceserver.vulnerabilityOpenAPI.VulnerabilityServicePG;
// import org.eclipse.sw360.testthrift.TestService.AsyncProcessor.test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.JsonNode;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.eclipse.sw360.rest.resourceserver.componentOpenApi.ComponentServicePG;
import org.eclipse.sw360.rest.resourceserver.core.RestControllerHelper;
import org.eclipse.sw360.rest.resourceserver.integration.TestIntegrationBase;
import org.eclipse.sw360.rest.resourceserver.projectOpenAPI.ProjectServicePG;
import org.eclipse.sw360.rest.resourceserver.releasesOpenAPI.ReleaseServicePG;
import org.cyclonedx.model.Hash;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.eclipse.sw360.datahandler.componentsApi.model.ComponentAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.VendorAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.VulnerabilityAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ComponentAPI.VisibilityEnum;

@RunWith(SpringRunner.class)
public class ComponentOpenApiTest extends TestIntegrationBase {

        @Value("${local.server.port}")
        private int port;

        private ComponentServicePG componentServicePGmock = new ComponentServicePG();

        private ProjectServicePG projectServicePGmock = new ProjectServicePG();

        private VendorServicePG vendorServicePGmock = new VendorServicePG();

        private ReleaseServicePG releaseServicePGmock = new ReleaseServicePG();

        private VulnerabilityServicePG vulnerabilityServicePGmock = new VulnerabilityServicePG();

        private TestRestTemplate testRestTemplate = new TestRestTemplate("admin@sw360.org", "12345",
                        TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

        // @Test
        // public void should_get_components_by_name() throws IOException {
        // String url = "http://localhost:" + port + "/componentsOpenAPI?name=Component name";
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // headers.setBasicAuth("admin@sw360.org", "12345");
        // HttpEntity<String> entity = new HttpEntity<>(headers);

        // User user = new User();
        // user.setEmail("admin@sw360.org");
        // user.setDepartment("Department");

        // ComponentPG component = new ComponentPG();
        // component.setName("Component name");
        // component.setDescription("Component description");
        // componentServicePGmock.createComponent(component, user);

        // ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET,
        // entity, String.class);

        // assertEquals(HttpStatus.OK, response.getStatusCode());

        // JsonNode responseNode = new ObjectMapper().readTree(response.getBody());

        // assertEquals("Component name", responseNode.get("_embedded")
        // .get("sw360:componentPGs").get(0).get("name").asText());
        // }

        @Test
        public void should_create_valid_component() throws IOException {
                String url = "http://localhost:" + port + "/componentsOpenAPI";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ComponentAPI component = new ComponentAPI();
                component.setName("Component name");
                component.setDescription("Component description");
                component.setVisibility(VisibilityEnum.PUBLIC);
                component.setBlog("Blog");
                component.setHomepage("https://example.com");

                VendorPG vendor = new VendorPG();
                vendor.setFullname("vendor1");
                vendor.setShortname("Vendor shortname");
                vendor.setRevision("1.0");

                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");
                VendorPG vendor_result = vendorServicePGmock.createVendor(vendor, user);

                component.setVendors(List.of(vendor_result.ToVendorAPI()));

                System.out.println("DEBUG COMPONENT: " + component);
                ObjectMapper objectMapper = new ObjectMapper();
                String componentJson = objectMapper.writeValueAsString(component);

                HttpEntity<String> entity = new HttpEntity<>(componentJson, headers);

                ResponseEntity<ComponentPG> response =
                                testRestTemplate.postForEntity(url, entity, ComponentPG.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
                // System.out.println("Response debug: " + response.getBody());
                // System.out.println("Response debug: " + response.getStatusCode());

                VendorPG testvendor = new VendorPG();
                testvendor.setFullname("testvendor1");
                testvendor.setShortname("testVendor shortname");
                testvendor.setRevision("1.0");

                VendorPG testvendor_result = vendorServicePGmock.createVendor(testvendor, user);
                assertEquals("testvendor1", testvendor_result.getFullname());
                assertTrue(testvendor_result.getId() != null);

                ComponentPG createdComponent = response.getBody();
                assert createdComponent != null;
                // ComponentRelations componentRelations = new ComponentRelations();
                // componentRelations.setComponent(createdComponent);
                // componentRelations.setVendor(testvendor_result);

                createdComponent.setName("Updated component name");
                createdComponent.setDescription("Updated component description");
                createdComponent.setVendors(List.of(testvendor_result));
                ComponentPG updatedcomponent = componentServicePGmock.updateComponent(
                                createdComponent.getId().toString(), createdComponent, user);
                System.out.println("Updated component: " + updatedcomponent);

                System.out.println(
                                "Vendors of updated component: " + updatedcomponent.getVendors());
                assertTrue(updatedcomponent.getVendors() != null);
                assertTrue(updatedcomponent.getVendors().size() > 0);

        }

        @Test
        public void should_create_component_release() throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(
                                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);

                ComponentPG tmpComponent = new ComponentPG();
                tmpComponent.setName("TMPComponent");
                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");

                ComponentPG component = componentServicePGmock.createComponent(tmpComponent, user);

                String releaseUrl = "http://localhost:" + port + "/releasesOpenAPI";
                ReleasePG release = new ReleasePG();
                release.setName("Release name");
                release.setVersion("1.0.0");
                release.setComponentId(component.getId().toString());

                HttpEntity<String> releaseEntity =
                                new HttpEntity<>(objectMapper.writeValueAsString(release), headers);

                ResponseEntity<EntityModel<ReleasePG>> releaseResponse =
                                testRestTemplate.postForEntity(releaseUrl, releaseEntity, null);
                // assertEquals(HttpStatus.CREATED, releaseResponse.getStatusCode());
                System.out.println("Release response DEB: " + releaseResponse.getBody());

        }

        @Test
        public void should_get_vendors() throws IOException {
                String url = "http://localhost:" + port + "/vendorsOpenAPI";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET,
                                entity, String.class);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        public void should_create_project() throws IOException {

                String url = "http://localhost:" + port + "/projectsOpenAPI";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");

                ProjectAPI project = new ProjectAPI();
                project.setName("Project name");
                project.setDescription("Project description");
                project.setVersion("1.0.0");
                project.setState(StateEnum.ACTIVE);
                project.setProjectType(ProjectTypeEnum.INTERNAL);
                project.setClearingState(ClearingStateEnum.OPEN);

                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");

                ObjectMapper objectMapper = new ObjectMapper();
                String projectJson = objectMapper.writeValueAsString(project);
                HttpEntity<String> entity = new HttpEntity<>(projectJson, headers);

                ResponseEntity<String> response =
                                testRestTemplate.postForEntity(url, entity, String.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        public void should_update_project() throws IOException {

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");

                // create test project
                ProjectPG project = new ProjectPG();
                project.setName("Project name");
                project.setDescription("Project description");
                project.setVersion("1.0.0");
                project.setState(StateEnum.ACTIVE);
                project.setProjectType(ProjectTypeEnum.INTERNAL);

                projectServicePGmock.createProject(project, null);

                ProjectPG updatedProject = projectServicePGmock.getRecentProjects(1).get(0);
                updatedProject.setName("Updated Project name");
                updatedProject.setDescription("Updated Project description");

                String url = "http://localhost:" + port + "/projectsOpenAPI/"
                                + updatedProject.getId();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                String projectJson = objectMapper.writeValueAsString(updatedProject);

                HttpEntity<String> entity = new HttpEntity<>(projectJson, headers);
                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.PATCH,
                                entity, String.class);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        public void should_link_component_to_project() throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());

                ProjectPG project = projectServicePGmock.getRecentProjects(1).get(0);
                ComponentPG tmpComponent = new ComponentPG();
                tmpComponent.setName("TMPComponent");
                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");

                ComponentPG component = componentServicePGmock.createComponent(tmpComponent, user);

                // String releaseUrl = "http://localhost:" + port + "/releasesOpenAPI";
                ReleasePG release = new ReleasePG();
                release.setName("Release name");
                release.setVersion("1.0.0");
                release.setComponentId(component.getId().toString());

                ReleasePG releasePG = releaseServicePGmock.createRelease(release, null);

                VulnerabilityPG vulnerabilityPG = new VulnerabilityPG();
                vulnerabilityPG.setDescription("Vulnerability description");
                vulnerabilityPG.setExternalId("Vulnerability external id");

                VulnerabilityReleaseRelationPG vulnerabilityReleaseRelationPG =
                                new VulnerabilityReleaseRelationPG();
                vulnerabilityReleaseRelationPG.setVulnerability(vulnerabilityPG);
                vulnerabilityReleaseRelationPG.setRelease(releasePG);
                vulnerabilityPG.setReleasesRelation(Set.of(vulnerabilityReleaseRelationPG));

                vulnerabilityServicePGmock.createVulnerability(vulnerabilityPG);

                String url = "http://localhost:" + port + "/projectsOpenAPI/" + project.getId()
                                + "/link/components";

                String componentJson = objectMapper.writeValueAsString(List.of(component));
                HttpEntity<String> entity = new HttpEntity<>(componentJson, headers);
                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.PATCH,
                                entity, String.class);

                assertEquals(HttpStatus.OK, response.getStatusCode());
                ProjectPG updatedProject = projectServicePGmock
                                .getProjectForUserById(project.getId().toString(), null);
                assertEquals(1, updatedProject.getComponents().size());
        }

        @Test
        public void should_create_vulnerability() throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(
                                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);

                String url = "http://localhost:" + port + "/vulnerabilitiesOpenAPI";
                VulnerabilityAPI vulnerability = new VulnerabilityAPI();
                vulnerability.setDescription("Vulnerability description");
                vulnerability.setExternalId("Vulnerability external id");

                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.POST,
                                new HttpEntity<>(objectMapper.writeValueAsString(vulnerability),
                                                headers),
                                String.class);
                assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        public void should_create_vulnerability2() throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(
                                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);

                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");

                VendorPG vendor = new VendorPG();
                vendor.setFullname("vendor name");
                vendor.setShortname("vendor shortname");
                vendor.setRevision("1.0");
                VendorPG vendorPG = vendorServicePGmock.createVendor(vendor, user);

                ComponentAPI component = new ComponentAPI();
                component.setName("Component name");
                component.setDescription("Component description");
                component.setVisibility(VisibilityEnum.PUBLIC);
                component.setVendors(List.of(vendorPG.ToVendorAPI()));

                ComponentPG componentPG = componentServicePGmock.createComponent(component, user);

                ReleasePG release = new ReleasePG();
                release.setName("Release name");
                release.setVersion("1.0.0");
                release.setComponentId(componentPG.getId().toString());
                ReleasePG releasePG = releaseServicePGmock.createRelease(release, user);


                String url = "http://localhost:" + port + "/vulnerabilitiesOpenAPI";
                VulnerabilityAPI vulnerability = new VulnerabilityAPI();
                vulnerability.setDescription("Vulnerability description");
                vulnerability.setExternalId("Vulnerability external id");
                vulnerability.setReleases(List.of(releasePG));

                VulnerabilityPG vulnerabilityPG =
                                vulnerabilityServicePGmock.createVulnerability(vulnerability);
                System.out.println("Created vulnerability with relations: "
                                + vulnerabilityPG.getReleasesRelation());
                assertEquals("Vulnerability description", vulnerabilityPG.getDescription());
        }

        @Test
        public void should_get_vulnerabilities() throws IOException {
                String url = "http://localhost:" + port + "/vulnerabilitiesOpenAPI";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET,
                                entity, String.class);
                assertEquals(HttpStatus.OK, response.getStatusCode());
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.configure(
                                com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                                false);
                // System.out.println("Response body: " + response.getBody());
                // JsonNode responseNode = objectMapper.readTree(response.getBody());
                // System.out.println("Response: " + responseNode);
        }

        @Test
        public void create_invalid_component() throws IOException {
                String url = "http://localhost:" + port + "/componentsOpenAPI";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");

                ComponentAPI component = new ComponentAPI();
                component.setDescription("Component description");

                ObjectMapper objectMapper = new ObjectMapper();
                String componentJson = objectMapper.writeValueAsString(component);
                HttpEntity<String> entity = new HttpEntity<>(componentJson, headers);

                ResponseEntity<EntityModel<ProjectPG>> response =
                                testRestTemplate.postForEntity(url, entity, null);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        }

        @Test
        public void should_create_invalid_project() throws IOException {
                String url = "http://localhost:" + port + "/projectsOpenAPI";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ProjectAPI project = new ProjectAPI();
                // project.setName("Project name");
                project.setDescription("Project description");
                project.setVersion("1.0.0");

                ObjectMapper objectMapper = new ObjectMapper();
                String projectJson = objectMapper.writeValueAsString(project);
                HttpEntity<String> entity = new HttpEntity<>(projectJson, headers);

                ResponseEntity<EntityModel<ProjectPG>> response =
                                testRestTemplate.postForEntity(url, entity, null);

                assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        }

        @Test
        public void should_delete_component() throws IOException {
                ComponentPG component = componentServicePGmock.getRecentComponents(1).get(0);

                String url = "http://localhost:" + port + "/componentsOpenAPI/" + component.getId();
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");

                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.DELETE,
                                entity, String.class);

                TestHelper.handleBatchDeleteResourcesResponse(response,
                                component.getId().toString(), 200);

        }

        @Test
        public void should_delete_project() throws IOException {
                ProjectPG project = projectServicePGmock.getRecentProjects(1).get(0);

                String url = "http://localhost:" + port + "/projectsOpenAPI/"
                                + project.getId().toString();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                HttpEntity<String> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.DELETE,
                                entity, String.class);
                assertEquals(HttpStatus.OK, response.getStatusCode());
        }

}
