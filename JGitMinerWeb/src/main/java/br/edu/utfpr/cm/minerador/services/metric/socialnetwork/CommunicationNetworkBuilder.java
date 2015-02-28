package br.edu.utfpr.cm.minerador.services.metric.socialnetwork;

import br.edu.utfpr.cm.JGitMinerWeb.dao.BichoDAO;
import br.edu.utfpr.cm.JGitMinerWeb.util.PairUtils;
import br.edu.utfpr.cm.minerador.services.matrix.model.Commenter;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class CommunicationNetworkBuilder {

    private final BichoDAO bichoDAO;
    private final Set<Integer> noCommenters;

    public CommunicationNetworkBuilder(BichoDAO bichoDAO) {
        this.bichoDAO = bichoDAO;
        this.noCommenters = new HashSet<>();
    }

    public Network<String, String> buildDirectedWeightedNetwork(Integer issue) {
        final List<Commenter> commenters = bichoDAO.selectCommentersByIssueOrderBySubmissionDate(issue);
        final Graph<String, String> graphMulti = new DirectedSparseGraph<>();
        final Map<String, Integer> edgesWeigth = new HashMap<>();

        if (commenters.isEmpty()) {
            noCommenters.add(issue);
        } else if (commenters.size() == 1) {
            graphMulti.addVertex(commenters.get(0).getName());
        } else {
            Map<UserPairDirectionalWeighted, UserPairDirectionalWeighted> pairCommenters
                    = PairUtils.buildUserPairDirectionalWeighted(commenters);

            if (pairCommenters.isEmpty()) {
                graphMulti.addVertex(commenters.get(0).getName());
            } else {
                for (UserPairDirectionalWeighted userPair : pairCommenters.keySet()) {
                    // adiciona conforme o peso
                    //  String edgeName = pairFile.getFileName() + "-" + pairFile.getFileName2() + "-" + i;

                    /* Sum commit for each pair file that the pair devCommentter has commited. */
                    // user > user2 - directed edge
                    if (edgesWeigth.containsKey(userPair.toStringDirectional())) {
                        // edgeName = user + user2
                        edgesWeigth.put(userPair.toStringDirectional(), edgesWeigth.get(userPair.toStringDirectional()) + userPair.getWeigth());
                            //            // for undirectional globalGraph
                        //            } else if (edgesWeigth.containsKey(pairUser.toStringUser2AndUser())) {
                        //                // edgeName = user2 + user - undirected edge
                        //                edgesWeigth.put(pairUser.toStringUser2AndUser(), edgesWeigth.get(pairUser.toStringUser2AndUser()) + weight);
                    } else {
                        edgesWeigth.put(userPair.toStringDirectional(), userPair.getWeigth());
                    }

//                    if (!globalGraph.containsVertex(userPair.getUser())
//                            || !globalGraph.containsVertex(userPair.getUser2())
//                            || !globalGraph.containsEdge(userPair.toStringDirectional())) {
//                        globalGraph.addEdge(userPair.toStringDirectional(), userPair.getUser(), userPair.getUser2(), EdgeType.DIRECTED);
//                    }

                    graphMulti.addEdge(userPair.toStringDirectional(), userPair.getUser(), userPair.getUser2(), EdgeType.DIRECTED);
                }
            }
        }
        Network<String, String> network = new Network<>(graphMulti, edgesWeigth, commenters);

        return network;
    }
}
