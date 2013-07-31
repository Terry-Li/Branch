/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Terry
 */
public class DepartmentNav{
    private static ListEngine engine;
    
    static {
        try {
            ArrayList<String> positives = Utility.getKeywords("Group/departments.txt");
            ArrayList<String> negatives = Utility.getKeywords("Group/Negatives.txt");
            ArrayList<String> degrees = Utility.getKeywords("Group/degrees.txt");
            ArrayList<String> urlNegatives = Utility.getKeywords("Group/URLNegatives.txt");
            engine = new ListEngine(positives, negatives, degrees, urlNegatives);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean contains(ArrayList<String> depts, ArrayList<String> parents) {
        for (String dept: depts) {
            String deptURL = dept.split("==")[1].replaceAll("\\/$", "");
            for (String school: parents) {
                String schoolURL = school.split("==")[1].replaceAll("\\/$", "");
                if (schoolURL.equalsIgnoreCase(deptURL)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public static SemanticList getDeptsResult(Link url, Set<String> visited, ArrayList<String> parents) {
        ArrayList<SemanticList> lists = new ArrayList<SemanticList>();
        SemanticList schools = null;
        ArrayList<Link> deptLinks = Utility.getDeptLinks(url, visited);
        //System.out.println(deptLinks.size());
        for (Link link : deptLinks) {
            try {
                System.out.println(link.url);
                schools = engine.getSchools(link,2);
                if (schools != null && !contains(schools.list, parents)) {
                    lists.add(schools);
                }
            } catch (IOException ex) {
                Logger.getLogger(DepartmentNav.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (lists.size() > 0) {
            for (SemanticList list : lists) {
                String head = list.head;
                String title = list.title;
                if ((head != null &&head.toLowerCase().contains("departments")) || (title !=null && title.toLowerCase().contains("departments"))) {
                    return list;
                }
            }
            return lists.get(0);
        } else {
            return null;
        }
    }
    
}