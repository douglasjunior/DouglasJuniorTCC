/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services;

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
        clientes.add(new GitHubCliente("douglasjunior", "master951"));
    }

    public static GitHubClient getGitHubCliente() {
        if (i >= clientes.size()) {
            i = 0;
        }
        return clientes.get(i++);
    }
}

class GitHubCliente extends GitHubClient {

    public GitHubCliente(String user, String pass) {
        super();
        setCredentials(user, pass);
    }
}
