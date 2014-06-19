/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author Douglas
 */
public class AuxUserUserDirectional {

    private final String user;
    private final String user2;
    private int weigth;

    public AuxUserUserDirectional(String user, String user2) {
        this.user = user;
        this.user2 = user2;
        this.weigth = 1;
    }

    public AuxUserUserDirectional(String login, String email, String login2, String email2) {
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
        final AuxUserUserDirectional other = (AuxUserUserDirectional) obj;
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

}
