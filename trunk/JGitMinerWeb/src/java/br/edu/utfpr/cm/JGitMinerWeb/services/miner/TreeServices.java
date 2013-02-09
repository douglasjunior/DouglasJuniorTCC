/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.miner;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityTree;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityTreeEntry;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;

/**
 *
 * @author douglas
 */
public class TreeServices implements Serializable  {

    public static EntityTree createTreeEntity(Tree gitTree, Repository gitRepo, GenericDao dao) {
        if (gitTree == null) {
            return null;
        }

        try {
            gitTree = new TreeService(AuthServices.getGitHubCliente()).getTree(gitRepo, gitTree.getSha());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        EntityTree tree = null;//getTreeByURL(gitTree.getUrl(), dao);

        if (tree == null) {
            tree = new EntityTree();
        }

        tree.setMineredAt(new Date());
        tree.setSha(gitTree.getSha());
        tree.setUrl(gitTree.getUrl());

        if (tree.getId() == null || tree.getId().equals(new Long(0))) {
            dao.insert(tree);
        } else {
            dao.edit(tree);
        }

        createTreeEntryEntitys(gitTree.getTree(), tree, dao);

        return tree;
    }

    private static EntityTree getTreeByURL(String url, GenericDao dao) {
        List<EntityTree> trees = dao.executeNamedQueryComParametros("Tree.findByURL", new String[]{"url"}, new Object[]{url});
        if (!trees.isEmpty()) {
            return trees.get(0);
        }
        return null;
    }

    private static void createTreeEntryEntitys(List<TreeEntry> gitTreeEntitys, EntityTree tree, GenericDao dao) {
        if (gitTreeEntitys != null) {
            for (TreeEntry gitTreeEntry : gitTreeEntitys) {
                EntityTreeEntry treeEntry = null;// findTreeEntryByURL(gitTreeEntry.getUrl(), dao);

                if (treeEntry == null) {
                    treeEntry = new EntityTreeEntry();
                }

                treeEntry.setMineredAt(new Date());
                treeEntry.setMode(gitTreeEntry.getMode());
                treeEntry.setPathTreeEntry(gitTreeEntry.getPath());
                treeEntry.setSha(gitTreeEntry.getSha());
                treeEntry.setSizeTreeEntry(gitTreeEntry.getSize());
                treeEntry.setType(gitTreeEntry.getType());
                treeEntry.setUrl(gitTreeEntry.getUrl());
                tree.addTreeEntry(treeEntry);

                if (treeEntry.getId() == null || treeEntry.getId().equals(new Long(0))) {
                    dao.insert(treeEntry);
                } else {
                    dao.edit(treeEntry);
                }
            }
        }
    }

    private static EntityTreeEntry findTreeEntryByURL(String url, GenericDao dao) {
        List<EntityTreeEntry> treeEntrys = dao.executeNamedQueryComParametros("TreeEntry.findByURL", new String[]{"url"}, new Object[]{url});
        if (!treeEntrys.isEmpty()) {
            return treeEntrys.get(0);
        }
        return null;
    }
}
