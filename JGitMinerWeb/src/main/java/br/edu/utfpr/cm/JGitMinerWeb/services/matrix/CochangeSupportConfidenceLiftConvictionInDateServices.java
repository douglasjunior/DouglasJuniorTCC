package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.FileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.dao.PairFileDAO;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Douglas
 */
public class CochangeSupportConfidenceLiftConvictionInDateServices extends AbstractMatrixServices {

    public CochangeSupportConfidenceLiftConvictionInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CochangeSupportConfidenceLiftConvictionInDateServices(GenericDao dao, EntityRepository repo, List<EntityMatrix> matrices, Map params, OutLog out) {
        super(dao, repo, matrices, params, out);
    }

    private Date getBeginDate() {
        return getDateParam("beginDate");
    }

    private Date getEndDate() {
        return getDateParam("endDate");
    }

    public Date getFutureBeginDate() {
        return getDateParam("futureBeginDate");
    }

    public Date getFutureEndDate() {
        return getDateParam("futureEndDate");
    }

    public List<String> getFilesToIgnore() {
        return getStringLinesParam("filesToIgnore", true, false);
    }

    public List<String> getFilesToConsiders() {
        return getStringLinesParam("filesToConsiders", true, false);
    }

    public Boolean isOnlyMergeds() {
        return getBooleanParam("onlyMergeds");
    }

    public Integer getMaxFilesPerCommit() {
        return getIntegerParam("maxFilesPerCommit");
    }

    public Integer getMinFilesPerCommit() {
        return getIntegerParam("minFilesPerCommit");
    }

