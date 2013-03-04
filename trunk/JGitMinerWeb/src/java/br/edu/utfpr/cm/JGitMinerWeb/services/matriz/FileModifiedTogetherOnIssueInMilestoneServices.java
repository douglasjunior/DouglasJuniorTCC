/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizNode;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        String jpql = "SELECT DISTINCT NEW " + AuxFilePull.class.getName() + "(f.filename, p) "
                + "FROM "
                + "EntityPullRequest p JOIN p.issue i JOIN i.milestone m JOIN p.repositoryCommits rc JOIN rc.files f "
                + "WHERE "
                + "p.repository = :repository AND "
                + "m.number = :milestoneNumber AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile";

        System.out.println(jpql);

        List<AuxFilePull> query = dao.selectWithParams(jpql,
                new String[]{"repository", "milestoneNumber", "prefixFile", "suffixFile"},
                new Object[]{getRepository(), mileNumber, getPrefixFile(), getSuffixFile()});

        System.out.println("query: " + query.size());

        List<EntityMatrizNode> nodes = new ArrayList<>();

        Set<AuxFileFilePull> controls = new HashSet<>(); // controla os coochanges em cada pull request

        for (int i = 0; i < query.size(); i++) {
            System.out.println(i + "/" + query.size());
            AuxFilePull aux = query.get(i);
            for (int j = i; j < query.size(); j++) {
                AuxFilePull aux2 = query.get(j);
                if (aux.getPull().equals(aux2.getPull())) {
                    if (!aux.getFileName().equals(aux2.getFileName())) {
                        AuxFileFilePull control = new AuxFileFilePull(aux.getFileName(), aux2.getFileName(), aux.getPull());
                        // verifica se o coochange ja foi registrado no pull
                        if (controls.add(control)) {
                            incrementNode(nodes, new EntityMatrizNode(aux.getFileName(), aux2.getFileName()));
                        }
                    }
                }
            }
        }
        setNodes(nodes);
    }

    private void incrementNode(List<EntityMatrizNode> nodes, EntityMatrizNode node) {
        int i = nodes.indexOf(node);
        if (i >= 0) {
            nodes.get(i).incWeight();
        } else {
            nodes.add(node);
        }
    }

    @Override
    public String convertToCSV(Collection<EntityMatrizNode> nodes) {
        StringBuilder sb = new StringBuilder("file;file2;count\n");
        for (EntityMatrizNode node : nodes) {
            sb.append(node.getFrom()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(node.getTo()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(node.getWeight()).append("\n");
        }
        return sb.toString();
    }
}
