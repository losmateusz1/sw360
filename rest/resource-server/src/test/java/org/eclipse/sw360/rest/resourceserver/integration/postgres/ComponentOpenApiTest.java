package org.eclipse.sw360.rest.resourceserver.integration.postgres;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.List;
import org.junit.Test;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ReleaseAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI.ClearingStateEnum;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI.ProjectTypeEnum;
import org.eclipse.sw360.datahandler.componentsApi.model.ProjectAPI.StateEnum;
import org.eclipse.sw360.datahandler.db.ProjectRepository;
import org.eclipse.sw360.datahandler.postgres.ComponentPG;
import org.eclipse.sw360.datahandler.postgres.ProjectPG;
import org.eclipse.sw360.datahandler.postgres.VendorPG;
import org.eclipse.sw360.datahandler.postgresql.ProjectRepositoryPG;
import org.eclipse.sw360.datahandler.thrift.RequestStatus;
import org.eclipse.sw360.datahandler.thrift.users.User;
import org.eclipse.sw360.rest.resourceserver.Sw360ResourceServer;
import org.eclipse.sw360.rest.resourceserver.TestHelper;
import org.eclipse.sw360.rest.resourceserver.security.basic.Sw360GrantedAuthority;
import org.eclipse.sw360.rest.resourceserver.vendorOpenAPI.VendorServicePG;
// import org.eclipse.sw360.testthrift.TestService.AsyncProcessor.test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
import org.eclipse.sw360.datahandler.componentsApi.model.ComponentAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.VendorAPI;
import org.eclipse.sw360.datahandler.componentsApi.model.ComponentAPI.VisibilityEnum;

@RunWith(SpringRunner.class)
public class ComponentOpenApiTest extends TestIntegrationBase {

        @Value("${local.server.port}")
        private int port;

        private ComponentServicePG componentServicePGmock = new ComponentServicePG();

        private ProjectServicePG projectServicePGmock = new ProjectServicePG();

        private VendorServicePG vendorServicePGmock = new VendorServicePG();

        // @MockBean
        // RestControllerHelper restControllerHelper;

        private TestRestTemplate testRestTemplate = new TestRestTemplate("admin@sw360.org", "12345",
                        TestRestTemplate.HttpClientOption.ENABLE_COOKIES);

        @Test
        public void should_get_components_by_name() throws IOException {
                String url = "http://localhost:" + port + "/componentsOpenAPI?name=Component name";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                HttpEntity<String> entity = new HttpEntity<>(headers);

                // User user = new User();
                // user.setEmail("admin@sw360.org");
                // user.setDepartment("Department");

                // when(restControllerHelper.getSw360UserFromAuthentication()).thenReturn(user);

                ResponseEntity<String> response = testRestTemplate.exchange(url, HttpMethod.GET,
                                entity, String.class);



                assertEquals(HttpStatus.OK, response.getStatusCode());

                JsonNode responseNode = new ObjectMapper().readTree(response.getBody());

                assertEquals("Component name", responseNode.get("_embedded")
                                .get("sw360:componentPGs").get(0).get("name").asText());
        }

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
                vendor.setFullname("Vendor1");
                vendor.setShortname("Vendor shortname");
                vendor.setRevision("1.0");

                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");
                VendorPG vendor_result = vendorServicePGmock.createVendor(vendor, user);
                assertEquals("Vendor1", vendor_result.getFullname());

                component.setVendorNames(List.of(vendor.getFullname()));
                component.setVendors(List.of(vendor));
                component.setDefaultVendor(vendor);

                ObjectMapper objectMapper = new ObjectMapper();
                String componentJson = objectMapper.writeValueAsString(component);

                HttpEntity<String> entity = new HttpEntity<>(componentJson, headers);



                ResponseEntity<ComponentPG> response =
                                testRestTemplate.postForEntity(url, entity, ComponentPG.class);

                assertEquals(HttpStatus.CREATED, response.getStatusCode());
        }

        @Test
        public void should_create_component_release() throws IOException {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.setBasicAuth("admin@sw360.org", "12345");
                ObjectMapper objectMapper = new ObjectMapper();

                ComponentPG tmpComponent = new ComponentPG();
                tmpComponent.setName("TMPComponent");
                User user = new User();
                user.setEmail("admin@sw360.org");
                user.setDepartment("Department");

                componentServicePGmock.createComponent(tmpComponent, user);

                ComponentPG component = componentServicePGmock.getRecentComponents(1).get(0);

                String releaseUrl = "http://localhost:" + port + "/releasesOpenAPI";
                ReleaseAPI release = new ReleaseAPI();
                release.setName("Release name");
                release.setVersion("1.0.0");
                release.setComponentId(component.getId().toString());
                HttpEntity<String> releaseEntity =
                                new HttpEntity<>(objectMapper.writeValueAsString(release), headers);

                ResponseEntity<String> releaseResponse = testRestTemplate.exchange(releaseUrl,
                                HttpMethod.POST, releaseEntity, String.class);
                assertEquals(HttpStatus.CREATED, releaseResponse.getStatusCode());
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

                // when(restControllerHelper.getSw360UserFromAuthentication()).thenReturn(user);

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
                ProjectAPI project = new ProjectAPI();
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
                ComponentAPI componentAPI = new ComponentAPI();
                componentAPI.setName("Component to link");
                componentAPI.setDescription("Component to link description");
                ComponentPG component = componentServicePGmock.createComponent(componentAPI, null);

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

                ResponseEntity<String> response =
                                testRestTemplate.postForEntity(url, entity, String.class);

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

                ResponseEntity<String> response =
                                testRestTemplate.postForEntity(url, entity, String.class);

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
