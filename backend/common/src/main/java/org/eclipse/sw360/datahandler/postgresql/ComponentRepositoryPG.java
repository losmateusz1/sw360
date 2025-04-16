package org.eclipse.sw360.datahandler.postgresql;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.eclipse.sw360.datahandler.postgres.Component;
import org.eclipse.sw360.datahandler.postgres.Release;
// import org.eclipse.sw360.datahandler.rest.ComponentResult;
import org.hibernate.Session;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

public class ComponentRepositoryPG {

    private final Session session = HibernateUtil.getSessionFactory().openSession();

    public org.eclipse.sw360.datahandler.componentsApi.model.Component saveComponent(
            Component component) {
        session.beginTransaction();
        org.eclipse.sw360.datahandler.componentsApi.model.Component savedComponent =
                (org.eclipse.sw360.datahandler.componentsApi.model.Component) session
                        .merge(component);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        return savedComponent;
    }

    public List<Component> getComponents(Map<String, String> params) {
        String psqlString = "SELECT * FROM Component";
        Set<String> keys = params.keySet();
        if (keys.size() > 0) {
            psqlString += " WHERE ";
            for (String key : keys) {
                if (key.equals("number") == false && key.equals("size") == false) {
                    psqlString += key + " = " + "'" + params.get(key) + "'" + " AND ";
                }
            }
            psqlString = psqlString.substring(0, psqlString.length() - 5);
        }

        try {
            List<Component> result =
                    session.createNativeQuery(psqlString, Component.class).getResultList();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Component getComponentById(String id) {
        try {
            Component result = session.find(Component.class, UUID.fromString(id));
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void deleteComponent(Component component) {
        try {
            session.beginTransaction();
            for (org.eclipse.sw360.datahandler.componentsApi.model.Release release : component
                    .getReleases()) {
                Release dbrelease = session.find(Release.class, release.getId());
                session.remove(dbrelease);
            }
            session.remove(component);
            session.flush();
            session.getTransaction().commit();
            session.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
