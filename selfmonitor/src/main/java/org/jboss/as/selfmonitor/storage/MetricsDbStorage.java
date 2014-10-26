package org.jboss.as.selfmonitor.storage;

import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.jboss.as.selfmonitor.entity.Metric;
import org.jboss.logging.Logger;

/**
 *
 * @author Vojtech Schlemmer
 */
public class MetricsDbStorage implements IMetricsStorage {

    private final Logger log = Logger.getLogger(MetricsDbStorage.class);
    private EntityManager entityManager;
    public static final String PERSISTENCE_UNIT_NAME = "SelfmonitorPU";
    
    public MetricsDbStorage(){
        this.entityManager = createEntityManagerFactory().createEntityManager();
    }
    
    public void initDatabase(){
        EntityManagerFactory emf = createEntityManagerFactory();
        if(emf != null){
            EntityManager em = emf.createEntityManager();
            Metric m = new Metric("testMetric1", "testMetric1", new Date(System.currentTimeMillis()), null);
            em.getTransaction().begin();
            em.persist(m);
            em.getTransaction().commit();
            String queryString = "SELECT m FROM Metric m WHERE m.name = 'testMetric2'";
            Query q = em.createQuery(queryString, Metric.class);
            List<Metric> metrics = q.getResultList();
            if(metrics.size() > 0){
                log.info("----------------------------");
                log.info("metric name: " + metrics.get(0).getName());
                log.info("----------------------------");
            }
            em.close();
        }
        else{
            log.info("----------------------------");
            log.info("emf is null!! ");
            log.info("----------------------------");
        }
    }
    
    private EntityManagerFactory createEntityManagerFactory(){	
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public void addMetric(String metricName, String metricPath, Date date, Object value) {
        Metric metric = new Metric(metricName, metricPath, date, (String) value);
        entityManager.getTransaction().begin();
        entityManager.persist(metric);
        entityManager.getTransaction().commit();
    }

    @Override
    public Map<Date, Object> getMetricRecords(String metricName, String metricPath) {
        Map<Date, Object> metricRecords = new HashMap<>();
        for (Metric m : retrieveMetricRecords(metricName, metricPath)){
            metricRecords.put(m.getDate(), m.getValue());
        }
        return metricRecords;
    }
    
    private List<Metric> retrieveMetricRecords(String metricName, String metricPath){
        String queryString = "SELECT m FROM Metric m WHERE m.name = '" +
                metricName + "' AND m.path = '" + metricPath + "'";
        Query q = entityManager.createQuery(queryString, Metric.class);
        List<Metric> metrics = q.getResultList();
        return metrics;
    }
    
}
