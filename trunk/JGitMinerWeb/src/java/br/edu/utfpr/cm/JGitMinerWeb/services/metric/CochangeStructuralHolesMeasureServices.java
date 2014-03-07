package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileCountSum;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.algorithms.metrics.StructuralHoles;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CochangeStructuralHolesMeasureServices extends AbstractMetricServices {

    private EntityRepository repository;

    public CochangeStructuralHolesMeasureServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CochangeStructuralHolesMeasureServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
    }

    @Override
    public void run() {
        System.out.println(params);

        System.out.println(getMatrix().getClassServicesName());

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matrix gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matrix.");
        }

        System.out.println("Selecionado matrix com " + getMatrix().getNodes().size() + " nodes.");
        
        final UndirectedSparseMultigraph<String, String> graphMulti = new UndirectedSparseMultigraph<>();
        final UndirectedSparseGraph<String, String> graph = new UndirectedSparseGraph<>();
        
        final Map<String, Double> edgeWeigth = new HashMap<>(getMatrix().getNodes().size());
        List<String> files = new ArrayList<>();
        List<String> commiters = new ArrayList<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            colectFile(files, columns[2]);
            colectCommiter(commiters, columns[0]);
            edgeWeigth.put(columns[2] + "(" + i + ")", Double.valueOf(columns[4]));
            graph.addEdge(
                    columns[2] + "(" + i + ")",
                    columns[1],
                    columns[0],
                    EdgeType.UNDIRECTED);
            graphMulti.addEdge(
                    columns[2] + "(" + i + ")",
                    columns[1],
                    columns[0],
                    EdgeType.UNDIRECTED);
        }

        Transformer<String, Number> edgeWeight = new Transformer<String, Number>() {

            @Override
            public Number transform(String edge) {
                return edgeWeigth.get(edge);
            }
        };
        StructuralHoles<String, String> structuralHoles = new StructuralHoles<>(graphMulti, edgeWeight);

        List<AuxFileMetrics> fileMetrics = new ArrayList<>();
                
        for (String commiter : commiters) {
            double efficiency = structuralHoles.efficiency(commiter);
            double effectiveSize = structuralHoles.effectiveSize(commiter);
            double constraint = structuralHoles.constraint(commiter);
            double aggregateConstraint = structuralHoles.aggregateConstraint(commiter);
            double hierarchy = structuralHoles.hierarchy(commiter);
            
            fileMetrics.add(new AuxFileMetrics(commiter,
                    efficiency, effectiveSize, constraint, 
                    aggregateConstraint, hierarchy));
            
        }
        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;"
                + "efficiency;effectiveSize;constraint;"
                + "aggregateConstraint;hierarchy"
                ;
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(
                UserModifySamePairOfFileInDateServices.class.getName()
        );
    }

    private void colectFile(List<String> files, String file) {
        if (!files.contains(file)) {
            files.add(file);
        }
    }

    private void colectCommiter(List<String> commiters, String commiter) {
        if (!commiters.contains(commiter)) {
            commiters.add(commiter);
        }
    }

    private Double calculeMax(Double v, Double vMax) {
        if (vMax < v) {
            return v;
        }
        return vMax;
    }

    private AuxFileCountSum calculeCodeChurnAndUpdates(String fileName, Date beginDate, Date endDate) {
        String jpql = "SELECT NEW " + AuxFileCountSum.class.getName() + "(f.filename, COUNT(f), SUM(f.changes)) "
                + "FROM "
                + "EntityRepositoryCommit rc JOIN rc.files f  "
                + "WHERE "
                + "rc.repository = :repo AND "
                + "rc.commit.committer.dateCommitUser BETWEEN :beginDate AND :endDate AND "
                + "f.filename = :fileName "
                + "GROUP BY f.filename";

        String[] bdParams = new String[]{
            "repo",
            "fileName",
            "beginDate",
            "endDate"
        };
        Object[] bdObjects = new Object[]{
            repository,
            fileName,
            beginDate,
            endDate
        };

        List<AuxFileCountSum> result = dao.selectWithParams(jpql, bdParams, bdObjects);

        if (!result.isEmpty()) {
            return result.get(0);
        } else {
            return new AuxFileCountSum(fileName, 0, 0);
        }
    }

    private EntityRepository getRepository() {
        String[] repoStr = getMatrix().getRepository().split("/");
        List<EntityRepository> repos = dao.executeNamedQueryWithParams(
                "Repository.findByNameAndOwner",
                new String[]{"login", "name"},
                new Object[]{repoStr[0], repoStr[1]});
        if (repos.size() == 1) {
            return repos.get(0);
        }
        return null;
    }
}
