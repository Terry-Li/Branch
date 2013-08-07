package org.fit.cssbox.demo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 给定系名，在指定页面中找出该名对应的URL
 * 
 * @author Administrator
 * 
 */
public class GetDepartmentURL {
        
        public static boolean hasPriority(ArrayList<Combo> current){
            String anchor = current.get(0).text.toLowerCase();
            if (anchor.contains("faculty") || anchor.contains("college") || anchor.contains("school") || anchor.startsWith("http")) {
                return true;
            } else {
                return false;
            }
            
        }
        
        public static ArrayList<String> parallelLinks(ArrayList<Combo> combos, ArrayList<Integer> index, String url, int count){
            
            ArrayList<ArrayList<Combo>> candidates = new ArrayList<ArrayList<Combo>>();
            ArrayList<String> links = new ArrayList<String>();
            if (index.size() < 2) return links;
            for (Combo c : combos) {
                //System.out.println(c);
            }

            HashMap<String, ArrayList<Combo>> list = Utility.verticalURL(combos);
            for (String key : list.keySet()) {
                ArrayList<Combo> current = list.get(key);
                for (Combo c : current) {
                    //if (c.x == 140)
                    //System.out.println(c.url);
                }
                //System.out.println("---------------------------");
                if (current.size() == count && current.get(0).index > index.get(0) && current.get(0).index < index.get(1)) {
                    candidates.add(current);
                }
            }
            if (candidates.size() == 1) {
                for (Combo c : candidates.get(0)) {
                    links.add(c.url);
                }
                return links;
            } else if (candidates.size() > 1) {
                for (ArrayList<Combo> current : candidates) {
                    if (hasPriority(current)) {
                        for (Combo c : current) {
                            links.add(c.url);
                        }
                        return links;
                    }
                }
                for (ArrayList<Combo> current : candidates) {
                    if (true) {
                        for (Combo c : current) {
                            links.add(c.url);
                        }
                        return links;
                    }
                }
            }

            return links;
        }
        
}
