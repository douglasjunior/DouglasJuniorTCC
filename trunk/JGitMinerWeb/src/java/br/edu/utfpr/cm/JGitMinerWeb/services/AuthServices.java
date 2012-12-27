/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.egit.github.core.client.GitHubClient;

/**
 *
 * @author douglas
 */
public class AuthServices {

    private static final List<GitHubCliente> clientes;
    private static int i;

    static {
        clientes = new ArrayList<GitHubCliente>();
        try {
            prepareAccounts();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static GitHubClient getGitHubCliente() {
        if (i >= clientes.size()) {
            i = 0;
        }

        return clientes.get(i++);
    }

    private static void prepareAccounts() throws FileNotFoundException, IOException {
        String path = AuthServices.class.getResource("../accounts").getPath();
        System.out.println("path accounts: " + path);
        System.out.println("path accounts decode: " + URLDecoder.decode(path, "ASCII"));
        File fileAccounts = new File(URLDecoder.decode(path, "ASCII"));
        BufferedReader bf = new BufferedReader(new FileReader(fileAccounts));
        while (bf.ready()) {
            String linha = bf.readLine();
            String[] login = linha.split("[,]");
            clientes.add(new GitHubCliente(login[0], login[1]));
        }
        System.out.println(clientes);
    }
}

class GitHubCliente extends GitHubClient {

    public GitHubCliente(String user, String pass) {
        super();
        setCredentials(user, pass);
    }

    @Override
    public String toString() {
        return getUser() + " | " + super.toString();
    }
}
