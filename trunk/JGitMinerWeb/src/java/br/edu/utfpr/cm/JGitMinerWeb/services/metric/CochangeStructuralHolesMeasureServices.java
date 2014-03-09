package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileCountSum;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.auxiliary.AuxFileFile;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.algorithms.metrics.StructuralHoles;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        
        final Map<String, Integer> edgeWeigth = new HashMap<>(getMatrix().getNodes().size());
        final Map<AuxFileFile, Set<String>> commitersPairFile = new HashMap<>();
        final Map<AuxFileFile, Integer> pairFileCommitCount = new HashMap<>();
        
        Set<AuxFileFile> pairFiles = new HashSet<>();
        Map<String, AuxFileFile> edgeFiles = new HashMap<>();
        for (int i = 0; i < getMatrix().getNodes().size(); i++) {
            EntityMatrixNode node = getMatrix().getNodes().get(i);
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);
            
            AuxFileFile pairFile = new AuxFileFile(columns[1], columns[2]);
            String edgeName = pairFile + "(" + i + ")";
            
            pairFiles.add(pairFile);
            edgeFiles.put(edgeName, pairFile);
            edgeWeigth.put(edgeName, Integer.valueOf(columns[4]));
            
            if (pairFileCommitCount.containsKey(pairFile)) {
                pairFileCommitCount.put(pairFile, Integer.valueOf(columns[4]) + edgeWeigth.get(edgeName));
            } else {
                pairFileCommitCount.put(pairFile, Integer.valueOf(columns[4]));
            }
            
            String commiter1 = columns[0];
            String commiter2 = columns[3];
            
            // extract all distinct developer that commit a pair of file
            if (commitersPairFile.containsKey(pairFile)) {
                Set<String> commiters = commitersPairFile.get(pairFile);
                commiters.add(commiter1);
                commiters.add(commiter2);
            } else {
                Set<String> commiters = new HashSet<>();
                commiters.add(commiter1);
                commiters.add(commiter2);
                commitersPairFile.put(pairFile, commiters);
            }
            
            graphMulti.addEdge(edgeName, commiter1, commiter2, EdgeType.UNDIRECTED);
        }

        Transformer<String, Integer> edgeWeigthTransformer = new Transformer<String, Integer>() {
            @Override public Integer transform(String edge) {
                return edgeWeigth.get(edge);
            }
        };
        
        StructuralHoles<String, String> structuralHoles = new StructuralHoles<>(graphMulti, edgeWeigthTransformer);
        Map<String, AuxFileMetrics> commitersMetrics = new HashMap<>();
        
        // Calcules structural holes metrics for each developer
        // on the cochange network based.
        // The network is contructed as follow: 
        // If developer A and developer B commits a pair file PF,
        // then they are connected in network.
        for (String commiter : graphMulti.getVertices()) {
            double efficiency = structuralHoles.efficiency(commiter);
            double effectiveSize = structuralHoles.effectiveSize(commiter);
            double constraint = structuralHoles.constraint(commiter);
            double hierarchy = structuralHoles.hierarchy(commiter);

            commitersMetrics.put(commiter, new AuxFileMetrics(commiter,
                efficiency, effectiveSize, constraint, hierarchy));
        }
        
        List<AuxFileFileMetrics> structuralHolesMetrics = new ArrayList<>(pairFiles.size());
        for (AuxFileFile pairFile : pairFiles) {
            double efficiencyMax = 0, efficiencyAvg, efficiencySum = 0;
            double effectiveSizeMax = 0, effectiveSizeAvg, effectiveSizeSum = 0;
            double constraintMax = 0, constraintAvg, constraintSum = 0;
            double hierarchyMax = 0, hierarchyAvg, hierarchySum = 0;
            long developers = 0;
            for (String commiter : commitersPairFile.get(pairFile)) {
                AuxFileMetrics commiterMetric = commitersMetrics.get(commiter);
                double efficiency = commiterMetric.getMetrics()[0];
                double effectiveSize = commiterMetric.getMetrics()[1];
                double constraint = commiterMetric.getMetrics()[2];
                double hierarchy = commiterMetric.getMetrics()[3];
                
                efficiencySum += efficiency;
                effectiveSizeSum += effectiveSize;
                constraintSum += constraint;
                hierarchySum += hierarchy;
                
                efficiencyMax = calculeMax(efficiencyMax, efficiency);
                effectiveSizeMax = calculeMax(effectiveSizeMax, effectiveSize);
                constraintMax = calculeMax(constraintMax, constraint);
                hierarchyMax = calculeMax(hierarchyMax, hierarchy);
                
                developers++;
            }
            efficiencyAvg = efficiencySum / developers;
            effectiveSizeAvg = effectiveSizeSum / developers;
            constraintAvg = constraintSum / developers;
            hierarchyAvg = hierarchySum / developers;
            
            structuralHolesMetrics.add(
                    new AuxFileFileMetrics(
                        pairFile.getFileName(), pairFile.getFileName2(),
                        efficiencySum, efficiencyAvg, efficiencyMax, 
                        effectiveSizeSum, effectiveSizeAvg, efficiencyMax,
                        constraintSum, constraintAvg, constraintMax,
                        hierarchySum, hierarchyAvg, hierarchyMax,
                        developers, pairFileCommitCount.get(pairFile)));
        }
        
        addToEntityMetricNodeList(structuralHolesMetrics);
    }

    @Override
    public String getHeadCSV() {
        return "file1;file2;"
                + "efficiencySum;efficiencyAvg;efficiencyMax;"
                + "effectiveSizeSum;effectiveSizeAvg;effectiveSizeMax;"
                + "constraintSum;constraintAvg;constraintMax;"
                + "hierarchySum;hierarchyAvg;hierarchyMax;"
                + "developers;commits"
                ;
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(
                UserModifySamePairOfFileInDateServices.class.getName()
        );
    }

    private Double calculeMax(Double v, Double vMax) {
        if (vMax < v) {
            return v;
        }
        return vMax;
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
