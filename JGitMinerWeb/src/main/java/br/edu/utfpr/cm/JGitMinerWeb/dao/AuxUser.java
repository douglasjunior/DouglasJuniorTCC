
package br.edu.utfpr.cm.JGitMinerWeb.dao;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class AuxUser {
    private final String user;

    public AuxUser(String user) {
        this.user = user;
    }

    public AuxUser(String userName, String userMail) {
        this(userName == null || userName.isEmpty() ? userMail : userName);
    }

    public String getUser() {
        return user;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUser) {
            AuxUser other = (AuxUser) obj;
            if (Util.stringEquals(this.user, other.user)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash
                + (Objects.hashCode(this.user));
        return hash;
    }

    @Override
    public String toString() {
        return user;
    }
}
