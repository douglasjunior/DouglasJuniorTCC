
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.ego;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import java.util.Map;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Rodrigo T. Kuroda
 */
public class EgoNetworkExtractorTest {
    
    private Graph<String, String> instance;
    private Graph<String, String> instance2;
    
    @Before
    public void setup() {
        instance = new UndirectedSparseGraph<>();
        instance.addVertex("D8");
        instance.addVertex("D9");
        instance.addVertex("D10");
        instance.addVertex("D11");
        instance.addVertex("D12");
        instance.addVertex("D13");
        instance.addVertex("D14");
        instance.addVertex("D15");
        instance.addVertex("D1");
        instance.addVertex("D2");
        instance.addVertex("D3");
        instance.addVertex("D4");
        instance.addVertex("D5");
        instance.addEdge("D1-D2", "D1", "D2");
        instance.addEdge("D1-D3", "D1", "D3");
        instance.addEdge("D1-D4", "D1", "D4");
        instance.addEdge("D1-D5", "D1", "D5");
        instance.addEdge("D4-D5", "D4", "D5");
        instance.addEdge("D4-D6", "D4", "D6");
        instance.addEdge("D1-D6", "D1", "D6");
        instance.addEdge("D1-D7", "D1", "D7");
        // A
        instance.addEdge("D4-D8", "D4", "D8");
        instance.addEdge("D4-D9", "D4", "D9");
        instance.addEdge("D5-D8", "D5", "D8");
        instance.addEdge("D5-D9", "D5", "D9");
        instance.addEdge("D8-D9", "D8", "D9");
        // B
        instance.addEdge("D2-D10", "D2", "D10");
        instance.addEdge("D2-D11", "D2", "D11");
        instance.addEdge("D10-D12", "D10", "D12");
        instance.addEdge("D2-D12", "D2", "D12");
        // C
        instance.addEdge("D3-D13", "D3", "D13");
        instance.addEdge("D3-D14", "D3", "D14");
        instance.addEdge("D14-D15", "D14", "D15");
        instance.addEdge("D13-D15", "D13", "D15");
        
    }
    
    @After
    public void tearDown() {
        instance = null;
    }
    
    @Test
    public void testEgoNetworkExtractedVertices() {
        Map<String, Graph<String, String>> egoNetworks = EgoNetworkExtractor.extractEgoNetwork(instance);
        
        String[] d1EgoNetworkVertices = new String[] 
            {"D1", "D2", "D3", "D4", "D5", "D6", "D7"};
        
        Assert.assertEquals(7, egoNetworks.get("D1").getVertexCount());
        Assert.assertThat(egoNetworks.get("D1").getVertices(), 
                CoreMatchers.hasItems(d1EgoNetworkVertices));
    }
    
    @Test
    public void testEgoNetworkExtractedEdges() {
        Map<String, Graph<String, String>> egoNetworks = EgoNetworkExtractor.extractEgoNetwork(instance);
        
        String[] d1EgoNetworkEdges = new String[] 
            {"D1-D2", "D1-D3", "D1-D4", "D1-D5", 
             "D1-D6", "D1-D7", "D4-D5", "D4-D6"};
        
        Assert.assertEquals(8, egoNetworks.get("D1").getEdgeCount());
        Assert.assertThat(egoNetworks.get("D1").getEdges(), 
                CoreMatchers.hasItems(d1EgoNetworkEdges));
    }
}
