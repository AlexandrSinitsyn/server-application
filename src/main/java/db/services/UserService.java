package db.services;

import db.annotations.Admin;
import db.annotations.Confirmation;
import db.annotations.PrivateOnly;
import db.annotations.SystemOnly;
import db.dao.UserDao;
import db.domain.User;

import java.util.List;

public class UserService {

    private final UserDao usersDao = new UserDao();

    public UserService() {
    }

    public User findUserById(final long id) {
        return usersDao.findById(id);
    }

    public List<User> findUsersByName(final String name) {
        return usersDao.findUsersByName(name);
    }

    public List<User> findUsersByLogin(final String login) {
        return usersDao.findUsersByLogin(login);
    }

    public List<User> findAllUsers() {
        return usersDao.findAll();
    }

    @SystemOnly
    public void saveUser(final User user, final String password) {
        usersDao.save(user, password);
    }

    @SystemOnly
    @Confirmation("action")
    public void deleteUser(final User user) {
        usersDao.delete(user);
    }

    @SystemOnly
    public void updateUser(final User user) {
        usersDao.update(user);
    }

    public int countUsers() {
        return usersDao.countUsers();
    }

    @PrivateOnly
    public void resetName(final User user, final String name) {
        user.setName(name);

        updateUser(user);
    }

    @PrivateOnly
    public void resetLogin(final User user, final String login) {
        user.setLogin(login);

        updateUser(user);
    }

    @PrivateOnly
    @Confirmation("password")
    public void resetPassword(final User user, final String password) {

    }

    @Admin
    public void makeAdmin(final User user) {
        user.setAdmin(true);

        updateUser(user);
    }

    @Admin
    public void makeRegular(final User user) {
        user.setAdmin(false);

        updateUser(user);
    }

    @SystemOnly
    public boolean checkPassword(final User user, final String password) {
        return usersDao.findUserByLoginAndPassword(user.getLogin(), password).equals(user);
    }
}
