package bgu.bioinf.rnaDesign.db;

import bgu.bioinf.rnaDesign.Producers.Utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by matan on 17/11/15.
 */
public class DBConnector {
    private static EntityManagerFactory entityManagerFactory = null;

    public static EntityManager getEntityManager() {
        EntityManager em = null;
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            if (entityManagerFactory != null)
                Utils.log("ERROR",true,"DBConnector.getEntityManager  - EMF closed, restarting it");
            entityManagerFactory = Persistence.createEntityManagerFactory("RNADesign");
        }
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            em = entityManagerFactory.createEntityManager();
        }
        return em;
    }

    public static void closeEMF() {
        if (entityManagerFactory != null) {
            try {
                entityManagerFactory.close();
            } catch (Exception ignore) {
            }
            entityManagerFactory = null;
        }
    }
}
