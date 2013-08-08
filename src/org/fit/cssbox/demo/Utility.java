/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import Zion.SUSE;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Terry
 */
public class Utility {
    private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g" 
                                                          + "|png|tiff?|mid|mp2|mp3|mp4"
                                                          + "|wav|avi|mov|mpeg|ram|m4v|pdf" 
                                                          + "|doc|docx|xls|xlsx|ppt|pptx"
                                                          + "|xml"
                                                          + "|rm|smil|wmv|swf|wma|zip|rar|gz))$");
    private final static String[] gates = {"colleges","divisions","schools","departments","academics","academic units",
    "academic areas","programs","faculties","department list","about the college","about the faculty","about the school",
    "faculty","directory","people","staff"};
    

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }
    
    public static List<String> getKeywords(String file) throws FileNotFoundException, IOException {
        List<String> keywords = new ArrayList<String>();
        FileInputStream fstream = null;
        fstream = new FileInputStream(file);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String strLine;
        while ((strLine = br.readLine()) != null && !strLine.trim().equals("")) {
            keywords.add(strLine.trim());
        }
        return keywords;
    }
    
    public static boolean isGate(String anchor) {
        for (String gate: gates) {
            if (anchor.toLowerCase().contains(gate)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean shouldVisit(String anchor, String url, Set<String> visited, String domainName) {
        if (anchor==null || !isGate(anchor) || anchor.split(" ").length > 6) return false;
        url = url.toLowerCase();
        if (url.contains(domainName) &&
            url.startsWith("http")&& !url.contains("#") &&!url.contains("@") && !FILTERS.matcher(url).matches() &&
            !url.contains("admission")) //&& !url.contains("faculty")
        {
            for (String str: visited) {
                if (str.equalsIgnoreCase(url)) {
                    return false;
                }
            }
            return true;
        } else return false;
    }
 
    public static boolean similar(int first, int second) {
        boolean result = false;
        if (first <= second*2 && first >= second/2) {
            result = true;
        }
        return result;
    }
    
    public static void group(ArrayList<Combo> combos) {
        if (combos.size() >= 3) {
            int[] intervals = new int[combos.size() - 1];
            for (int i = 0; i < combos.size() - 1; i++) {
                intervals[i] = Math.abs(combos.get(i + 1).y - combos.get(i).y) - combos.get(i).contract;
                //if (combos.get(0).x == 116) System.out.println(combos.get(i).contract);
            } 
            //System.out.println(Arrays.toString(intervals));
            for (int k=0;k<intervals.length-1;k++) {
                if (similar(intervals[k], intervals[k+1])) {
                    combos.get(k).setGroup(1);
                    combos.get(k+1).setGroup(1);
                    combos.get(k+2).setGroup(1);
                    int group = 1;
                    for (int j = k+3; j < combos.size(); j++) {
                        if (intervals[j - 1] > intervals[j - 2] * 2) {
                            group++;
                            combos.get(j).setGroup(group);
                        } else {
                            combos.get(j).setGroup(group);
                        }
                    }
                    break;
                }
            }  
        } else {
            for (Combo c: combos) {
                c.setGroup(1);
            }
        }
    }
    
    public static void groupExact(ArrayList<Combo> combos) {
        //bubble(combos);
        if (combos.size() >= 3) {
            int[] intervals = new int[combos.size() - 1];
            for (int i = 0; i < combos.size() - 1; i++) {
                intervals[i] = Math.abs(combos.get(i + 1).y - combos.get(i).y) - combos.get(i).contract;
            } 
            for (int k=0;k<intervals.length-1;k++) {
                if (intervals[k] == intervals[k+1]) {
                    combos.get(k).setGroup(1);
                    combos.get(k+1).setGroup(1);
                    combos.get(k+2).setGroup(1);
                    int group = 1;
                    for (int j = k+3; j < combos.size(); j++) {
                        if (intervals[j - 1] > intervals[j - 2]) { //intervals[j - 1] != intervals[j - 2]
                            group++;
                            combos.get(j).setGroup(group);
                        } else {
                            combos.get(j).setGroup(group);
                        }
                    }
                    break;
                }
            }  
        }else {
            for (Combo c: combos) {
                c.setGroup(1);
            }
        }
    }
    
    public static HashMap<String,ArrayList<Combo>> vertical(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        //HashMap<String,ArrayList<Combo>> result = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + ""+ c.style;
                //if (c.text.equals("School of Business")) System.out.println(c);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        /*
        for (String key: sets.keySet()) {
            ArrayList<Combo> current = sets.get(key);
            boolean hasTitle = false;
            for (int i=0;i<current.size()-1;i++) {
                if (!current.get(i+1).previous.equals(current.get(i))){
                    hasTitle = true;
                    break;
                }
            }
            if (!hasTitle) {
                result.put(key, current);
            } else {
                ArrayList<Combo> temp = new ArrayList<Combo>();
                temp.add(current.get(0));
                //System.out.println("Need to split...");
                //System.out.println(current.get(0));
                for (int i=1;i<current.size();i++) {
                    //System.out.println(current.get(i).previous);
                    //System.out.println(current.get(i-1));
                    //System.out.println("-------------------------");
                    if (current.get(i).previous != current.get(i-1)) { //!current.get(i).previous.equals(current.get(i-1))
                        //System.out.println(current.get(i));
                        result.put(key+""+i, temp);
                        temp = new ArrayList<Combo>();
                        temp.add(current.get(i));
                    } else {
                        //System.out.println("=====================");
                        temp.add(current.get(i));
                    }
                }
                result.put(key+""+current.size(), temp);
            }
        }*/
        return sets;
    }
    
    public static HashMap<String,ArrayList<Combo>> verticalURL(ArrayList<Combo> combos) {
        ArrayList<Combo> urlCombos = new ArrayList<Combo>();
        for (Combo c: combos) {
            if (c.url != null && !c.url.contains("#") && !c.url.contains("@")) {
                urlCombos.add(c);
            }
        }
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        //HashMap<String,ArrayList<Combo>> result = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: urlCombos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + ""+ c.style;
                //if (c.text.equals("School of Business")) System.out.println(c);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        
        return sets;
    }
    
    
    public static HashMap<String,ArrayList<Combo>> verticalNames(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + "" + c.style;
                //System.out.println(c.style);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        return sets;
    }
    
    public static HashMap<String,ArrayList<Combo>> horizontal(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.y + "" + c.height + ""+ c.style;
                //System.out.println(c);
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        return sets;
    }
    
    public static HashMap<String,ArrayList<Combo>> arithmetic(ArrayList<Combo> combos, int grouptype) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height + "" + c.style;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        HashMap<String,ArrayList<Combo>> results = new HashMap<String,ArrayList<Combo>>();
        for (String key: sets.keySet()) {
            ArrayList<Combo> temps = sets.get(key);
            if (temps.size() > 3) {
                if (grouptype == 1) {
                    group(temps);
                } else groupExact(temps);
                //System.out.println("Vertical:");
                for (Combo t : temps) {
                    //System.out.println(t);
                }
                for (Combo c : temps) {
                    if (c.group != 0) {
                        String newKey = key + "" + c.group;
                        if (results.keySet().contains(newKey)) {
                            results.get(newKey).add(c);
                        } else {
                            ArrayList<Combo> empty = new ArrayList<Combo>();
                            empty.add(c);
                            results.put(newKey, empty);
                        }
                    }
                }
            }
        }
        return results;
    }

    public static HashMap<String,ArrayList<Combo>> tiled(ArrayList<Combo> combos, int grouptype) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height+ "" + c.style;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        HashMap<String,ArrayList<Combo>> results = new HashMap<String,ArrayList<Combo>>();
        for (String key: sets.keySet()) {
            ArrayList<Combo> temps = sets.get(key);
            if (temps.size() > 1) {
                if (grouptype == 1) {
                    group(temps);
                } else if(grouptype == 2) {
                    groupExact(temps);
                }
                for (Combo c : temps) {
                    //if(grouptype==0)System.out.println(c);
                    if (c.group != 0) {
                        //if (c.text.equals("School of Business")) System.out.println(c);
                        String newKey = key + "" + c.group;
                        if (results.keySet().contains(newKey)) {
                            results.get(newKey).add(c);
                        } else {
                            ArrayList<Combo> empty = new ArrayList<Combo>();
                            empty.add(c);
                            results.put(newKey, empty);
                        }
                    }
                }
            }
        }
        return results;
    }
    
    public static HashMap<String,ArrayList<Combo>> tiled(ArrayList<Combo> combos) {
        HashMap<String,ArrayList<Combo>> sets = new HashMap<String,ArrayList<Combo>>();
        for (Combo c: combos) {
            if (c.text.trim().length()!=0) {
                String key = c.x + "" + c.height+ "" + c.style;
                if (sets.keySet().contains(key)) {
                    sets.get(key).add(c);
                } else {
                    ArrayList<Combo> empty = new ArrayList<Combo>();
                    empty.add(c);
                    sets.put(key, empty);
                }
            }
        }
        HashMap<String,ArrayList<Combo>> results = new HashMap<String,ArrayList<Combo>>();
        for (String key: sets.keySet()) {
            ArrayList<Combo> temps = sets.get(key);
            if (temps.size() > 1) {
                float total = 0;
                for (int i=0;i<temps.size()-1;i++) {
                    total += temps.get(i+1).y - temps.get(i).y - temps.get(i).contract;
                }
                float gap = total/(temps.size()-1);
                if (gap > 50) {
                    results.put(key, temps);
                }
            }
        }
        return results;
    }
    
    public static Set<String> getVisited(String url, String domainName){
        Set<String> deptLinks = new HashSet<String>();
        try { 
            if (SUSE.polite) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Document doc = Jsoup.connect(url).timeout(0).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.2 (KHTML, like Gecko) Chrome/15.0.874.120 Safari/535.2").get();
            Elements result = doc.select("a");
            for (Element e : result) {
                String anchor = e.text().trim().toLowerCase();
                String href = e.attr("abs:href").trim();
                if (shouldVisit(anchor, href,(Set)new HashSet<>(),domainName)) {
                    deptLinks.add(href);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Utility.class.getName()).log(Level.SEVERE, null, ex);
            return new HashSet<String>();
        }
        return deptLinks;
    }
    
}
