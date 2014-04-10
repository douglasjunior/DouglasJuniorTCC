/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.matrix;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Douglas
 */
public class CochangeSupportConfidenceLiftConvictionInDateServices extends AbstractMatrixServices {

    private EntityRepository repository;

    public CochangeSupportConfidenceLiftConvictionInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CochangeSupportConfidenceLiftConvictionInDateServices(GenericDao dao, EntityRepository repo, List<EntityMatrix> matrices, Map params, OutLog out) {
        super(dao, repo, matrices, params, out);
    }

    private Integer getIntervalOfMonths() {
        return getIntegerParam("intervalOfMonths");
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
        return getStringLinesParam("filesToIgnore", true, true);
    }

    @Override
    public void run() {

        System.out.println(params);

        if (getRepository() == null) {
            throw new IllegalArgumentException("Parâmetro Repository não pode ser nulo.");
        }

        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();
        Date beginDate = getBeginDate();
        Date endDate = getEndDate();

        out.printLog("Iniciando preenchimento da lista de pares.");
        Set<AuxFileFileMetrics> pairFileMetrics = new HashSet<>();
        

        out.printLog(pairFileMetrics.size() + " pares encontrados.");

        dao.clearCache(true);

        out.printLog("Iniciando cálculo do support, confidence, lift e conviction.");
        for (AuxFileFileMetrics pairFile : pairFileMetrics) {
            Long pairFileNumberOfPullrequestOfPair = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), beginDate, endDate);
            Long pairFileNumberOfPullrequestOfPairFuture = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), futureBeginDate, futureEndDate);
            Long fileNumberOfPullrequestOfPairFuture = calculeUpdates(pairFile.getFile(), null, futureBeginDate, futureEndDate);
            Long file2NumberOfPullrequestOfPairFuture = calculeUpdates(pairFile.getFile2(), null, futureBeginDate, futureEndDate);
            Long numberOfAllPullrequestFuture = calculeUpdates(null, null, futureBeginDate, futureEndDate);

            pairFile.addMetrics(pairFileNumberOfPullrequestOfPair, pairFileNumberOfPullrequestOfPairFuture, fileNumberOfPullrequestOfPairFuture, file2NumberOfPullrequestOfPairFuture, numberOfAllPullrequestFuture);

            Double supportFile = numberOfAllPullrequestFuture == 0 ? 0d : fileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double supportFile2 = numberOfAllPullrequestFuture == 0 ? 0d : file2NumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double supportPairFile = numberOfAllPullrequestFuture == 0 ? 0d : pairFileNumberOfPullrequestOfPairFuture.doubleValue() / numberOfAllPullrequestFuture.doubleValue();
            Double confidence = supportFile == 0 ? 0d : supportPairFile / supportFile;
            Double confidence2 = supportFile2 == 0 ? 0d : supportPairFile / supportFile2;
            Double lift = supportFile * supportFile2 == 0 ? 0d : supportPairFile / (supportFile * supportFile2);
            Double conviction = 1 - confidence == 0 ? 0d : (1 - supportFile2) / (1 - confidence);
            Double conviction2 = 1 - confidence2 == 0 ? 0d : (1 - supportFile2) / (1 - confidence2);

            pairFile.addMetrics(supportFile, supportFile2, supportPairFile, confidence, confidence2, lift, conviction, conviction2);
        }

        EntityMatrix matrix = new EntityMatrix();
        matrix.setNodes(objectsToNodes(pairFileMetrics));
        
        pairFileMetrics.clear();
        dao.clearCache(true);
        
        saveMatrix(matrix);
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
            dao.insert(node);
            it.remove();
            dao.getEntityManager().getEntityManagerFactory().getCache().evict(node.getClass());
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

    private long calculeUpdates(String file, String file2, Date beginDate, Date endDate) {
        String jpql = "SELECT COUNT(pp) "
                + "FROM "
                + "EntityPullRequest pp "
                + "WHERE "
                + "pp.repository = :repo AND "
                + "pp.createdAt BETWEEN :beginDate AND :endDate ";

        if (file != null) {
            jpql += " AND EXISTS (SELECT p FROM EntityPullRequest p JOIN p.repositoryCommits r JOIN r.files f WHERE p = pp AND f.filename = :fileName) ";
        }

        if (file2 != null) {
            jpql += " AND EXISTS (SELECT p2 FROM EntityPullRequest p2 JOIN p2.repositoryCommits r2 JOIN r2.files f2 WHERE p2 = pp AND f2.filename = :fileName2) ";
        }

        String[] bdParams = new String[]{
            "repo",
            file != null ? "fileName" : "#none#",
            file2 != null ? "fileName2" : "#none#",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            repository,
            file,
            file2,
            beginDate,
            endDate
        };

        Long count = dao.selectOneWithParams(jpql, bdParams, bdObjects);

        return count != null ? count : 0l;
    }

    private Double fixNanValue(Double value) {
        if (value.isNaN()) {
            return 0d;
        }
        return value;
    }

    private static <V, E> Transformer<E, ? extends Number> createWeigthTransformer(final Graph<V, E> graph, final Map<E, ? extends Number> edgeWeigth) {
        Transformer<E, ? extends Number> edgeWeigthTransformer = new Transformer<E, Number>() {
            @Override
            public Number transform(E edge) {
                Number num = edgeWeigth.get(edge);
                return num != null ? num : 0;
            }
        };
        return edgeWeigthTransformer;
    }
}
