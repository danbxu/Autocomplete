package test;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Test;
import indexing.IndexBuilder;

public class IndexBuilderTest {


    String file1 = "http://cit594.ericfouh.com/page1.html";
    String file2 = "http://cit594.ericfouh.com/page2.html";
    String file3 = "http://cit594.ericfouh.com/page3.html";
    String file4 = "http://cit594.ericfouh.com/page4.html";
    String file5 = "http://cit594.ericfouh.com/page5.html";


    //String root = "/autograder/submission/";
    String root = "";


    /*
     * This checks if parseFeed() works as intend, storing 
     * the link as key and the words in a List<string> as the
     * value
     */
    @Test
    public void testParse() {
        IndexBuilder a = new IndexBuilder();

        List<String> container = new ArrayList<>();
        container.add(root + "http://cit594.ericfouh.com/"
                + "sample_rss_feed.xml");


        Map<String, List<String>> hold = 
                a.parseFeed(container);

        assertNotNull(hold);
        assertEquals(5, hold.size());

        String check = "http://cit594.ericfouh.com/page5.html";
        assertTrue(hold.containsKey(check));
        assertEquals(18, hold.get(check).size());
    }


    /*
     * This checks if buildIndex contains the urls, the appropriate words
     * from each url, and the TD_IDF values
     */
    @Test
    public void testBuildIndex() {
        IndexBuilder a = new IndexBuilder();

        List<String> container = new ArrayList<>();
        container.add(root + "http://cit594.ericfouh.com/"
                + "sample_rss_feed.xml");

        Map<String, List<String>> hold = 
                a.parseFeed(container);

        Map<String, Map<String, Double>> tmp = 
                a.buildIndex(hold);

        assertNotNull(tmp);
        assertEquals(5, tmp.size());


        ArrayList<String> urls = new ArrayList<String>();

        for (String urlLink: tmp.keySet()) {
            urls.add(urlLink);
        }

        //Checks if map contains appropriate links
        assertTrue(urls.add(file1));
        assertTrue(urls.add(file2));
        assertTrue(urls.add(file3));
        assertTrue(urls.add(file4));
        assertTrue(urls.add(file5));

        //Checks size of value (Map<String, Double>>)
        assertEquals(8, tmp.get(file1).size());
        assertEquals(40, tmp.get(file2).size());
        assertEquals(29, tmp.get(file3).size());
        assertEquals(21, tmp.get(file4).size());
        assertEquals(18, tmp.get(file5).size());

        //Check for the word "data" (This word is only in files 1, 2, 3)
        assertTrue(tmp.get(file1).containsKey("data"));
        assertTrue(tmp.get(file2).containsKey("data"));
        assertTrue(tmp.get(file3).containsKey("data"));
        assertFalse(tmp.get(file4).containsKey("data"));
        assertFalse(tmp.get(file5).containsKey("data"));

        //Check for TF_IDF value of data in files 1, 2, 3
        double delta = 0.001;

        double dataValue1 = 0.10216512150720541;
        double dataValue2 = 0.0464386922861747;
        double dataValue3 = 0.015479564095391566;

        double dataValueMap1 = tmp.get(file1).get("data");
        double dataValueMap2 = tmp.get(file2).get("data");
        double dataValueMap3 = tmp.get(file3).get("data");

        boolean dataValueMatchesMap1 = false;
        boolean dataValueMatchesMap2 = false;
        boolean dataValueMatchesMap3 = false;

        if (dataValue1 - tmp.get(file1).get("data") < delta) {
            dataValueMatchesMap1 = true;
        }

        if (dataValue2 - tmp.get(file2).get("data") < delta) {
            dataValueMatchesMap2 = true;
        }

        if (dataValue3 - tmp.get(file3).get("data") < delta) {
            dataValueMatchesMap3 = true;
        }

        assertTrue(dataValueMatchesMap1);
        assertTrue(dataValueMatchesMap2);
        assertTrue(dataValueMatchesMap3);
    }


    /*
     * This checks buildInvertedIndex() and looks for the
     * values for keys: "data" and "you". 
     * 
     * The following verifies the expected size of each value, which
     * are made of Entry<String, Double> and checks if the String key,
     * of the Entry<String, Double> matches the urls. Should the
     * keys match, the TD_IDF values will also be correct
     */

