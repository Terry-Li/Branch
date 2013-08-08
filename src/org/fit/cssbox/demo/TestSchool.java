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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author Terry
 */
public class TestSchool {
    public static void testSchool(String url) throws FileNotFoundException, IOException {
        
        Link l = new Link();
        l.url = url;
        l.context = new ArrayList<String>();
        List<String> positives = FileUtils.readLines(new File("Group/schools.txt"));
        List<String> negatives = FileUtils.readLines(new File("Group/Negatives.txt"));
        List<String> degrees = FileUtils.readLines(new File("Group/degrees.txt"));
        List<String> urlNegatives = FileUtils.readLines(new File("Group/URLNegatives.txt"));
        ListEngine engine = new ListEngine(positives, negatives, degrees, urlNegatives);
        SemanticList list = engine.getSchools(l,1);
        if (list != null)
        for (String school: list.list) {
            System.out.println(school);
        }
    }
    
    public static void testDepartment(String url) throws FileNotFoundException, IOException {
        Link l = new Link();
        l.url = url;
        l.context = new ArrayList<String>();
        Set<String> visited = new HashSet<>();
        ArrayList<String> parents = new ArrayList<String>();
        SemanticList list = DepartmentNav.getDeptsResult(l, visited, parents,"");
        if (list != null)
        for (String school: list.list) {
            System.out.println(school);
        }
    }
    
    public static void testUniv(String url) throws IOException {
        Link univLink = new Link();
        univLink.url = url;
        univLink.context = new ArrayList<String>();
        Set<String> visited = new HashSet<String>();
        SemanticList schoolList = SchoolNav.getSchoolsResult(univLink, visited, "");
        if (schoolList != null) {
            ArrayList<String> schools = schoolList.list;
            for (String school : schools) {
                //String schoolFac = FacultyNav.getFacultyURL(school.split("==")[1], visited);
                System.out.println("School==" + school);

                String schoolURL = school.split("==")[1];
                if (!schoolURL.equals("null")) {
                    Link schoolLink = new Link();
                    schoolLink.url = schoolURL;
                    schoolLink.context = new ArrayList<String>();
                    SemanticList deptList = DepartmentNav.getDeptsResult(schoolLink, visited, schools, "");
                    if (deptList != null) {
                        ArrayList<String> departments = deptList.list;
                        for (String dept : departments) {
                            System.out.println("Dept==" + dept);
                        }
                    }
                }
                System.out.println();
            }

        }
    }
    
    public static void testLines() throws FileNotFoundException, IOException {
        List<String> positives = Utility.getKeywords("Group/departments.txt");       
        System.out.println(positives.size());        
    }
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        //testDepartment("http://www.dal.ca/faculty/science.html");
        //testSchool("http://faculties.ualberta.ca/");
        testLines();
    }
}
