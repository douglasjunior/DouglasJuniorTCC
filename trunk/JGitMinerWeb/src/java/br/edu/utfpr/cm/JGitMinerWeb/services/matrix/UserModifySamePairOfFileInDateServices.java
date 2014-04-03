/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUser;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserFile;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author douglas
 */
public class UserModifySamePairOfFileInDateServices extends AbstractMatrixServices {

    public UserModifySamePairOfFileInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public UserModifySamePairOfFileInDateServices(GenericDao dao, EntityRepository repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    private Integer getMinFilesPerCommit() {
        return Util.stringToInteger(params.get("minFilesPerCommit") + "");
    }

    private List<String> getFilesName() {
        List<String> filesName = new ArrayList<>();
        for (String fileName : (params.get("filesName") + "").split("\n")) {
            fileName = fileName.trim();
            if (!fileName.isEmpty()) {
                filesName.add(fileName);
            }
        }
        return filesName;
    }
    
    @Override
    public Date getBeginDate() {
        Calendar beginDateCalendar = Calendar.getInstance();
        beginDateCalendar.setTime(super.getBeginDate());
        beginDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        beginDateCalendar.set(Calendar.MINUTE, 0);
        beginDateCalendar.set(Calendar.SECOND, 0);
        beginDateCalendar.set(Calendar.MILLISECOND, 0);
        return beginDateCalendar.getTime();
    }
    
    @Override
    public Date getEndDate() {
        Calendar endDateCalendar = Calendar.getInstance();
        endDateCalendar.setTime(super.getEndDate());
        endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endDateCalendar.set(Calendar.MINUTE, 59);
        endDateCalendar.set(Calendar.SECOND, 59);
        endDateCalendar.set(Calendar.MILLISECOND, 999);
        return endDateCalendar.getTime();
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }
        Date beginDate = getBeginDate();
        Date endDate = getEndDate();
        
        List<String> filesName = getFilesName();
        String prefix = getPrefixFile();
        String suffix = getSuffixFile();
        Integer minFilesPerCommit = getMinFilesPerCommit();

        String jpql = "SELECT DISTINCT rc "
                + "FROM "
                + "EntityPullRequest p JOIN p.repositoryCommits rc "
                + "WHERE "
                + "p.repository = :repo AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "(SELECT COUNT(f) FROM EntityCommitFile f WHERE f.repositoryCommit = rc) BETWEEN 2 AND :minFilesPerCommit";

        //System.out.println(jpql);

        List<EntityRepositoryCommit> commits = dao.selectWithParams(jpql,
                new String[]{"repo", "beginDate", "endDate", "minFilesPerCommit"},
                new Object[]{getRepository(), beginDate, endDate, minFilesPerCommit});

        // primeiro monta-se a rede dos desenvolvedores e os pares de arquivos
        List<AuxUserFileFile> temp = new ArrayList<>();
        int count = 0;
        for (EntityRepositoryCommit commit : commits) {
            out.printLog("############################### ID: " + commit.getId() + " - URL: " + commit.getUrl());
            out.printLog(count + " of the " + commits.size());
            out.printLog(commit.getFiles().size() + " files to analyse");
            EntityCommitFile[] arrFiles = new EntityCommitFile[commit.getFiles().size()];
            arrFiles =  commit.getFiles().toArray(arrFiles);
            for (int i = 0; i < arrFiles.length; i++) {
                EntityCommitFile file = arrFiles[i];
                for (int j = i + 1; j < arrFiles.length; j++) {
                    EntityCommitFile file2 = arrFiles[j];
                    if (file != null && file2 != null) {
                        String login;
                        String email;
                        if (commit.getCommitter() != null) {
                            login = commit.getCommitter().getLogin();
                            email = commit.getCommitter().getEmail();
                        } else {
                            login = commit.getCommit().getCommitter().getEmail();
                            email = commit.getCommit().getCommitter().getEmail();
                        }
                        AuxUserFileFile aux = new AuxUserFileFile(login, email, file.getFilename(), file2.getFilename());
                        boolean contains = false;
                        for (AuxUserFileFile a : temp) {
                            if (a.equals(aux)) {
                                a.inc();
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            temp.add(aux);
                        }
                    }
                }
            }
            out.printLog("Temp result: " + temp.size());
            count++;
        }

        List<AuxUserFileFileUser> result = new ArrayList<>();
        // por fim liga-se os desenvolvedores que modificaram o mesmo par de arquivos
        for (int i = 0; i < temp.size(); i++) {
            AuxUserFileFile iAux = temp.get(i);
            for (int j = i + 1; j < temp.size(); j++) {
                AuxUserFileFile jAux = temp.get(j);
                if (!Util.stringEquals(iAux.getUser(), jAux.getUser()) && iAux.fileEquals(jAux)) {
                    AuxUserFileFileUser aux = new AuxUserFileFileUser(iAux.getUser(), jAux.getUser(), iAux.getFileName(), iAux.getFileName2(), iAux.getWeight() + jAux.getWeight());
                    result.add(aux);
                }
            }
        }

        System.out.println("Result : " + result.size());

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(result));
        matricesToSave.add(matrix);
    }

    @Override
    public String getHeadCSV() {
        return "user;file;file2;user2;weight";
    }

    private List<AuxUserUserFile> removeDuplicade(List<AuxUserUserFile> result) {
        List<AuxUserUserFile> newResult = new ArrayList<>(result.size());
        for (AuxUserUserFile aux : result) {
            if (!newResult.contains(aux)) {
                newResult.add(aux);
            }
        }
        result.clear();
        result = null;
        return newResult;
    }

}
