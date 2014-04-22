/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric;

import br.edu.utfpr.cm.JGitMinerWeb.dao.GenericDao;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrix;
import br.edu.utfpr.cm.JGitMinerWeb.model.matrix.EntityMatrixNode;
import br.edu.utfpr.cm.JGitMinerWeb.model.miner.EntityRepository;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInAllDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserCommentedSamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.matrix.UserModifySamePairOfFileInDateServices;
import br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary.AuxFileFileMetrics;
import br.edu.utfpr.cm.JGitMinerWeb.util.JsfUtil;
import br.edu.utfpr.cm.JGitMinerWeb.util.OutLog;
import edu.uci.ics.jung.graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections15.Transformer;

/**
 *
 * @author Douglas
 */
public class PairFileSupportConfidenceLiftConvictionInDateServices extends AbstractMetricServices {

    private EntityRepository repository;

    public PairFileSupportConfidenceLiftConvictionInDateServices(GenericDao dao, OutLog out) {
        super(dao, out);
    }

    public PairFileSupportConfidenceLiftConvictionInDateServices(GenericDao dao, EntityMatrix matrix, Map params, OutLog out) {
        super(dao, matrix, params, out);
    }

    private Integer getIntervalOfMonths() {
        return getIntegerParam("intervalOfMonths");
    }

    private Date getBeginDate() {
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            return getDateParam("matrixBeginDate");
        }
        return getDateParam("beginDate");
    }

    private Date getEndDate() {
        if (getMatrix().getClassServicesName().equals(UserCommentedSamePairOfFileInAllDateServices.class.getName())) {
            return getDateParam("matrixEndDate");
        }
        return getDateParam("endDate");
    }

    public Date getFutureBeginDate() {
        return getDateParam("futureBeginDate");
    }

    public Date getFutureEndDate() {
        return getDateParam("futureEndDate");
    }

    @Override
    public void run() {

        if (getMatrix() == null
                || !getAvailableMatricesPermitted().contains(getMatrix().getClassServicesName())) {
            throw new IllegalArgumentException("Selecione uma matriz gerada pelo Services: " + getAvailableMatricesPermitted());
        }

        repository = getRepository();

        if (repository == null) {
            throw new IllegalArgumentException("Não foi possível encontrar o repositório utilizado nesta matriz.");
        }

        Date futureBeginDate = getFutureBeginDate();
        Date futureEndDate = getFutureEndDate();

        if (futureBeginDate == null && futureEndDate == null) {
            if (getIntervalOfMonths() == null || getIntervalOfMonths() == 0) {
                throw new IllegalArgumentException("A matriz selecionada não possui parâmetro de Interval Of Months, informe Future Begin Date e Future End Date.");
            }
            futureBeginDate = (Date) getEndDate().clone();
            Calendar cal = Calendar.getInstance();
            cal.setTime((Date) futureBeginDate.clone());
            cal.add(Calendar.MONTH, getIntervalOfMonths());
            futureEndDate = cal.getTime();
        }

        params.put("futureBeginDate", futureBeginDate);
        params.put("futureEndDate", futureEndDate);

        // user | file | file2 | user2 | weigth
        out.printLog("Iniciado calculo da métrica de matriz com " + getMatrix().getNodes().size() + " nodes. Parametros: " + params);

        out.printLog("Iniciando preenchimento da lista de pares.");
        Collection<AuxFileFileMetrics> pairFileMetrics = new HashSet<>();

        for (EntityMatrixNode node : getMatrix().getNodes()) {
            String[] columns = node.getLine().split(JsfUtil.TOKEN_SEPARATOR);

            AuxFileFileMetrics pairFile = new AuxFileFileMetrics(columns[1], columns[2]);
            pairFileMetrics.add(pairFile);
        }
        out.printLog(pairFileMetrics.size() + " pares encontrados.");

        getMatrix().getNodes().clear();
        pairFileMetrics = new ArrayList<>(pairFileMetrics);

        out.printLog("Iniciando cálculo do support, confidence, lift e conviction.");
        int i = 0;
        for (AuxFileFileMetrics pairFile : pairFileMetrics) {
            if (i % 10000 == 0) {
                try {
                    System.out.println("Dormindo por 5 segundos.");
                    System.gc();
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            Long pairFileNumberOfPullrequestOfPair = calculeUpdates(pairFile.getFile(), pairFile.getFile2(), getBeginDate(), getEndDate());
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
            System.out.println(i + "/" + pairFileMetrics.size());
            i++;
        }

        out.printLog("Iniciando conversão dos nós.");
        addToEntityMetricNodeList(pairFileMetrics);
        pairFileMetrics.clear();

        System.gc();
    }

    @Override
    public String getHeadCSV() {
        return "file;file2;"
                + "pairFileCochange;pairFileCochangeFuture;fileChangeFuture;file2ChangeFuture;allPullrequestFuture;"
                + "supportFile;supportFile2;supportPairFile;confidence;confidence2;lift;conviction;conviction2";
    }

    @Override
    public List<String> getAvailableMatricesPermitted() {
        return Arrays.asList(UserModifySamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInDateServices.class.getName(),
                UserCommentedSamePairOfFileInAllDateServices.class.getName());
    }

    private long calculeUpdates(String file, String file2, Date beginDate, Date endDate) {
        List selectParams = new ArrayList(); 
        
        String jpql = " SELECT count(pul.*) "
                + " FROM gitpullrequest pul "
                + " where pul.repository_id = ? "
                + "   and pul.createdat between ? and ? ";
        
        selectParams.add(repository.getId());
        selectParams.add(beginDate);
        selectParams.add(endDate);
        
        if (file != null) {
            jpql += "  and exists  "
                    + "  ( select f.* "
                    + "  from gitpullrequest_gitrepositorycommit r, "
                    + "       gitcommitfile f  "
                    + "  where r.entitypullrequest_id = pul.id "
                    + "    and f.repositorycommit_id = r.repositorycommits_id "
                    + "    and f.filename = ? ) ";
            selectParams.add(file);
        }

        if (file2 != null) {
            jpql += "  and exists  "
                    + "  ( select f2.*  "
                    + "  from gitpullrequest_gitrepositorycommit r2, "
                    + "       gitcommitfile f2  "
                    + "  where r2.entitypullrequest_id = pul.id "
                    + "    and f2.repositorycommit_id = r2.repositorycommits_id "
                    + "    and f2.filename = ? ) ";
            selectParams.add(file2);
        }

         Long count = dao.selectNativeOneWithParams(jpql, selectParams.toArray());

        return count != null ? count : 0l;
    }

    private Double fixNanValue(Double value) {
        if (value.isNaN()) {
            return 0d;
        }
        return value;
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
