/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.edu.utfpr.cm.JGitMinerWeb.util;

import java.util.Date;

/**
 *
 * @author Douglas
 */
public class OutLog {

    private StringBuilder log;
    private String currentProcess;

    public OutLog() {
        log = new StringBuilder();
    }

    public String getCurrentProcess() {
        return currentProcess;
    }

    public void setCurrentProcess(String currentProcess) {
        currentProcess = currentProcess;
        printLog(currentProcess);
    }

    public String getLog() {
        return log.toString();
    }

    public void printLog(String logStr) {
        log.insert(0, new Date() + ": " + logStr + "\n");
        System.out.println(logStr);
    }

    public void resetLog() {
        log = new StringBuilder();
        currentProcess = "";
    }
}
