/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Douglas
 */
public class OutLog implements Serializable {

    private List<String> log;
    private String currentProcess;
    private static SimpleDateFormat dateFormat;

    static {
        dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    }

    public OutLog() {
        log = new ArrayList<>();
    }

    public String getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(String currentProcess) {
        this.currentProcess = currentProcess;
        printLog(currentProcess);
    }

    public StringBuffer getLog() {
        StringBuffer l = new StringBuffer();
        synchronized (log) {
            for (int i = log.size() - 1; i >= 0; i--) {
                l.append(log.get(i) + "\n");
            }
        }
        return l;
    }

    public String getSingleLog() {
        StringBuilder l = new StringBuilder();
        synchronized (log) {
            for (int i = log.size() - 1; i >= log.size() - 100 &&  i >= 0; i--) {
                l.append(log.get(i));
            }
        }
        return l.toString();
    }

    public void printLog(String logStr) {
        logStr = dateFormat.format(new Date()) + ": " + logStr + "\n";
        synchronized (log) {
            log.add(logStr);
        }
        System.out.println(logStr);
    }

    public void resetLog() {
        synchronized (log) {
            log.clear();
        }
        currentProcess = "";
    }
}
