package db.domain;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "server_users")
public final class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String name;
    private String login;
    private boolean admin;
    // todo creation time

    public User() {

    }

    public User(final String name, final String login) {
        this.name = name;
        this.login = login;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(final String login) {
        this.login = login;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(final boolean admin) {
        this.admin = admin;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof final User user) {
            return id == user.id && admin == user.admin &&
                    Objects.equals(name, user.name) && Objects.equals(login, user.login);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, login, admin);
    }
}