    @Override
    public void run() {
        try {
            PairFileDAO pairFileDAO = new PairFileDAO(dao);
            FileDAO fileDAO = new FileDAO(dao);

            if (getRepository() == null) {
                throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
            }

            Date futureBeginDate = getFutureBeginDate();
            Date futureEndDate = getFutureEndDate();
            Date beginDate = getBeginDate();
            Date endDate = getEndDate();
            List<String> filesToIgnore = getFilesToIgnore();
            List<String> filesToConsiders = getFilesToConsiders();

            out.printLog("Iniciando preenchimento da lista de pares.");

            List<Object> selectParams = new ArrayList<>();

            StringBuilder select = new StringBuilder();
            select.append("select distinct fil.filename, fil2.filename ")
                    .append(" from gitcommitfile fil, ")
                    .append("      gitpullrequest_gitrepositorycommit prc, ")
                    .append("      gitcommitfile fil2, ")
                    .append("      gitpullrequest_gitrepositorycommit prc2, ")
                    .append("      gitpullrequest pul, ")
                    .append("      gitissue i ")
                    .append("where pul.repository_id = ? ")
                    .append("  and pul.issue_id = i.id ")
                    .append("  and i.commentscount > 1 ")
                    .append("  and pul.createdat between ? and ? ");

            selectParams.add(getRepository().getId());
            selectParams.add(beginDate);
            selectParams.add(endDate);

            if (isOnlyMergeds()) {
                select.append("  and pul.mergedat is not null ");
            }
            select.append("  and prc.entitypullrequest_id = pul.id ")
                    .append("  and (select count(distinct(f.filename)) from gitcommitfile f where f.repositorycommit_id = prc.repositorycommits_id) between ? and ?")
                    .append("  and fil.repositorycommit_id = prc.repositorycommits_id ")
                    .append("  and prc2.entitypullrequest_id = pul.id ")
                    .append("  and (select count(distinct(f.filename)) from gitcommitfile f where f.repositorycommit_id = prc2.repositorycommits_id) between ? and ?")
                    .append("  and fil2.repositorycommit_id = prc2.repositorycommits_id ")
                    .append("  and md5(fil.filename) <> md5(fil2.filename) ");

            selectParams.add(getMinFilesPerCommit());
            selectParams.add(getMaxFilesPerCommit());
            selectParams.add(getMinFilesPerCommit());
            selectParams.add(getMaxFilesPerCommit());

            if (!filesToIgnore.isEmpty()) {
                for (String fileName : filesToIgnore) {
                    select.append("  and fil.filename not like ? ")
                            .append("  and fil2.filename not like ? ");
                    selectParams.add(fileName);
                    selectParams.add(fileName);
                }
            }

            if (!filesToConsiders.isEmpty()) {
                select.append(" and (");

                int likeFilename = 0;
                for (String fileName : filesToConsiders) {
                    if (likeFilename++ > 0) {
                        select.append("or");
                    }
                    select.append(" fil.filename like ? ");
                    selectParams.add(fileName);
                }
                select.append(")");

                select.append(" and (");

                likeFilename = 0;
                for (String fileName : filesToConsiders) {
                    if (likeFilename++ > 0) {
                        select.append("or");
                    }
                    select.append(" fil2.filename like ? ");
                    selectParams.add(fileName);
                }
                select.append(")");
            }

            System.out.println(select);

            List<Object[]> cochangeResult = dao.selectNativeWithParams(select.toString(), selectParams.toArray());

            Set<AuxFileFileMetrics> pairFileMetrics = new HashSet<>();
            for (Object[] record : cochangeResult) {
                AuxFileFileMetrics pairFile = new AuxFileFileMetrics(record[0] + "", record[1] + "");

                // o arquivo deve aparecer em mais de 1 pull request e não somente 1
                // caso contrário não é incluso
                long file1PullRequestIn = fileDAO.calculeNumberOfPullRequestWhereFileIsIn(
                        getRepository(), pairFile.getFile(),
                        beginDate, endDate, 0, getMaxFilesPerCommit(), isOnlyMergeds());
                if (file1PullRequestIn > 1) {

                    long file2PullRequestIn = fileDAO.calculeNumberOfPullRequestWhereFileIsIn(
                            getRepository(), pairFile.getFile2(),
                            beginDate, endDate, 0, getMaxFilesPerCommit(), isOnlyMergeds());
                    if (file2PullRequestIn > 1) {
                        pairFileMetrics.add(pairFile);
                    } else {
                        out.printLog(pairFile.getFile2() + " in #" + file2PullRequestIn + " pull requests");
                    }
                } else {
                    out.printLog(pairFile.getFile2() + " in #" + file1PullRequestIn + " pull requests");
                }
            }
            cochangeResult.clear();

            out.printLog(pairFileMetrics.size() + " pares encontrados.");

            out.printLog("Iniciando cálculo do support, confidence, lift e conviction.");
            int i = 1;
            for (AuxFileFileMetrics pairFile : pairFileMetrics) {
                if (i % 100 == 0 || i == pairFileMetrics.size()) {
                    System.out.println(i + "/" + pairFileMetrics.size());
                }

                Long pairFileNumberOfPullrequestOfPair = pairFileDAO.calculeNumberOfPullRequest(getRepository(), pairFile.getFile(), pairFile.getFile2(), getBeginDate(), getEndDate(), isOnlyMergeds());
                Long pairFileNumberOfPullrequestOfPairFuture = pairFileDAO.calculeNumberOfPullRequest(getRepository(), pairFile.getFile(), pairFile.getFile2(), futureBeginDate, futureEndDate, isOnlyMergeds());
                Long fileNumberOfPullrequestOfPairFuture = pairFileDAO.calculeNumberOfPullRequest(getRepository(), pairFile.getFile(), null, futureBeginDate, futureEndDate, isOnlyMergeds());
                Long file2NumberOfPullrequestOfPairFuture = pairFileDAO.calculeNumberOfPullRequest(getRepository(), pairFile.getFile2(), null, futureBeginDate, futureEndDate, isOnlyMergeds());
                Long numberOfAllPullrequestFuture = pairFileDAO.calculeNumberOfPullRequest(getRepository(), null, null, futureBeginDate, futureEndDate, isOnlyMergeds());

                pairFile.addMetrics(pairFileNumberOfPullrequestOfPair, pairFileNumberOfPullrequestOfPairFuture, fileNumberOfPullrequestOfPairFuture, file2NumberOfPullrequestOfPairFuture, numberOfAllPullrequestFuture);

                Double supportFile = numberOfAllPullrequestFuture == 0 ? 0d : fileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
                Double supportFile2 = numberOfAllPullrequestFuture == 0 ? 0d : file2NumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
                Double supportPairFile = numberOfAllPullrequestFuture == 0 ? 0d : pairFileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
                Double confidence = supportFile == 0 ? 0d : supportPairFile / supportFile;
                Double confidence2 = supportFile2 == 0 ? 0d : supportPairFile / supportFile2;
                Double lift = supportFile * supportFile2 == 0 ? 0d : supportPairFile / (supportFile * supportFile2);
                Double conviction = 1 - confidence == 0 ? 0d : (1 - supportFile) / (1 - confidence);
                Double conviction2 = 1 - confidence2 == 0 ? 0d : (1 - supportFile2) / (1 - confidence2);

                pairFile.addMetrics(supportFile, supportFile2, supportPairFile, confidence, confidence2, lift, conviction, conviction2);

                i++;
            }

            EntityMatrix matrix = new EntityMatrix();
            matrix.setNodes(objectsToNodes(pairFileMetrics));

            pairFileMetrics.clear();

            saveMatrix(matrix);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void saveMatrix(EntityMatrix entityMatrix) {
        out.printLog("Iniciando salvamento da rede.");
        List<EntityMatrixNode> matrixNodes = entityMatrix.getNodes();
        entityMatrix.setNodes(new ArrayList<EntityMatrixNode>());
        entityMatrix.getParams().putAll(params);
        entityMatrix.setRepository(getRepository() + "");
        entityMatrix.setClassServicesName(this.getClass().getName());
        entityMatrix.setLog(out.getLog().toString());
        dao.insert(entityMatrix);
        for (Iterator<EntityMatrixNode> it = matrixNodes.iterator(); it.hasNext();) {
            EntityMatrixNode node = it.next();
            node.setMatrix(entityMatrix);
            entityMatrix.getNodes().add(node);
            dao.insert(node);
            it.remove();
        }
        entityMatrix.setStoped(new Date());
        entityMatrix.setComplete(true);
        out.printLog("Concluida geração da rede.");
        entityMatrix.setLog(out.getLog().toString());
        dao.edit(entityMatrix);
        out.printLog("");
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "pairFileCochange;pairFileCochangeFuture;fileChangeFuture;file2ChangeFuture;allPullrequestFuture;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2";
    }
}
