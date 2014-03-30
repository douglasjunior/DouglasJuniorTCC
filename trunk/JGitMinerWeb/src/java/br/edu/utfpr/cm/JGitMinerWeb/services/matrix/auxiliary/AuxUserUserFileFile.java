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
public class AuxUserUserFileFile {

    private String user;
    private String user2;
    private String fileName;
    private String fileName2;

    public AuxUserUserFileFile(String user, String user2, String fileName, String fileName2) {
        this.user = user;
        this.user2 = user2;
        this.fileName = fileName;
        this.fileName2 = fileName2;
    }

    public AuxUserUserFileFile(String userLogin, String userMail, String userLogin2, String userMail2, String fileName, String fileName2) {
        this(userLogin == null || userLogin.isEmpty() ? userMail : userLogin,
                userLogin2 == null || userLogin2.isEmpty() ? userMail2 : userLogin2,
                fileName, fileName2);
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

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof AuxUserUserFileFile) {
            AuxUserUserFileFile other = (AuxUserUserFileFile) obj;
            // os arquivos não tem direção
            if ((Util.stringEquals(this.fileName, other.fileName) && Util.stringEquals(this.fileName2, other.fileName2))
                    || (Util.stringEquals(this.fileName, other.fileName2) && Util.stringEquals(this.fileName2, other.fileName))) {
                // os usuários tem direção, não pode ser igualado "user" com "user2"
                if (Util.stringEquals(this.user, other.user)
                        && Util.stringEquals(this.user2, other.user2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + Objects.hashCode(this.user) + Objects.hashCode(this.user2);
        hash = 23 * hash + Objects.hashCode(this.fileName) + Objects.hashCode(this.fileName2);
        return hash;
    }

    @Override
    public String toString() {
        return user + ";" + user2 + ";" + fileName + ";" + fileName2;
    }
}
