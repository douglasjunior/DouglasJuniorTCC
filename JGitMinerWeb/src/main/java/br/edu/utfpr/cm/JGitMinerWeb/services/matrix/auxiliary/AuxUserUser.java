/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxUserUser {

    private String user;
    private String user2;
    private String userAndUser2;
    private String user2AndUser;

    public AuxUserUser(String user, String user2) {
        this.user = user;
        this.user2 = user2;
        this.userAndUser2 = user + ";" + user2;
        this.user2AndUser = user2 + ";" + user;
    }

    public AuxUserUser(String userLogin, String userMail, String userLogin2, String userMail2, String fileName) {
        this(userLogin == null || userLogin.isEmpty() ? userMail : userLogin,
                userLogin2 == null || userLogin2.isEmpty() ? userMail2 : userLogin2);
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserUser) {
            AuxUserUser other = (AuxUserUser) obj;
            if (Util.stringEquals(this.user, other.user2)
                    && Util.stringEquals(this.user2, other.user)) {
                return true;
            }
            if (Util.stringEquals(this.user, other.user)
                    && Util.stringEquals(this.user2, other.user2)) {
                return true;
            }
        }
        return false;
    }
    
    public String toStringUserAndUser2() {
        return user + ";" + user2;
    }
    
    public String toStringUser2AndUser() {
        return user2 + ";" + user;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + 
                (Objects.hashCode(this.user) + Objects.hashCode(this.user2));
        return hash;
    }

    @Override
    public String toString() {
        return userAndUser2;
    }
}
