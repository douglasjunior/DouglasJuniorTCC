/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import br.edu.utfpr.cm.JGitMinerWeb.util.Util;

/**
 *
 * @author douglas
 */
public class AuxFileFileMetrics {

    private String file;
    private String file2;
    private double[] metrics;

    public AuxFileFileMetrics(String file,String file2, double... metrics) {
        this.file = file;
        this.file2 = file2;
        this.metrics = metrics;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public double[] getMetrics() {
        return metrics;
    }

    public void setMetrics(double[] metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(file + ";" + file2);
        for (double m : metrics) {
            sb.append(";");
            sb.append(Util.tratarDoubleParaString(m));
        }
        return sb.toString();
    }
}