    @Test
    public void testBuildInvertedIndex() {
        IndexBuilder a = new IndexBuilder();

        List<String> container = new ArrayList<>();
        container.add(root + "http://cit594.ericfouh.com/"
                + "sample_rss_feed.xml");

        Map<String, List<String>> hold = 
                a.parseFeed(container);

        Map<String, Map<String, Double>> tmp = 
                a.buildIndex(hold);


        Map<?, ?> invertedIndex = a.buildInvertedIndex(tmp);

        Collection<Entry<String, Double>> subContainerData = 
                (Collection<Entry<String, Double>>) invertedIndex.get("data");
        Collection<Entry<String, Double>> subContainerYou = 
                (Collection<Entry<String, Double>>) invertedIndex.get("you");

        assertEquals(3, subContainerData.size());
        assertEquals(2, subContainerYou.size());

        boolean containsFile1 = false;
        boolean containsFile2 = false;
        boolean containsFile3 = false;

        for (Entry<String, Double> url : subContainerData) {
            if (file1.equals(url.getKey())) {
                containsFile1 = true;
            }
            if (file2.equals(url.getKey())) {
                containsFile2 = true;
            }
            if (file3.equals(url.getKey())) {
                containsFile3 = true;
            }
        }

        //Checks that for data, it contains file 1, 2, 3
        //which are stored as the keys 
        assertTrue(containsFile1);
        assertTrue(containsFile2);
        assertTrue(containsFile3);

    }

    /*
     * This checks if homePage correctly removed stop words and kept
     * the none stopwords. 
     */
    @Test
    public void testBuildHomePage() {
        IndexBuilder a = new IndexBuilder();

        List<String> container = new ArrayList<>();
        container.add(root + "http://cit594.ericfouh.com/"
                + "sample_rss_feed.xml");

        Map<String, List<String>> hold = 
                a.parseFeed(container);

        Map<String, Map<String, Double>> tmp = 
                a.buildIndex(hold);


        Map<?, ?> invertedIndex = a.buildInvertedIndex(tmp);

        Collection<Entry<String, List<String>>> homePage = 
                a.buildHomePage(invertedIndex);

        int dataSize = 0;
        int youSize = 0;

        for (Entry<String, List<String>> word : homePage) {
            if (word.getKey().equals("you")) {
                youSize = word.getValue().size();
                break;
            }
            if (word.getKey().equals("data")) {
                dataSize = word.getValue().size();
                break;
            }
        }

        /*
         * This checks if "you" (a stopword, was removed) and 
         * if "data" (not a stopword, is kept). If "you" was removed,
         * the size should be 0 and if "data" is kept, then the size should be
         * 3
         */
        assertEquals(57, homePage.size());
        assertEquals(3, dataSize);
        assertEquals(0, youSize);

    }

    /*
     * This tests if createAutocompleteFile() prints out the appropriate # of words
     * The easiest way to check for this is to check the return value because the return
     * value is a Collection<String> and we can use a .size() to check for the total
     * # of words output to the "Autocomplete.txt" file
     */

    @Test
    public void testcreateAutocompleteFile() {
        IndexBuilder a = new IndexBuilder();

        List<String> container = new ArrayList<>();
        container.add(root + "http://cit594.ericfouh.com/"
                + "sample_rss_feed.xml");

        Map<String, List<String>> hold = 
                a.parseFeed(container);

        Map<String, Map<String, Double>> tmp = 
                a.buildIndex(hold);


        Map<?, ?> invertedIndex = a.buildInvertedIndex(tmp);

        Collection<Entry<String, List<String>>> homePage = 
                a.buildHomePage(invertedIndex);

        Collection < ? > printOuts = 
                a.createAutocompleteFile(homePage);

        assertEquals(57, printOuts.size());
    }

    /*
     * This checks if looking up a specific term will return the appropriate
     * URLS of articles that contain the term 
     */

    @Test
    public void testsearchArticles() {
        IndexBuilder a = new IndexBuilder();

        List<String> container = new ArrayList<>();
        container.add(root + "http://cit594.ericfouh.com/"
                + "sample_rss_feed.xml");

        Map<String, List<String>> hold = 
                a.parseFeed(container);

        Map<String, Map<String, Double>> tmp = 
                a.buildIndex(hold);


        Map<?, ?> invertedIndex = a.buildInvertedIndex(tmp);

        List<String> articles = a.searchArticles("data", invertedIndex);

        //The word "data" is found in files 1, 2, 3
        assertTrue(articles.contains(file1));
        assertTrue(articles.contains(file2));
        assertTrue(articles.contains(file3));
        assertEquals(3, articles.size());
    }
}
