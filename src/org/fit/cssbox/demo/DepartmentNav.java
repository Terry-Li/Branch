/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
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
            List<String> positives = Utility.getKeywords("Group/departments.txt");
            List<String> negatives = Utility.getKeywords("Group/Negatives.txt");
            List<String> degrees = Utility.getKeywords("Group/degrees.txt");
            List<String> urlNegatives = Utility.getKeywords("Group/URLNegatives.txt");
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
    
    public static ArrayList<Link> getDeptLinks(Link link, Set<String> visited, String domainName){
        ArrayList<Link> links = new ArrayList<Link>();
        links.add(link);
        ArrayList<Link> parents = sortLinks(getNavLinks(link,visited, domainName));
        links.addAll(parents);
        for (Link parent: parents) {
            links.addAll(sortLinks(getNavLinks(parent,visited, domainName)));
        }  
        //for (String link: links) System.out.println(link);
        return links;
    }
    
    public static ArrayList<Link> sortLinks(Set<Link> links){
        ArrayList<Link> sort = new ArrayList<Link>();
        ArrayList<Link> medPrio = new ArrayList<Link>();
        ArrayList<Link> lowPrio = new ArrayList<Link>();
        for (Link link: links){
            if (link.url.toLowerCase().contains("departments")){
                sort.add(link);
            } else if (!link.url.toLowerCase().contains("graduate") || !link.url.toLowerCase().contains("undergrad")
                    || !link.url.toLowerCase().contains("special") || !link.url.toLowerCase().contains("summer")
                    || !link.url.toLowerCase().contains("online") || !link.url.toLowerCase().contains("international")
                    || !link.url.toLowerCase().contains("affiliate") || !link.url.toLowerCase().contains("additional")) {
                medPrio.add(link);
            } else {
                lowPrio.add(link);
            }
        }
        sort.addAll(medPrio);
        sort.addAll(lowPrio);
        //for (String str: sort) {System.out.println(str);}
        return sort;
    }
    
    public static Set<Link> getNavLinks(Link link, Set<String> visited, String domainName){
        Set<Link> deptLinks = new HashSet<Link>();
        try {       
            Document doc = Jsoup.connect(link.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            //System.out.println("----------------"+link.url+"---------------");
            for (Element e : result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                //System.out.println(anchor+": "+href);
                if (Utility.shouldVisit(anchor,href, visited, domainName) && (anchor.contains("departments") || anchor.contains("academics") || anchor.contains("department list")
                        || anchor.contains("programs") || anchor.contains("academic units") || anchor.contains("about the faculty") || anchor.contains("about the school")
                        || anchor.contains("about the college") || anchor.contains("divisions"))) {
                    Link newLink = new Link();
                    newLink.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(link.context);
                    context.add(anchor);
                    newLink.context = context;
                    deptLinks.add(newLink);
                }
            }
            visited.addAll(Utility.getVisited(link.url, domainName));
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return new HashSet<Link>();
        }
        return deptLinks;
    }
    
    
    public static SemanticList getDeptsResult(Link url, Set<String> visited, ArrayList<String> parents, String domainName) {
        ArrayList<SemanticList> lists = new ArrayList<SemanticList>();
        SemanticList schools = null;
        ArrayList<Link> deptLinks = getDeptLinks(url, visited, domainName);
        for (Link l: deptLinks) {
            //System.out.println(l.url);
        }
        //System.out.println("-----------------------");
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