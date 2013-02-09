package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author marcelo
 */
public class PasswordHash implements Serializable {

    /**
     * Encripta uma string utilizando um hash de 256 bits
     *
     * @param pwd pwd password a ser encriptado
     * @return uma <code>String</code> com o password encriptado, essa String
     * contem 64 caracteres.
     */
    public String hash256(String pwd) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger hash = new BigInteger(1, md.digest(pwd.getBytes()));
        return hash.toString(16);

    }

    /**
     * Encripta uma string utilizando um hash de 512 bits
     *
     * @param pwd password a ser encriptado
     * @return uma <code>String</code> com o password encriptado, essa String
     * contem 128 caracteres.
     */
    public String hash512(String pwd) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        BigInteger hash = new BigInteger(1, md.digest(pwd.getBytes()));
        return hash.toString(16);

    }
}
