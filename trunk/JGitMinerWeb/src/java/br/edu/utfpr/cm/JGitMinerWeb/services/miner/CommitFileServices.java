/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepositoryCommit;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.CommitFile;

/**
 *
 * @author Douglas
 */
public class CommitFileServices implements Serializable {

    public static EntityCommitFile createEntity(CommitFile gitCommitFile, GenericDao dao, EntityRepositoryCommit repoCommit) {
        if (gitCommitFile == null) {
            return null;
        }

        EntityCommitFile commitFile = findByCommitAndSHA(gitCommitFile.getSha(), repoCommit, dao);

        if (commitFile == null) {
            commitFile = new EntityCommitFile();
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
            dao.insert(commitFile);
        }

        return commitFile;
    }

    private static EntityCommitFile findByCommitAndSHA(String sha, EntityRepositoryCommit repoCommit, GenericDao dao) {
        List<EntityCommitFile> files = dao.executeNamedQueryComParametros("CommitFile.findByCommitAndSHA", new String[]{"sha", "repoCommit"}, new Object[]{sha, repoCommit}, true);
        if (!files.isEmpty()) {
            return files.get(0);
        }
        return null;
    }
}
