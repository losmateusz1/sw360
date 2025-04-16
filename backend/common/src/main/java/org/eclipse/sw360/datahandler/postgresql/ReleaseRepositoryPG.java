package org.eclipse.sw360.datahandler.postgresql;

import java.util.List;
import java.util.UUID;
import org.eclipse.sw360.datahandler.postgres.Release;
import org.eclipse.sw360.datahandler.resourcelists.PaginationResult;
import org.hibernate.Session;

public class ReleaseRepositoryPG {

    private final Session session = HibernateUtil.getSessionFactory().openSession();

    public Release saveRelease(Release release) {
        session.beginTransaction();
        Release savedRelease = (Release) session.merge(release);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        return savedRelease;
    }

    public Release getReleaseById(String id) {
        session.beginTransaction();
        Release release = session.get(Release.class, UUID.fromString(id));
        session.getTransaction().commit();
        return release;
    }

    public List<Release> getReleasesByComponentId(String id) {
        session.beginTransaction();
        List<Release> releases =
                session.createQuery("FROM Release WHERE component.id = :id", Release.class)
                        .setParameter("id", UUID.fromString(id)).getResultList();
        session.getTransaction().commit();

        return releases;
    }

    public void deleteRelease(Release release) {
        session.beginTransaction();
        session.remove(release);
        session.flush();
        session.getTransaction().commit();
        session.clear();
    }

    public PaginationResult<Release> getAllReleases(int page, int size) {
        session.beginTransaction();
        List<Release> releases = session.createQuery("FROM Release", Release.class)
                .setFirstResult(page * size).setMaxResults(size).getResultList();

        session.flush();
        session.getTransaction().commit();

        return new PaginationResult<>(releases);
    }
}
