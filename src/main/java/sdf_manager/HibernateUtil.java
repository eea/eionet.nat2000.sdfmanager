package sdf_manager;


import java.io.FileInputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Hibernate Utility class with a convenient method to get Session Factory object.
 *
 * @author charbda
 */
public class HibernateUtil {

    private final static org.apache.logging.log4j.Logger log = org.apache.logging.log4j.LogManager.getLogger(HibernateUtil.class .getName());
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

            StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder()
                    .applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect")
                    .applySetting("hibernate.hikari.dataSourceClassName", "com.mysql.cj.jdbc.MysqlDataSource")
                    .applySetting("hibernate.hikari.dataSource.url", "jdbc:mysql://" + properties.getProperty("db.host")
                    + ":" +  properties.getProperty("db.port")
                    + "/" + dbSchemaName + "?autoReconnect=true")
                    .applySetting("hibernate.hikari.dataSource.user", properties.getProperty("db.user"))
                    .applySetting("hibernate.hikari.dataSource.password", properties.getProperty("db.password"))
                    .applySetting("hibernate.connection.release_mode", "after_transaction")
                    .configure("hibernate.cfg.xml").build();
            Metadata metadata = new MetadataSources(standardRegistry)
                    .getMetadataBuilder().build();
            sessionFactory = metadata.getSessionFactoryBuilder().build();

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
}
