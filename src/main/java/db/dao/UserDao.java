package db.dao;

import db.domain.User;
import db.utils.HibernateSessionFactoryUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;
import java.util.function.Consumer;

public final class UserDao {

    private void transaction(final Consumer<Session> action) {
        final Session session = HibernateSessionFactoryUtil.getSessionFactory().openSession();
        final Transaction transaction = session.beginTransaction();
        action.accept(session);
        transaction.commit();
        session.close();
    }
    private Query<?> generateQuery(final String request, final Object... args) {
        final Query<?> query = HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery(request);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i + 1, args[i]);
        }

        return query;
    }

    public User findById(final long id) {
        return HibernateSessionFactoryUtil.getSessionFactory().openSession().get(User.class, id);
    }

    public User findUserByLoginAndPassword(final String login, final String password) {
        return (User) generateQuery("SELECT * FROM User WHERE login=?1 AND passwordSha=SHA1(CONCAT('1be3db47a7684152', ?1, ?2))",
                login, password).getSingleResult();
    }

    public void save(final User user, final String password) {
        transaction(session -> session.save(user));

        generateQuery("UPDATE user SET passwordSha=SHA1(CONCAT('1be3db47a7684152', ?2, ?3)) WHERE id=?1",
                user.getId(), user.getLogin(), password).getSingleResult();
    }

    public void update(final User user) {
        transaction(session -> session.update(user));
    }

    public void delete(final User user) {
        transaction(session -> session.delete(user));
    }

    @SuppressWarnings("unchecked")
    public List<User> findAll() {
        return (List<User>) generateQuery("FROM User").list();
    }

    public int countUsers() {
        return findAll().size();
    }

    @SuppressWarnings("unchecked")
    public List<User> findUsersByLogin(final String login) {
        return (List<User>) generateQuery("SELECT * FROM User WHERE login=?1", login);
    }

    @SuppressWarnings("unchecked")
    public List<User> findUsersByName(final String name) {
        return (List<User>) generateQuery("SELECT * FROM User WHERE name=?1", name);
    }
}
