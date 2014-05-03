package br.edu.utfpr.cm.JGitMinerWeb.util;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * Export tool for JUNG graph.
 *
 * @author Rodrigo T. Kuroda
 */
public class JungExport {

    public static void exportToImage(
            Graph<String, String> graph, String path, String filename) {
        try {
            Dimension d = new Dimension(1920, 1080);
            VisualizationViewer<String, String> vv = 
                    new VisualizationViewer<>(new FRLayout(graph, d));
            // Create the VisualizationImageServer
            // vv is the VisualizationViewer containing my graph
            VisualizationImageServer<String, String> vis
                    = new VisualizationImageServer<>(vv.getGraphLayout(),
                            vv.getGraphLayout().getSize());

            // Configure the VisualizationImageServer the same way
            // you did your VisualizationViewer. In my case e.g.
            vis.setBackground(Color.WHITE);

            // vis.getRenderContext().setEdgeLabelTransformer(NOPTransformer.INSTANCE);
            vis.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<String, String>());
            vis.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
            vis.getRenderer().getVertexLabelRenderer()
                    .setPosition(Renderer.VertexLabel.Position.CNTR);

            // Create the buffered image
            BufferedImage image = (BufferedImage) vis.getImage(
                    new Point2D.Double(vv.getGraphLayout().getSize().getWidth() / 2,
                            vv.getGraphLayout().getSize().getHeight() / 2),
                    new Dimension(vv.getGraphLayout().getSize()));

            // Write image to a png file
            File outputfile = new File(path, filename + ".png");
            outputfile.mkdirs();

            try {
                System.out.println("Saving graph image in " + outputfile.getAbsolutePath());
                ImageIO.write(image, "png", outputfile);
            } catch (IOException e) {
                // Exception handling
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
