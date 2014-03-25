package sdf_manager;


import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.File;

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
            properties.load(new FileInputStream(new java.io.File("").getAbsolutePath()+File.separator+"database" + File.separator+"sdf_database.properties"));

            AnnotationConfiguration annotationConfig = new AnnotationConfiguration();
            annotationConfig.setProperty("hibernate.dialect","org.hibernate.dialect.MySQLDialect");
            annotationConfig.setProperty("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
            annotationConfig.setProperty("hibernate.connection.url", "jdbc:mysql://" + properties.getProperty("host")+"/natura2000?autoReconnect=true" );
            annotationConfig.setProperty("hibernate.connection.username", properties.getProperty("user"));
            annotationConfig.setProperty("hibernate.connection.password", properties.getProperty("password"));
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
            cfg.setProperty("hibernate.connection.url", "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=" + fileName+"");
            cfg.setProperty("hibernate.connection.username", "");
    }
}
