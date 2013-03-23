/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary;

/**
 *
 * @author douglas
 */
public class AuxUserFile {

    private String userLogin;
    private String userEmail;
    private String fileName;

    public AuxUserFile(String userLogin, String userEmail, String fileName) {
        this.userLogin = userLogin;
        this.userEmail = userEmail;
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Object getUserIdentity() {
        if (userLogin != null && !userLogin.isEmpty()) {
            return userLogin;
        }
        return userEmail;
    }
}
