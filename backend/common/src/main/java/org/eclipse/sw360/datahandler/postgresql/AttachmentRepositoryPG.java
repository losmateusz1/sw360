/*
 * Copyright Siemens AG, 2018. Part of the SW360 Portal Project.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License 2.0 which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.sw360.datahandler.postgresql;

import org.eclipse.sw360.datahandler.cloudantclient.DatabaseConnectorCloudant;
import org.eclipse.sw360.datahandler.cloudantclient.DatabaseRepositoryCloudantClient;
import org.eclipse.sw360.datahandler.postgres.Attachment;
import org.hibernate.Session;

import com.ibm.cloud.cloudant.v1.model.DesignDocumentViewsMapReduce;
import com.ibm.cloud.cloudant.v1.model.PostViewOptions;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AttachmentRepositoryPG {
    private static Session session = HibernateUtil.getSessionFactory().openSession();

    public List<Attachment> getAttachments() {
        try {
            List<Attachment> result =
                    session.createNativeQuery("SELECT * FROM attachment", Attachment.class)
                            .getResultList();
            System.out.println("Got attachments");
            System.out.println(result);
            return result;

        } catch (Exception e) {
            System.out.println("Danger! Danger! Danger!");
            e.printStackTrace();
            return null;
        }
    }

}
