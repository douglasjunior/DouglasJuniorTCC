/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Douglas
 */
public class OutLog implements Serializable  {

    private StringBuffer log;
    private String currentProcess;
    private static SimpleDateFormat dateFormat;

    public OutLog() {
        log = new StringBuffer();
        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        }
    }

    public String getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(String currentProcess) {
        this.currentProcess = currentProcess;
        printLog(currentProcess);
    }

    public StringBuffer getLog() {
        return log;
    }

    public String getSingleLog() {
        if (log.length() > 99999) {
            return log.substring(0, 99999);
        }
        return log.toString();
    }

    public void printLog(String logStr) {
        logStr = dateFormat.format(new Date()) + ": " + logStr + "\n";
        log.insert(0, logStr);
        System.out.println(logStr);
    }

    public void resetLog() {
        log = new StringBuffer();
        currentProcess = "";
    }
}
