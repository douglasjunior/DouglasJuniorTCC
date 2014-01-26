/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxUserFileFileUser {

    private String user;
    private String user2;
    private String fileName;
    private String fileName2;
    private int weight;

    public AuxUserFileFileUser(String user, String user2, String fileName, String fileName2, int weight) {
        this.user = user;
        this.user2 = user2;
        this.fileName = fileName;
        this.fileName2 = fileName2;
        this.weight = weight;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName2() {
        return fileName2;
    }

    public void setFileName2(String fileName2) {
        this.fileName2 = fileName2;
    }

    public int inc() {
        return weight++;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserFileFileUser) {
            AuxUserFileFileUser other = (AuxUserFileFileUser) obj;
            if (userEquals(other) && fileEquals(other)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.user);
        hash = 23 * hash + Objects.hashCode(this.fileName);
        hash = 23 * hash + Objects.hashCode(this.fileName2);
        return hash;
    }

    @Override
    public String toString() {
        return user + ";" + fileName + ";" + fileName2 + ";" + user2 + ";" + weight;
    }

    public boolean fileEquals(AuxUserFileFileUser other) {
        if ((Util.stringEquals(this.fileName, other.fileName) && Util.stringEquals(this.fileName2, other.fileName2))
                || (Util.stringEquals(this.fileName, other.fileName2) && Util.stringEquals(this.fileName2, other.fileName))) {
            return true;
        }
        return false;
    }

    public boolean userEquals(AuxUserFileFileUser other) {
        if ((Util.stringEquals(this.user, other.user) && Util.stringEquals(this.user2, other.user2))
                || (Util.stringEquals(this.user, other.user2) && Util.stringEquals(this.user2, other.user))) {
            return true;
        }
        return false;
    }
}
