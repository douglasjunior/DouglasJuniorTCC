/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matriz;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.matriz.EntityMatrizRecord;
import br.edu.utfpr.cm.JGitMinerWeb.pojo.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileFileCount;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFileFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFilePull;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
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

        String jpql = "SELECT DISTINCT NEW br.edu.utfpr.cm.JGitMinerWeb.services.matriz.auxiliary.AuxFilePull(f.filename, p) "
                + "FROM "
                + "EntityPullRequest p JOIN p.issue i JOIN i.milestone m JOIN p.repositoryCommits rc JOIN rc.files f "
                + "WHERE "
                + "p.repository = :repository AND "
                + "m.number = :milestoneNumber AND "
                + "f.filename LIKE :prefixFile AND "
                + "f.filename LIKE :suffixFile ";

        System.out.println(jpql);

        List<AuxFilePull> query = dao.selectWithParams(jpql,
                new String[]{"repository", "milestoneNumber", "prefixFile", "suffixFile"},
                new Object[]{getRepository(), mileNumber, getPrefixFile(), getSuffixFile()});

        System.out.println("query: " + query.size());

        List<EntityMatrizRecord> records = new ArrayList<EntityMatrizRecord>();

        List<AuxFileFileCount> coochangesGeneral = new ArrayList<AuxFileFileCount>(); // contabiliza a quantidade de coochanges
        List<AuxFileFilePull> coochangesPull = new ArrayList<AuxFileFilePull>(); // controla os coochanges em cada pull request

        int total = query.size() * query.size();
        int i = 0;
        for (AuxFilePull aux : query) {
            System.out.println(i + "/" + total);
            for (AuxFilePull aux2 : query) {
                i++;
                if (aux.getPull().equals(aux2.getPull())) {
                    if (!aux.getFileName().equals(aux2.getFileName())) {
                        AuxFileFilePull cooPull = new AuxFileFilePull(aux.getFileName(), aux2.getFileName(), aux.getPull());
                        // verifica se o coochange ja foi registrado no pull
                        if (coochangesPull.contains(cooPull)) {
                            //    System.out.println(i + "/" + total + " Já existe neste pull: " + aux.getFileName() + " " + aux2.getFileName() + " " + aux.getPull());
                            continue; // se o coochange ja foi contabilizado no pull request então pula para o proximo
                        } else {
                            coochangesPull.add(cooPull); // se o coochange ainda não existe no pull então registra-o
                        }
                        // cria uma linha de coochange entre arquivos
                        AuxFileFileCount coo = new AuxFileFileCount(aux.getFileName(), aux2.getFileName());
                        // pega o index caso o coochange ja exista
                        int index = coochangesGeneral.indexOf(coo);
                        // verifica se o index existe
                        if (index == -1) {
                            coochangesGeneral.add(coo); // se nao existe adiciona
                            //   System.out.println(i + "/" + total + " Primeiro registro: " + aux.getFileName() + " " + aux2.getFileName() + " " + aux.getPull());
                        } else {
                            coochangesGeneral.get(index).incCount(); // se ja existe incrementa
                            //   System.out.println(i + "/" + total + " Registro incrementado: " + aux.getFileName() + " " + aux2.getFileName() + " " + aux.getPull());
                        }
                    }
                }
            }
        }

        System.out.println("coochanges: " + coochangesGeneral.size());
        setRecords(records);
    }

    @Override
    public String convertToCSV(List<EntityMatrizRecord> records) {
        StringBuilder sb = new StringBuilder("file;file2;count\n");
        for (EntityMatrizRecord record : records) {
            sb.append(record.getValueX()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(record.getValueY()).append(JsfUtil.TOKEN_SEPARATOR);
            sb.append(record.getValueZ()).append("\n");
        }
        return sb.toString();
    }
}
