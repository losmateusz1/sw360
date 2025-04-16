package org.eclipse.sw360.datahandler.postgresql;

import java.util.UUID;
import org.eclipse.sw360.datahandler.postgres.VendorPG;
import org.hibernate.HibernateException;
import org.hibernate.Session;
// import jakarta.validation.Valid;

public class VendorRepositoryPG {

    public VendorPG saveVendor(VendorPG vendor) throws HibernateException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        VendorPG savedVendor = session.merge(vendor);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        session.close();
        return savedVendor;
    }

    public VendorPG getVendorByFullName(String fullname) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        VendorPG vendor =
                session.createQuery("FROM VendorPG WHERE fullname = :fullname", VendorPG.class)
                        .setParameter("fullname", fullname).uniqueResult();
        session.getTransaction().commit();
        session.close();
        return vendor;
    }

    public VendorPG getVendorById(UUID id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        VendorPG vendor = session.get(VendorPG.class, id);
        session.getTransaction().commit();
        session.close();
        return vendor;
    }

    public VendorPG updateVendor(VendorPG existingVendor) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        VendorPG updatedVendor = session.merge(existingVendor);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        session.close();
        return updatedVendor;
    }

    public void deleteVendor(VendorPG vendor) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.remove(vendor);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        session.close();
    }
}
