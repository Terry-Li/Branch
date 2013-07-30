/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fit.cssbox.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
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
        ArrayList<String> positives = Utility.getKeywords("Group/schools.txt");
        ArrayList<String> negatives = Utility.getKeywords("Group/Negatives.txt");
        ArrayList<String> degrees = Utility.getKeywords("Group/degrees.txt");
        ArrayList<String> urlNegatives = Utility.getKeywords("Group/URLNegatives.txt");
        ListEngine engine = new ListEngine(positives, negatives, degrees, urlNegatives);
        SemanticList list = engine.getSchools(l,1);
        if (list != null)
        for (String school: list.list) {
            System.out.println(school);
        }
    }
    
    public static void testDepartment(String url) throws IOException {
        Link univLink = new Link();
        univLink.url = url;
        univLink.context = new ArrayList<String>();
        Set<String> visited = new HashSet<String>();
        SemanticList schoolList = SchoolNav.getSchoolsResult(univLink, visited);
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
                    SemanticList deptList = DepartmentNav.getDeptsResult(schoolLink, visited, schools);
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
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        testDepartment("http://www.virginia.edu/");
    }
}
