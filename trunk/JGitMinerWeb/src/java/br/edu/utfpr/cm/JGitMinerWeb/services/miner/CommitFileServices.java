/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitFile;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.CommitFile;

/**
 *
 * @author Douglas
 */
public class CommitFileServices implements Serializable  {

    public static EntityCommitFile createEntity(CommitFile gitCommitFile, GenericDao dao) {
        if (gitCommitFile == null) {
            return null;
        }

        EntityCommitFile commitFile = new EntityCommitFile();

        commitFile.setMineredAt(new Date());
        commitFile.setAdditions(gitCommitFile.getAdditions());
        commitFile.setBlobUrl(gitCommitFile.getBlobUrl());
        commitFile.setChanges(gitCommitFile.getChanges());
        commitFile.setDeletions(gitCommitFile.getDeletions());
        commitFile.setFilename(gitCommitFile.getFilename());
        commitFile.setPatch(gitCommitFile.getPatch());
        commitFile.setRawUrl(gitCommitFile.getRawUrl());
        commitFile.setSha(gitCommitFile.getSha());
        commitFile.setStatus(gitCommitFile.getStatus());

        if (commitFile.getId() == null || commitFile.getId().equals(new Long(0))) {
            dao.insert(commitFile);
        } else {
            dao.edit(commitFile);
        }

        return commitFile;
    }

    private static EntityCommitFile getCommitFileBySHA(String sha, GenericDao dao) {
        List<EntityCommitFile> files = dao.executeNamedQueryComParametros("CommitFile.findBySHA", new String[]{"sha"}, new Object[]{sha});
        if (!files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }
}
