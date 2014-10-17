/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.NoSuchPageException;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.client.PagedRequest;

/**
 *
 * @author douglas
 */
public class StarServices implements Serializable {

    public static List<User> getStarsByRepository(String ownerRepositoryLogin, String repositoryName) {
        // /repos/:owner/:repo/stargazers
        try {
            StringBuilder uri = new StringBuilder("/repos");
            uri.append('/').append(ownerRepositoryLogin);
            uri.append('/').append(repositoryName);
            uri.append("/stargazers");
            PagedRequest<User> request = new PagedRequest<User>(1, 100);
            request.setUri(uri);
            request.setType(new TypeToken<List<User>>() {
            }.getType());
            PageIterator<User> pageIterator = new PageIterator<User>(request, AuthServices.getGitHubClient());
            List<User> elements = new ArrayList<User>();
            try {
                while (pageIterator.hasNext()) {
                    elements.addAll(pageIterator.next());
                }
            } catch (NoSuchPageException pageException) {
                throw pageException;
            }
            return elements;
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return getStarsByRepository(ownerRepositoryLogin, repositoryName);
        }
    }

}
