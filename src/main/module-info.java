module database {
    requires kotlin.stdlib;
    requires kotlin.reflect;

    requires org.postgresql.jdbc;
    requires java.persistence;
    requires org.hibernate.orm.core;

    exports db;
    exports db.domain;
    exports db.services;
    exports db.utils;
}