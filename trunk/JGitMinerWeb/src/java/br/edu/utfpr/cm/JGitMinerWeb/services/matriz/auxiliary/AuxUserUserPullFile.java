/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxUserUserPullFile {

    private String userIdentity;
    private String userIdentity2;
    private Integer pullNumber;
    private String fileName;

    public AuxUserUserPullFile(String userIdentity, String userIdentity2, Integer pullNumber, String fileName) {
        this.userIdentity = userIdentity;
        this.userIdentity2 = userIdentity2;
        this.pullNumber = pullNumber;
        this.fileName = fileName;
    }

    public String getUserIdentity() {
        return userIdentity;
    }

    public void setUserIdentity(String userIdentity) {
        this.userIdentity = userIdentity;
    }

    public String getUserIdentity2() {
        return userIdentity2;
    }

    public void setUserIdentity2(String userIdentity2) {
        this.userIdentity2 = userIdentity2;
    }

    public Integer getPullNumber() {
        return pullNumber;
    }

    public void setPullNumber(Integer pullNumber) {
        this.pullNumber = pullNumber;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserUserPullFile) {
            AuxUserUserPullFile other = (AuxUserUserPullFile) obj;
            if (this.pullNumber.equals(other.pullNumber) && this.fileName.equals(other.fileName)) {
                if (this.userIdentity.equals(other.userIdentity) && this.userIdentity2.equals(other.userIdentity2)) {
                    return true;
                }
                if (this.userIdentity.equals(other.userIdentity2) && this.userIdentity2.equals(other.userIdentity)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.userIdentity);
        hash = 31 * hash + Objects.hashCode(this.userIdentity2);
        hash = 31 * hash + Objects.hashCode(this.pullNumber);
        hash = 31 * hash + Objects.hashCode(this.fileName);
        return hash;
    }

    @Override
    public String toString() {
        return userIdentity + " | " + userIdentity2 + " | " + pullNumber + " | " + fileName;
    }
}
