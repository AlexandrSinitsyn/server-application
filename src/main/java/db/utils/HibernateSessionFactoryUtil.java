package db.utils;

import db.ServerException;
import db.domain.User;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateSessionFactoryUtil {

    private static SessionFactory sessionFactory;

    private HibernateSessionFactoryUtil() {}

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                final var configuration = new Configuration().configure();
                configuration.addAnnotatedClass(User.class);
                final var builder = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(builder.build());
            } catch (final HibernateException e) {
                throw new ServerException("Database is not on", e);
            }
        }

        return sessionFactory;
    }
}
