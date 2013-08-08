/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Yifeng
 */
public class VisualFaculty implements Runnable {
    public static List<String> names;
    public static ListEngine facultyEngine;
    public static int count = 0;
    public String url;

    public VisualFaculty(String url) {
        this.url = url;
    }
    
    public static synchronized void increment() {
        count++;
    }
    
    
    static {
        try {
            names = Utility.getKeywords("Group/Names.txt");
            facultyEngine = new ListEngine(names, new ArrayList<String>(),new ArrayList<String>(),new ArrayList<String>());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(VisualFaculty.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VisualFaculty.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean identify(String url) throws IOException{
        ArrayList<Combo> combos = CSSModel.getCombos(url);
        ArrayList<String> rows = facultyEngine.getNames(url, combos);
        if (rows.size() > 0) {
            return true;
        } else {
            return false;
        }
    }
    
    public void run() {
        if (FacultyNav.identify(url)) {
            System.out.println("====================================");
            increment();
        }
    }
    
    public static void main(String[] args) throws MalformedURLException, IOException, InterruptedException {
        List<String> lines = Utility.getKeywords("Group/TestFaculty.txt");
        int cpus = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(cpus);
        for (int r = 0; r < lines.size(); r++) {
            Runnable task = new VisualFaculty(lines.get(r));
            executor.execute(task);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            System.out.println("Status: " + executor.getCompletedTaskCount() + "/" + executor.getTaskCount() + " threads are completed ...");
            System.out.println(count+" over "+lines.size()+" have been correctly identified so far.");
            Thread.sleep(10000);
        }
        System.out.println(count+" over "+lines.size()+" are correctly identified.");
    }
}
