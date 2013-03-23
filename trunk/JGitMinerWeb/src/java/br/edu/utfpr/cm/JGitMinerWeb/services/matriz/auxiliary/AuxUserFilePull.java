/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

/**
 *
 * @author douglas
 */
public class AuxUserFilePull {

    private String fileName;
    private String userLogin;
    private String userEmail;
    private Integer pullNumber;

    public AuxUserFilePull(String userLogin, String userEmail, Integer pullNumber, String fileName) {
        this.userLogin = userLogin;
        this.userEmail = userEmail;
        this.pullNumber = pullNumber;
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getPullNumber() {
        return pullNumber;
    }

    public void setPullNumber(Integer pullNumber) {
        this.pullNumber = pullNumber;
    }

    public String getUserIdentity() {
        if (userLogin != null && !userLogin.isEmpty()) {
            return userLogin;
        }
        return userEmail;
    }

    @Override
    public String toString() {
        return getUserIdentity() + ";" + getPullNumber() + ";" + getFileName();
    }
}
