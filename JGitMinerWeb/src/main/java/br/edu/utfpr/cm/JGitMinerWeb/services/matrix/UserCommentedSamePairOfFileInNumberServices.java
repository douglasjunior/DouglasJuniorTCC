package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityComment;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityCommitFile;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityIssue;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityPullRequest;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepositoryCommit;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserFileFileUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxUserUserDirectional;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import br.edu.utfpr.cm.JGitMinerWeb.util.Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author douglas
 */
// TODO Testar, foi refatorado por Rodrigo Kuroda 01/06/2014
public class UserCommentedSamePairOfFileInNumberServices extends AbstractMatrixServices {

    public UserCommentedSamePairOfFileInNumberServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public UserCommentedSamePairOfFileInNumberServices(GenericDao dao, EntityRepository repository, List<EntityMatrix> matricesToSave, Map params, OutLog out) {
        super(dao, repository, matricesToSave, params, out);
    }

    private String getPrefixFile() {
        return params.get("prefixFile") + "%";
    }

    private String getSuffixFile() {
        return "%" + params.get("suffixFile");
    }

    private Integer getMaxFilesPerCommit() {
        return Util.stringToInteger(params.get("maxFilesPerCommit") + "");
    }
    
    private boolean isOnlyMerged() {
        return "true".equals(params.get("onlyMerged"));
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

    public Long getBeginNumber() {
        return getLongParam("beginNumber");
    }

    public Long getEndNumber() {
        return getLongParam("endNumber");
    }

    @Override
    public void run() {
        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        StringBuilder jpql = new StringBuilder();

        List<AuxUserFileFileUserDirectional> result = new ArrayList<>();

        List<String> filesName = getFilesName();
        String prefix = getPrefixFile();
        String suffix = getSuffixFile();
        
        final List<String> paramNames = new ArrayList<>();
        paramNames.add("repo");
        paramNames.add("beginNumber");
        paramNames.add("endNumber");

        final List<Object> paramValues = new ArrayList<>();
        paramValues.add(getRepository());
        paramValues.add(getBeginNumber());
        paramValues.add(getEndNumber());

        jpql.append("SELECT DISTINCT i")
            .append(" FROM")
            .append(" EntityPullRequest p JOIN p.issue i JOIN p.repositoryCommits rc JOIN rc.files f")
            .append(" WHERE")
            .append(" p.repository = :repo")
            .append(" AND p.number BETWEEN :beginNumber AND :endNumber")
            .append(" AND i.commentsCount > 1");

        if (isOnlyMerged()) {
            jpql.append(" AND p.mergedAt IS NOT NULL");
        }
        
        // select a issue/pullrequest comments
        List<EntityIssue> issuesCommenteds;
        if (!filesName.isEmpty()) {
            
            int nameIndex = 0;
            for (String fileName : filesName) {
                String paramName = "filesName" + nameIndex++;
                
                paramNames.add(paramName);
                jpql.append(" AND f.filename NOT LIKE :").append(paramName);
                
                paramValues.add(fileName);
            }
        }
        
        if (prefix.length() > 1 || suffix.length() > 1) {
            if (prefix.length() > 1) {
                jpql.append(" AND f.filename LIKE :prefix");
                paramNames.add("prefix");
                paramValues.add(prefix);
            }
            
            if (suffix.length() > 1) {
                jpql.append(" AND f.filename LIKE :suffix");
                paramNames.add("suffix");
                paramValues.add(suffix);
            }
        }
        
        System.out.println(jpql);

        issuesCommenteds = dao.selectWithParams(jpql.toString(),
                    paramNames.toArray(new String[paramNames.size()]), 
                    paramValues.toArray());
        
        out.printLog("Issues comentadas: " + issuesCommenteds.size());
        
        String selectPullRequests = "SELECT p "
                + " FROM EntityPullRequest p "
                + " WHERE p.repository = :repo "
                + (isOnlyMerged() ? " AND p.mergedAt IS NOT NULL " : "") // merged
                + " AND p.issue = :issue "
                + " ORDER BY p.createdAt ";

        String[] selectPullRequestsParams = new String[]{"repo", "issue"};

        String selectComments = "SELECT c "
                + " FROM EntityComment c "
                + " WHERE c.issue = :issue "
                + " ORDER BY c.createdAt ";

        String[] selectCommentsParams = new String[]{"issue"};
        
        int count = 1;
        for (EntityIssue issue : issuesCommenteds) {
            out.printLog("##################### NR: " + issue.getNumber() + " URL: " + issue.getUrl());
            out.printLog(count + " of the " + issuesCommenteds.size());

            EntityPullRequest pr = dao.selectOneWithParams(selectPullRequests,
                    selectPullRequestsParams,
                    new Object[]{getRepository(), issue});

            if (pr.getRepositoryCommits().isEmpty()) {
                continue;
            }

            out.printLog(pr.getRepositoryCommits().size() + " commits in pull request ");

            List<EntityCommitFile> commitFiles = new ArrayList<>();
            for (EntityRepositoryCommit comm : pr.getRepositoryCommits()) {
                if (comm.getFiles().size() <= getMaxFilesPerCommit()) {
                    commitFiles.addAll(comm.getFiles());
                }
            }

            out.printLog(commitFiles.size() + " files in pull request ");

            Set<AuxFileFile> tempResultFiles = new HashSet<>();

            for (int i = 0; i < commitFiles.size(); i++) {
                EntityCommitFile file1 = commitFiles.get(i);
                for (int j = i + 1; j < commitFiles.size(); j++) {
                    EntityCommitFile file2 = commitFiles.get(j);
                    if (!file1.equals(file2)
                            && !Util.stringEquals(file1.getFilename(), file2.getFilename())) {
                        tempResultFiles.add(new AuxFileFile(file1.getFilename(), file2.getFilename()));
                    }
                }
            }
            commitFiles.clear();

            List<EntityComment> comments = dao.selectWithParams(selectComments,
                    selectCommentsParams,
                    new Object[]{issue});
            out.printLog(comments.size() + " comments");

            List<AuxUserUserDirectional> tempResultUsers = new ArrayList<>();

            for (int k = 0; k < comments.size(); k++) {
                EntityComment iCom = comments.get(k);
                for (int l = k - 1; l >= 0; l--) {
                    EntityComment jCom = comments.get(l);
                    if (iCom.getUser().equals(jCom.getUser())) {
                        break;
                    }
                    boolean contem = false;
                    AuxUserUserDirectional aux = new AuxUserUserDirectional(
                            iCom.getUser().getLogin(),
                            iCom.getUser().getEmail(),
                            jCom.getUser().getLogin(),
                            jCom.getUser().getEmail());
                    for (AuxUserUserDirectional a : tempResultUsers) {
                        if (a.equals(aux)) {
                            a.inc();
                            contem = true;
                            break;
                        }
                    }
                    if (!contem) {
                        tempResultUsers.add(aux);
                    }
                }
            }
            comments.clear();

            for (AuxUserUserDirectional users : tempResultUsers) {
                for (AuxFileFile files : tempResultFiles) {
                    AuxUserFileFileUserDirectional aux = new AuxUserFileFileUserDirectional(
                            users.getUser(),
                            files.getFileName(),
                            files.getFileName2(),
                            users.getUser2(),
                            users.getWeigth());
                    boolean contem = false;
                    for (AuxUserFileFileUserDirectional a : result) {
                        if (a.equals(aux)) {
                            a.inc();
                            contem = true;
                            break;
                        }
                    }
                    if (!contem) {
                        result.add(aux);
                    }
                }
            }

            count++;
            out.printLog("Temp user result: " + result.size());
        }

        System.out.println("Result: " + result.size());

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(result));
        matricesToSave.add(matrix);
    }

    @Override
    public String getHeadCSV() {
        return "user;file;file2;user2;weigth";
    }
}