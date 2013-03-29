/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.nodes.NodeGeneric;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class FileModifiedTogetherOnIssueInMilestoneServices extends AbstractMatrizServices {

    public FileModifiedTogetherOnIssueInMilestoneServices(GenericDao dao) {
        super(dao);
    }

    public FileModifiedTogetherOnIssueInMilestoneServices(GenericDao dao, EntityRepository repository, Map params) {
        super(dao, repository, params);
    }

    public int getMilestoneNumber() {
        String mileNumber = params.get("milestoneNumber") + "";
        return Util.tratarStringParaInt(mileNumber);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        int mileNumber = getMilestoneNumber();

        if (mileNumber <= 0) {
            throw new IllegalArgumentException("Numero do Milestone inválido.");
        }

        String jpql = "SELECT DISTINCT NEW " + AuxFileFilePull.class.getName() + "(f.filename, f2.filename, p.number) "
                + "FROM "
                + "EntityPullRequest p JOIN p.issue i JOIN i.milestone m JOIN p.repositoryCommits rc JOIN p.repositoryCommits rc2 JOIN rc.files f JOIN rc2.files f2 "
                + "WHERE "
                + "p.repository = :repository AND "
                + "m.number = :milestoneNumber AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile AND "
                + "f2.filename LIKE :prefixFile AND "
                + "f2.filename LIKE :suffixFile AND "
                + "f.filename <> f2.filename ";

        System.out.println(jpql);

        int countTemp = 0;
        int offset = 1;
        final int limit = 50000;
        int countTotal = 0;

        List<NodeGeneric> nodes = new ArrayList<>();
        do {
            List<AuxFileFilePull> resultTemp = dao.selectWithParams(jpql,
                    new String[]{"repository", "milestoneNumber", "prefixFile", "suffixFile"},
                    new Object[]{getRepository(), mileNumber, getPrefixFile(), getSuffixFile()},
                    offset, limit);
            countTemp = resultTemp.size();
            countTotal += countTemp;
            System.out.println("countTemp: " + countTemp);
            System.out.println("countTotal: " + countTotal);
            System.out.println("Transformando em Nodes...");
            for (Iterator<AuxFileFilePull> it = resultTemp.iterator(); it.hasNext();) {
                AuxFileFilePull aux = it.next();
                incrementNode(nodes, new NodeGeneric(aux.getFileName(), aux.getFileName2()));
            }
            System.out.println("Transformação concluída!");
            offset += limit;
            System.gc();
        } while (countTemp >= limit);

        System.out.println("Nodes: " + nodes.size());
        addToEntityMatrizNodeList(nodes);
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;count";
    }
}
