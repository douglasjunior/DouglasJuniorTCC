/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.services.metric.auxiliary;

import java.util.Objects;

/**
 *
 * @author douglas
 */
public class AuxUserMilestoneCount {

    private String user;
    private String milestone;
    private Double count;

    public AuxUserMilestoneCount(String user, String milestone, Double count) {
        this.user = user;
        this.milestone = milestone;
        this.count = count;
    }

    public String getMilestone() {
        return milestone;
    }

    public void setMilestone(String milestone) {
        this.milestone = milestone;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Double getCount() {
        return count;
    }

    public void setCount(Double count) {
        this.count = count;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof AuxUserMilestoneCount) {
            AuxUserMilestoneCount other = (AuxUserMilestoneCount) o;
            if (this.user.equals(other.user)
                    && this.milestone.equals(other.milestone)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.user);
        hash = 97 * hash + Objects.hashCode(this.milestone);
        return hash;
    }

    public void increment() {
        count++;
    }

    @Override
    public String toString() {
        return user + ";" + count;
    }
}
