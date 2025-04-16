package org.eclipse.sw360.rest.resourceserver.releasesOpenAPI;

import org.eclipse.sw360.datahandler.postgres.Release;
import org.springframework.data.repository.support.Repositories;
import java.util.List;
import org.eclipse.sw360.datahandler.db.ComponentRepository;
import org.eclipse.sw360.datahandler.postgresql.ComponentRepositoryPG;
import org.eclipse.sw360.datahandler.postgresql.ReleaseRepositoryPG;
import org.eclipse.sw360.datahandler.resourcelists.PaginationResult;
import org.eclipse.sw360.datahandler.thrift.users.User;

public class ReleaseServicePG {
        private final ReleaseRepositoryPG releaseRepository = new ReleaseRepositoryPG();
        private final ComponentRepositoryPG componentRepository = new ComponentRepositoryPG();

        public Release getReleaseForUserById(String id, User user) {
                Release release = releaseRepository.getReleaseById(id);
                if (release == null) {
                        throw new IllegalArgumentException("Release not found");
                }

                // Release openApiRelease = new Release();
                // openApiRelease.setName(release.getName());
                // openApiRelease.setVersion(release.getVersion());
                // openApiRelease.setId(release.getId());

                return release;
        }

        public Release createRelease(Release release, User user) {
                if (release.getName() == null || release.getName().isEmpty()) {
                        throw new IllegalArgumentException("Release name is required");
                }

                org.eclipse.sw360.datahandler.postgres.Release internalRelease =
                                new org.eclipse.sw360.datahandler.postgres.Release();
                internalRelease.setName(release.getName());
                internalRelease.setVersion(release.getVersion());
                if (release.getComponentId() != null) {
                        org.eclipse.sw360.datahandler.postgres.Component component =
                                        componentRepository
                                                        .getComponentById(release.getComponentId());
                        if (component == null) {
                                throw new IllegalArgumentException("Component not found");
                        }
                        internalRelease.setComponent(component);
                }

                org.eclipse.sw360.datahandler.postgres.Release savedRelease =
                                releaseRepository.saveRelease(internalRelease);

                Release createdRelease = new Release();
                createdRelease.setName(savedRelease.getName());
                createdRelease.setVersion(savedRelease.getVersion());
                createdRelease.setId(savedRelease.getId());
                createdRelease.setComponentId(savedRelease.getComponent() != null
                                ? savedRelease.getComponent().getId().toString()
                                : null);

                return createdRelease;
        }

        public List<Release> getReleasesByComponentId(String id, User sw360User) {
                List<org.eclipse.sw360.datahandler.postgres.Release> releases =
                                releaseRepository.getReleasesByComponentId(id);
                if (releases == null || releases.isEmpty()) {
                        throw new IllegalArgumentException("No releases found for component");
                }
                List<Release> openApiReleases = releases.stream().map(release -> {
                        Release openApiRelease = new Release(release);
                        // openApiRelease.setName(release.getName());
                        // openApiRelease.setVersion(release.getVersion());
                        // openApiRelease.setId(release.getId());
                        return openApiRelease;
                }).toList();

                return openApiReleases;
        }

        public PaginationResult<Release> getAllReleases(User sw360User, int page, int size) {
                PaginationResult<org.eclipse.sw360.datahandler.postgres.Release> releases =
                                releaseRepository.getAllReleases(page, size);
                if (releases == null || releases.getResources().isEmpty()) {
                        throw new IllegalArgumentException("No releases found");
                }
                List<Release> openApiReleases = releases.getResources().stream().map(release -> {
                        Release openApiRelease = new Release();
                        openApiRelease.setName(release.getName());
                        openApiRelease.setVersion(release.getVersion());
                        openApiRelease.setId(release.getId());
                        return openApiRelease;
                }).toList();

                return new PaginationResult<>(openApiReleases);
        }

        public PaginationResult<Release> getReleasesForUser(User sw360User, int page, int size) {
                PaginationResult<org.eclipse.sw360.datahandler.postgres.Release> releases =
                                releaseRepository.getAllReleases(page, size);
                if (releases == null || releases.getResources().isEmpty()) {
                        throw new IllegalArgumentException("No releases found");
                }
                List<Release> openApiReleases = releases.getResources().stream().map(release -> {
                        Release openApiRelease = new Release();
                        openApiRelease.setName(release.getName());
                        openApiRelease.setVersion(release.getVersion());
                        openApiRelease.setId(release.getId());
                        return openApiRelease;
                }).toList();

                return new PaginationResult<>(openApiReleases);
        }
}
