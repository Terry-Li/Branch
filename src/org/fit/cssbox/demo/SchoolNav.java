/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;


import Zion.SUSE;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
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
public class SchoolNav{
    private static ListEngine engine;
    
    static {
        try {
            List<String> positives = Utility.getKeywords("Group/schools.txt");
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
    
    public static boolean needURLStrong(ArrayList<String> temps) {
        for (int i=0; i<temps.size()-1; i++) {
            if (temps.get(i).split("==").length==2 && !temps.get(i).split("==")[1].equals("null")) {
                return false;
            }
        }
        return true;
    }
      
    public static void addURLs(ArrayList<Combo> combos, ArrayList<String> temps, ArrayList<Integer> index, String baseURL) throws IOException{
        if (!needURLStrong(temps)) return; //|| temps.size() != index.size()

        ArrayList<String> as = GetDepartmentURL.parallelLinks(combos, index, baseURL, temps.size());
        if (as.size() != 0) {
            for (int i = 0; i < temps.size(); i++) {
                String[] tokens = temps.get(i).split("==");
                temps.set(i, tokens[0] + "==" + as.get(i));
            }
        } else {
            for (int i=0; i<index.size()-1; i++) {
                for (int j=index.get(i)+1; j<index.get(i+1); j++) {
                    String url = combos.get(j).url;
                    String anchor = combos.get(j).text.toLowerCase();
                    if (url != null && !url.contains("#") && !url.contains("@") && (anchor.contains("website")||
                            anchor.contains("home")||anchor.contains("site")||
                            anchor.startsWith("http"))) {
                        String[] tokens = temps.get(i).split("==");
                        temps.set(i, tokens[0] + "==" + url);
                        break;
                    }
                }
            }
            for (int j = index.get(index.size()-1) + 1; j < combos.size(); j++) {
                String url = combos.get(j).url;
                String anchor = combos.get(j).text.toLowerCase();
                if (url != null && !url.contains("#") && !url.contains("@") && (anchor.contains("website")||
                        anchor.contains("home")||anchor.contains("site")||
                        anchor.startsWith("http"))) {
                    String[] tokens = temps.get(index.size()-1).split("==");
                    temps.set(index.size()-1, tokens[0] + "==" + url);
                    break;
                }
            }
            if (needURLStrong(temps)) {
                for (int i = 0; i < index.size() - 1; i++) {
                    for (int j = index.get(i) + 1; j < index.get(i + 1); j++) {
                        String url = combos.get(j).url;
                        if (url != null && !url.contains("#") && !url.contains("@")) {
                            String[] tokens = temps.get(i).split("==");
                            temps.set(i, tokens[0] + "==" + url);
                            break;
                        }
                    }
                }
                for (int j = index.get(index.size() - 1) + 1; j < combos.size(); j++) {
                    String url = combos.get(j).url;
                    if (url != null && !url.contains("#") && !url.contains("@")) {
                        String[] tokens = temps.get(index.size() - 1).split("==");
                        temps.set(index.size() - 1, tokens[0] + "==" + url);
                        break;
                    }
                }
            }
        }
        
    }
    
    
    public static ArrayList<String> dedup(ArrayList<Combo> combos, ArrayList<String> dups, ArrayList<Integer> index, String baseURL) {
        if (index.size()==0) return new ArrayList<String>();
        int count = 0;
        for (int i=0; i<dups.size(); i++) {
            String[] pairs = dups.get(i).split("==");
            if (pairs.length!=2) continue;
            if (pairs[1].contains("#") || pairs[1].contains("@")) {
                count++;
                //dups.set(i, pairs[0]+"==null");
            }
        }
        if (count*2 >= index.size()) return new ArrayList<String>();//if contain #, not counted as a valid list
       
        
        try {
            addURLs(combos,dups,index,baseURL);
        } catch (IOException ex) {
            Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
        }
        ArrayList<String> urls = new ArrayList<String>();
        ArrayList<String> dedups = new ArrayList<String>();
        for (String dup: dups) {
            if (dup.split("==").length!=2) continue;
            //System.out.println(dup);
            String url = dup.split("==")[1];
            if (url.equals("null")) {
                dedups.add(dup);
            } else 
            if (!urls.contains(url)) { 
                urls.add(url);
                dedups.add(dup);
            }
        }
        return dedups;
    }
     
    
    public static ArrayList<Link> getSchoolLinks(Link url, String domainName) {
        if (SUSE.polite) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ArrayList<Link> schoolLinks = new ArrayList<Link>();
        try {
            Document doc = Jsoup.connect(url.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e: result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if (Utility.shouldVisit(anchor, href, new HashSet<String>(), domainName) && (anchor.contains("colleges")|| anchor.contains("divisions") || anchor.contains("schools") || anchor.contains("faculties") || anchor.contains("departments"))) {
                    Link link = new Link();
                    link.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(url.context);
                    context.add(anchor);
                    link.context = context;
                    schoolLinks.add(link);
                } else {
                    Elements images = e.select("img");
                    if (images.size()>0) {
                        for (Element image: images) {
                            String alt = image.attr("alt").toLowerCase();
                            String title = image.attr("title").toLowerCase();
                            if (Utility.shouldVisit(alt+" "+title,href, new HashSet<String>(),domainName) && 
                                    (alt.contains("schools")||alt.contains("divisions")||alt.contains("colleges")||
                                    alt.contains("faculties")||alt.contains("departments")||title.contains("schools")||
                                    title.contains("divisions")||title.contains("colleges")||
                                    title.contains("faculties")||title.contains("departments"))
                                    ) {
                                Link link = new Link();
                                link.url = href;
                                ArrayList<String> context = new ArrayList<String>();
                                context.addAll(url.context);
                                if (alt.contains("schools")||alt.contains("divisions")||alt.contains("colleges")||alt.contains("faculties")||alt.contains("departments")) {
                                    context.add(alt);
                                } else {
                                    context.add(title);
                                }
                                link.context = context;
                                schoolLinks.add(link);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return schoolLinks;
        }
        return schoolLinks;
    }
    
    public static ArrayList<Link> getAcademicLinks(Link url, String domainName) {
        if (SUSE.polite) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ArrayList<Link> schoolLinks = new ArrayList<Link>();
        try {
            Document doc = Jsoup.connect(url.url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e: result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if (Utility.shouldVisit(anchor, href, new HashSet<String>(),domainName) && (anchor.contains("academics") || anchor.contains("academic units") || 
                     anchor.contains("academic divisions") || anchor.contains("academic areas") || 
                     anchor.contains("academic programs") || anchor.contains("faculties") || anchor.contains("departments"))) {
                    Link link = new Link();
                    link.url = href;
                    ArrayList<String> context = new ArrayList<String>();
                    context.addAll(url.context);
                    context.add(anchor);
                    link.context = context;
                    schoolLinks.add(link);
                } else {
                    Elements images = e.select("img");
                    if (images.size()>0) {
                        for (Element image: images) {
                            String alt = image.attr("alt").toLowerCase();
                            String title = image.attr("title").toLowerCase();
                            if (Utility.shouldVisit(alt+" "+title, href, new HashSet<String>(), domainName) && (alt.contains("academics")||alt.contains("academic units")||alt.contains("academic divisions")||
                                    alt.contains("academic areas")||alt.contains("academic programs")||alt.contains("departments")
                                    ||title.contains("academics")||title.contains("academic units")||
                                    title.contains("academic divisions")||title.contains("academic areas")||
                                    title.contains("academic programs")||title.contains("departments")) 
                                 ) {
                                Link link = new Link();
                                link.url = href;
                                ArrayList<String> context = new ArrayList<String>();
                                context.addAll(url.context);
                                if (alt.contains("academics")||alt.contains("academic units")||alt.contains("academic divisions")||alt.contains("academic areas")||alt.contains("academic programs")||alt.contains("departments")) {
                                    context.add(alt);
                                } else {
                                    context.add(title);
                                }
                                link.context = context;
                                schoolLinks.add(link);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return schoolLinks;
        }
        return schoolLinks;
    }
    
    public static ArrayList<Link> dedupNavLinks(ArrayList<Link> dups) {
        ArrayList<Link> dedups = new ArrayList<Link>();
        ArrayList<String> urls = new ArrayList<String>();
        for (Link dup: dups) {
            if (!urls.contains(dup.url)) {
                dedups.add(dup);
                urls.add(dup.url);
            }
        }
        return dedups;
    }
    
    public static ArrayList<Link> getNavLinks(Link url, String domainName) {
        ArrayList<Link> navLinks = new ArrayList<Link>();
        navLinks.add(url); //homepage
        ArrayList<Link> schoolLinks = getSchoolLinks(url, domainName);
        navLinks.addAll(schoolLinks);
        ArrayList<Link> academicLinks = getAcademicLinks(url, domainName);
        navLinks.addAll(academicLinks);
        for (Link link: academicLinks) {
            navLinks.addAll(getSchoolLinks(link, domainName));
        }
        return dedupNavLinks(navLinks);
    }
    
    public static SemanticList getSchoolsResult(Link link, Set<String> visited, String domainName) {
        ArrayList<SemanticList> lists = new ArrayList<SemanticList>();
        SemanticList schools = null;
        ArrayList<Link> toSchedule = getNavLinks(link, domainName); 
        //System.out.println(toSchedule.size());
        for (int i=0;i<toSchedule.size();i++){
            try {
                //System.out.println(l.url);
                schools = engine.getSchools(toSchedule.get(i),1);
                visited.addAll(Utility.getVisited(toSchedule.get(i).url, domainName));
                if (schools != null) {
                    //System.out.println("oh yeah");
                    lists.add(schools);
                }
            } catch (IOException ex) {
                Logger.getLogger(SchoolNav.class.getName()).log(Level.SEVERE, null, ex);
            }
        }      
        
        if (lists.size() > 0) {
            //System.out.println("oh yeah");
            for (SemanticList list : lists) {
                ArrayList<String> items = list.list;
                for (String item: items) {
                    if (item.toLowerCase().contains("school") || item.toLowerCase().contains("college") || item.toLowerCase().contains("faculty")){
                        return list;
                    }
                }
            }
            for (SemanticList list : lists) {
                String head = list.head;
                String title = list.title;
                if (head == null) head = "";
                if (title == null) title = "";
                if (head.toLowerCase().contains("schools") || head.toLowerCase().contains("colleges")
                        || head.toLowerCase().contains("divisions")|| head.toLowerCase().contains("faculties") || title.toLowerCase().contains("schools")
                        || title.toLowerCase().contains("colleges") || title.toLowerCase().contains("divisions") || title.toLowerCase().contains("faculties")) {
                    return list;
                }
            }
            for (SemanticList list : lists) {
                ArrayList<String> context = list.context.context;
                if (context.size()>0) {
                    String anchor = context.get(0).toLowerCase();
                    if (anchor.contains("schools") || anchor.contains("colleges") || anchor.contains("faculties")) {
                        return list;
                    }
                }
            }
            return lists.get(0);
        } else {
            return null;
        }

    }
}
