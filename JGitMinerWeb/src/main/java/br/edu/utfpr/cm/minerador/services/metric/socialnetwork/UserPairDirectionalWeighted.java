package br.edu.utfpr.cm.minerador.services.metric.socialnetwork;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class UserPairDirectionalWeighted {
    private final String user;
    private final String user2;
    private int weigth;

    public UserPairDirectionalWeighted(String user, String user2) {
        this.user = user;
        this.user2 = user2;
        this.weigth = 1;
    }

    public UserPairDirectionalWeighted(String login, String email, String login2, String email2) {
        this(login == null || login.isEmpty() ? email : login,
                login2 == null || login2.isEmpty() ? email2 : login2);
    }

    public String getUser() {
        return user;
    }

    public String getUser2() {
        return user2;
    }

    public int getWeigth() {
        return weigth;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.user);
        hash = 41 * hash + Objects.hashCode(this.user2);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserPairDirectionalWeighted other = (UserPairDirectionalWeighted) obj;
        if (!Util.stringEquals(this.user, other.user)) {
            return false;
        }
        if (!Util.stringEquals(this.user2, other.user2)) {
            return false;
        }
        return true;
    }

    public int inc() {
        return weigth++;
    }

    @Override
    public String toString() {
        return user + ";" + user2 + ";" + weigth;
    }

    public String toStringDirectional() {
        return user + ";" + user2;
    }
}
