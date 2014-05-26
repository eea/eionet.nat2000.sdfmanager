package sdf_manager;


import java.io.FileInputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author charbda
 */
public class HibernateUtil {

    private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HibernateUtil.class .getName());
    /**
     *
     */
    private static final SessionFactory sessionFactory;

    /**
     *
     */
    static {
        try {

            Properties properties = new Properties();
            //properties.load(new FileInputStream(new java.io.File("").getAbsolutePath() + File.separator + "database" + File.separator + "sdf_database.properties"));
            properties.load(new FileInputStream(SDF_ManagerApp.LOCAL_PROPERTIES_FILE));
            String dbSchemaName = SDF_ManagerApp.isEmeraldMode() ? "emerald" : "natura2000";

            AnnotationConfiguration annotationConfig = new AnnotationConfiguration();
            annotationConfig.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            annotationConfig.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
            annotationConfig.setProperty("hibernate.connection.url", "jdbc:mysql://" + properties.getProperty("db.host")
                    + ":" +  properties.getProperty("db.port")
                    + "/" + dbSchemaName + "?autoReconnect=true");
            annotationConfig.setProperty("hibernate.connection.username", properties.getProperty("db.user"));
            annotationConfig.setProperty("hibernate.connection.password", properties.getProperty("db.password"));
            annotationConfig.setProperty("hibernate.transaction.factory_class", "org.hibernate.transaction.JDBCTransactionFactory");


            //sessionFactory = annotationConfig.configure().buildSessionFactory();
            sessionFactory = annotationConfig.configure().buildSessionFactory();

        } catch (Throwable ex) {
            // Log the exception.
            System.err.println("Initial SessionFactory creation failed." + ex);
            HibernateUtil.log.error("Initial SessionFactory creation failed." + ex.getMessage());
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     *
     * @return
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static /**
     *
     * @param fileName
     */
    void openSession(String fileName) {
            Configuration cfg = new Configuration();
            cfg.configure();
            cfg.setProperty("hibernate.dialect", "sdf_manager.MSAccessDialect");
            cfg.setProperty("hibernate.connection.driver_class", "sun.jdbc.odbc.JdbcOdbcDriver");
            cfg.setProperty("hibernate.connection.url", "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + fileName + "");
            cfg.setProperty("hibernate.connection.username", "");
    }
}
