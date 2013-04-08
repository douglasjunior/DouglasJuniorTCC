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
public class AuxUserMetrics {

    private String user;
    private double[] metrics;

    public AuxUserMetrics(String user, double... metrics) {
        this.user = user;
        this.metrics = metrics;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public double[] getMetrics() {
        return metrics;
    }

    public void setMetrics(double[] metrics) {
        this.metrics = metrics;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(user);
        for (double m : metrics) {
            sb.append(";");
            sb.append(Util.tratarDoubleParaString(m));
        }
        return sb.toString();
    }
}
