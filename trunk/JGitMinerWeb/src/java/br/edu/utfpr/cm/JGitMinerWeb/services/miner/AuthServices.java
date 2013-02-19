/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.egit.github.core.Application;
import org.eclipse.egit.github.core.Authorization;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OAuthService;

/**
 *
 * @author douglas
 */
public class AuthServices implements Serializable {

    private static final List<GitHubClient> clients;
    private static int i;
    private static int rate;
    private static final String APP_NAME;

    static {
        APP_NAME = "JGitMinerWeb";
        i = 0;
        rate = 0;
        clients = new ArrayList<GitHubClient>();
        try {
            prepareAccounts();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static GitHubClient getGitHubCliente() {
        rate++;
        if (rate >= 500) {
            rate = 0;
            i++;
            if (i >= clients.size()) {
                i = 0;
            }
        }
        return clients.get(i);
    }

    private static void prepareAccounts() throws FileNotFoundException, IOException {
        String path = AuthServices.class.getResource("../../accounts").getPath();
        File fileAccounts = new File(URLDecoder.decode(path, "ASCII"));
        BufferedReader bf = new BufferedReader(new FileReader(fileAccounts));
        while (bf.ready()) {
            String linha = bf.readLine();
            String[] login = linha.split("[,]");
            GitHubClient cl = createCliente(login[0], login[1]);
            if (cl != null) {
                clients.add(cl);
            }
        }
    }

    private static GitHubClient createCliente(String user, String pass) {
        GitHubClient cliente = new GitHubClient();
        cliente.setCredentials(user, pass);
        OAuthService oauth = new OAuthService(cliente);
        Authorization auth = new Authorization();
        auth.setApp(new Application().setName(APP_NAME));
        try {
            String token;
            if (oauth.getAuthorizations() == null || oauth.getAuthorizations().isEmpty()) {
                auth = oauth.createAuthorization(auth);
                token = auth.getToken();
                System.out.println("autorizooou: " + token);
            } else {
                List<Authorization> auths = oauth.getAuthorizations();
                System.out.println("autorizaçoes: " + auths.size());
                System.out.println("autorização: " + auths.get(0));
                token = auths.get(0).getToken();
                System.out.println("token: " + token);
            }
            return new GitHubClient().setOAuth2Token(token);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
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
