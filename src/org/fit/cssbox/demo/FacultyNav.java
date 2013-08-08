/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class FacultyNav{
    public static List<String> keywords;
    
    static {
        try {
            keywords = Utility.getKeywords("Group/Names.txt");
        } catch (IOException ex) {
            Logger.getLogger(FacultyNav.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static String getExactLink(ArrayList<String> links, String keyword) {
        for (String link: links) {
            String[] tokens = link.split("==");
            if (tokens[0].equalsIgnoreCase(keyword)) {
                return tokens[1];
            }
        }
        return null;
    }
    
    
    
    public static ArrayList<String> getLinks(String url, Set<String> visited, String domainName) {
        try {
            ArrayList<String> links = new ArrayList<String>();
            Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e : result) {
                String anchor = e.text().trim();
                String href = e.attr("abs:href").trim();
                if (Utility.shouldVisit(anchor, href, visited, domainName)) {
                    links.add(anchor+"=="+href);
                }
            }
            return links;
        } catch (IOException ex) {
            Logger.getLogger(FacultyNav.class.getName()).log(Level.SEVERE, null, ex);
            return new ArrayList<String>();
        }
    }

    
    public static ArrayList<String> getFacultyLinks(ArrayList<String> links) {
        ArrayList<String> facLinks = new ArrayList<String>();
        int count = 0;
        for (String link: links) {
            String anchor = link.split("==")[0];
            if (count <2 && anchor.split(" ").length >= 2 && anchor.split(" ").length <=4 && anchor.toLowerCase().contains("faculty") 
                    && !anchor.toLowerCase().contains("adjunct")&& !anchor.toLowerCase().contains("emeriti") && !anchor.toLowerCase().contains("awards")
                    && !anchor.toLowerCase().contains("publication")) { 
                facLinks.add(link.split("==")[1]);
                count++;
            }
        }
        return facLinks;
    }
    
    public static String getFacultyAndStaff(ArrayList<String> links) {
        for (String link: links) {
            String[] tokens = link.split("==");
            if (tokens[0].toLowerCase().contains("faculty") && tokens[0].toLowerCase().contains("staff")) {
                return tokens[1];
            }
        }
        return null;
    }
    
    public static ArrayList<String> getFacultyListURL(String url, Set<String> visited, String domainName) {
        ArrayList<String> candidates = new ArrayList<String>();
        ArrayList<String> links = getLinks(url, visited,domainName );
        String facultyURL = getExactLink(links, "Faculty");
        String peopleURL = getExactLink(links, "People");
        String directoryURL = getExactLink(links, "Directory");
        String facultyStaffURL = getFacultyAndStaff(links);
        if (facultyURL != null) {
            candidates.add(facultyURL);
            visited.add(facultyURL);
            ArrayList<String> seconds = getFacultyLinks(getLinks(facultyURL,visited, domainName));
            candidates.addAll(seconds);
            visited.addAll(seconds);
        }
        if (peopleURL != null) {
            candidates.add(peopleURL);
            visited.add(peopleURL);
            String facultyURL2 = getExactLink(getLinks(peopleURL,visited, domainName), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
                visited.add(facultyURL2);
            }
        }
        if (facultyStaffURL != null) {
            candidates.add(facultyStaffURL);
            visited.add(facultyStaffURL);
            String facultyURL2 = getExactLink(getLinks(facultyStaffURL,visited, domainName), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
                visited.add(facultyURL2);
            }
        }
        if (directoryURL != null) {
            candidates.add(directoryURL);
            visited.add(directoryURL);
            String facultyURL2 = getExactLink(getLinks(directoryURL,visited, domainName), "Faculty");
            if (facultyURL2 != null) {
                candidates.add(facultyURL2);
                visited.add(directoryURL);
            }
        }
        ArrayList<String> temps = getFacultyLinks(links);
        candidates.addAll(temps);
        visited.addAll(temps);
        for (String temp: temps) {
            ArrayList<String> seconds = getFacultyLinks(getLinks(temp,visited, domainName));
            candidates.addAll(seconds);
            visited.addAll(seconds);
        }
        return candidates;
    }
    
    public static String getFacultyURL(String url, Set<String> visited, String domainName) {
        if (url.equals("null")) return null;
        ArrayList<String> candidates = getFacultyListURL(url, visited, domainName);
        for (String candidate: candidates) {
            //System.out.println(candidate);
            if (candidate != null && identify(candidate)) {//&& Faculty2.identify(candidate)
                return candidate;
            }
        }
        return null;
    }
    
    public static boolean isName(String text) {
        String[] names = text.toUpperCase().trim().split(" ");
        if (names.length <= 4) {
            for (String name : names) {
                for (String keyword : keywords) {
                    if (name.equals(keyword)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

  
    public static boolean valid(ArrayList<Combo> combos) {
        int count = 0;
        int size = combos.size();
        for (Combo c: combos) {
            if (isName(c.text) || c.text.contains("@") || c.text.contains("Professor")) {
                count++;
            }
        }
        if (size > 4 && count > size*2/3) {return true;}
        else return false;
    }
    
    
    public static boolean identify(String link) {
        ArrayList<String> names = new ArrayList<String>();
        ArrayList<Combo> combos = CSSModel.getCombos(link);
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.style + "" + c.height;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        for (String key: sets.keySet()) {
            ArrayList<Combo> value = sets.get(key);
            //for (Combo c: value) System.out.println(c);
            //System.out.println("-------------------------------------");
            if (valid(value)) {
                for (Combo c: value) {
                    names.add(c.text+"=="+c.url);
                }
            }
        }
        if (names.size() > 4) {
            return true;
        } else return false;
    }
    
}
