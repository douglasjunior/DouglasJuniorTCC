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
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CochangeEgoNetworkMeasureServices extends AbstractMetricServices {

    private EntityRepository repository;

    public CochangeEgoNetworkMeasureServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public CochangeEgoNetworkMeasureServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
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

        // in undirected network, these measures are same for all
        List<AuxFileMetrics> fileMetrics = new ArrayList<>();
        int size = graphMulti.getVertexCount(); // The number of nodes in the ego network [#16, p.113, #23, p.5]
        int pairs = size * (size - 1); // Number of possible directed edges in the ego network [#23, p.5]
        int ties = graphMulti.getEdgeCount(); // Number of edges in the ego network [#16, p.113]
        int density = pairs == 0 ? 0 : ties / pairs; // Proportion of possible ties that actually are present (Ties/Pairs) [#16, p.113, #23, p.5]
                
        for (String commiter : commiters) {
            int inDegree = graphMulti.inDegree(commiter);
            int outDegree = graphMulti.outDegree(commiter);
            int inOutDegree = inDegree + outDegree;
            int weakComponent = size - graphMulti.getNeighborCount(commiter); // weakly connected (acho que é nós ligados indiretamente) [#16, p.113]
            int normalizedWeakComponent = size == 0 ? 0 : weakComponent / size;
            
            int twoStepReach = graphMulti.getNeighborCount(commiter); // The proportion of nodes that are within two hops of ego [#16, p.113]
            for (String neighboor : graphMulti.getNeighbors(commiter)) {
                twoStepReach += graphMulti.getNeighborCount(neighboor);
            }
            int reachEfficiency = size == 0 ? 0 : twoStepReach / size;
            fileMetrics.add(new AuxFileMetrics(commiter,
                    inDegree, outDegree, inOutDegree, 
                    density, weakComponent, normalizedWeakComponent, 
                    twoStepReach, reachEfficiency,
                    size, pairs, ties));
            
        }
        addToEntityMetricNodeList(fileMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file;"
                + "inDregree;outDegree;inOutDegree;"
                + "density;weakComponent;normalizedWeakComponent;"
                + "twoStepReach;reachEfficiency;"
                + "size;pairs;ties;"
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
