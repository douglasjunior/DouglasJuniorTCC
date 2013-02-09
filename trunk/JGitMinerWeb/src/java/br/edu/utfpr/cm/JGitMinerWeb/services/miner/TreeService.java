/**
 * ****************************************************************************
 * Copyright (c) 2012 GitHub Inc. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Douglas Nassif Roma Junior
 * ***************************************************************************
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import java.io.IOException;
import java.io.Serializable;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_GIT;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_TREES;
import org.eclipse.egit.github.core.service.GitHubService;

/**
 * Service for interacting with repository trees
 *
 * @see <a href="http://developer.github.com/v3/git/trees/">GitHub commit API
 * documentation</a>
 */
public class TreeService extends GitHubService implements Serializable  {

    /**
     * Create tree service
     */
    public TreeService() {
        super();
    }

    /**
     * Create tree service
     *
     * @param client
     */
    public TreeService(GitHubClient client) {
        super(client);
    }

    /**
     * Get trees with given SHA-1 from given repository
     *
     * @param repository
     * @param sha
     * @return tree
     * @throws IOException
     */
    public Tree getTree(IRepositoryIdProvider repository,
            String sha) throws IOException {
        String id = getId(repository);
        if (sha == null) {
            throw new IllegalArgumentException("Sha cannot be null"); //$NON-NLS-1$
        }
        if (sha.length() == 0) {
            throw new IllegalArgumentException("Sha cannot be empty"); //$NON-NLS-1$
        }
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(id);
        uri.append(SEGMENT_GIT);
        uri.append(SEGMENT_TREES);
        uri.append('/').append(sha);
        GitHubRequest request = createRequest();
        request.setUri(uri);
        System.out.println("uri: " + uri);
        request.setType(Tree.class);
        return (Tree) client.get(request).getBody();
    }
}
