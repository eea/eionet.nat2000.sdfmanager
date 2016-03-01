package sdf_manager;


import java.io.FileInputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

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
    private static final ServiceRegistry serviceRegistry;

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
        			.applySetting("hibernate.dialect", "org.hibernate.dialect.MySQL5Dialect")
        			.applySetting("hibernate.hikari.dataSourceClassName", "com.mysql.jdbc.jdbc2.optional.MysqlDataSource")
        			.applySetting("hibernate.hikari.dataSource.url", "jdbc:mysql://" + properties.getProperty("db.host")
                    + ":" +  properties.getProperty("db.port")
                    + "/" + dbSchemaName + "?autoReconnect=true")
        			.applySetting("hibernate.hikari.dataSource.user", properties.getProperty("db.user"))
        			.applySetting("hibernate.hikari.dataSource.password", properties.getProperty("db.password"))
        			.applySetting("hibernate.connection.release_mode", "after_transaction")
        			.configure("hibernate.cfg.xml").build();
        	Metadata metadata = new MetadataSources(standardRegistry)        			        			
        			.getMetadataBuilder().build();        	
                   
            serviceRegistry = standardRegistry;
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
