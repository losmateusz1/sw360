package org.eclipse.sw360.datahandler.postgresql;

import java.util.UUID;
import org.eclipse.sw360.datahandler.postgres.Vendor;
import org.hibernate.Session;
import jakarta.validation.Valid;

public class VendorRepositoryPG {
    private final Session session = HibernateUtil.getSessionFactory().openSession();

    public Vendor saveVendor(Vendor vendor) {
        session.beginTransaction();
        Vendor savedVendor = (Vendor) session.merge(vendor);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        return savedVendor;
    }

    public Vendor getVendorByFullName(String fullname) {
        session.beginTransaction();
        Vendor vendor = session.createQuery("FROM Vendor WHERE fullname = :fullname", Vendor.class)
                .setParameter("fullname", fullname).uniqueResult();
        session.getTransaction().commit();
        return vendor;
    }

    public Vendor getVendorById(UUID id) {
        session.beginTransaction();
        Vendor vendor = session.get(Vendor.class, id);
        session.getTransaction().commit();
        return vendor;
    }

    public Vendor updateVendor(Vendor existingVendor) {
        session.beginTransaction();
        Vendor updatedVendor = (Vendor) session.merge(existingVendor);
        session.flush();
        session.getTransaction().commit();
        session.clear();
        return updatedVendor;
    }

    public void deleteVendor(Vendor vendor) {
        session.beginTransaction();
        session.remove(vendor);
        session.flush();
        session.getTransaction().commit();
        session.clear();
    }
}
