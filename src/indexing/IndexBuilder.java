package indexing;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class IndexBuilder implements IIndexBuilder {

    /**
     * <parseFeed> Parse each document/rss feed in the list and return a Map of
     * each document and all the words in it. (punctuation and special
     * characters removed)
     * 
     * @param feeds a List of rss feeds to parse
     * @return a Map of each documents (identified by its url) and the list of
     *         words in it.
     */
    @Override
    public Map<String, List<String>> parseFeed(List<String> feeds) {

        HashMap<String, List<String>> hashMap = new HashMap<String, List<String>>();

        for (int i  = 0; i < feeds.size(); i++) {

            String url = feeds.get(i);

            try {
                Document doc = Jsoup.connect(url).get();
                Elements links = doc.getElementsByTag("link");

                for (Element link : links) {

                    String linkText = link.text();

                    Document temp = Jsoup.connect(linkText).get();
                    Elements words = temp.getElementsByTag("body");
                    String wordsText = words.text();

                    List<String> tempList = parseThis(wordsText);

                    hashMap.put(linkText, tempList);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }       
        }
        return hashMap;
    }

    /*
     * This method is a helper function used in parseFeed(). This
     * method will take the string taken from <link> tag, split the words
     * into tokens and remove any non-alphanumeric characters and set all
     * letters to lower case. This will then return all the modified tokens
     * into a List<String>
     * 
     * @param the text line from <link> tag
     * @return List<string> of all the words 
     */
    private List<String> parseThis(String line) {
        if (line != null) {
            //Break by space
            String[] dump = line.split(" ");
            List<String> container = new ArrayList<String>();

            for (int i = 0; i < dump.length; i++) {

                String cleanWords = dump[i].toLowerCase();
                cleanWords = cleanWords.replaceAll("[^A-Za-z0-9 ]", "");
                container.add(cleanWords);
            }
            return container;
        }
        return null;
    }

    /**
     * @param docs a map computed by {@parseFeed}
     * @return the forward index: a map of all documents and their 
     *         tags/keywords. the key is the document, the value is a 
     *         map of a tag term and its TFIDF value. 
     *         The values (Map<String, Double>) are sorted
     *         by lexicographic order on the key (tag term)
     *  
     */

    @Override
    public Map<String, Map<String, Double>> buildIndex(Map<String, List<String>> docs) {
        double tf = 0;
        double idf = 0; 
        double tfidf = 0;

        Map<String, Map<String, Double>> container = new HashMap<String, Map<String, Double>>();

        //make a comparator and pass in the comparator 
        for (String key : docs.keySet()) {

            //Store values of each key 
            List<String> tmp = docs.get(key);

            Set<String> distinct = new HashSet<String>(tmp);
            Map<String, Double> subcontainer = new TreeMap<String, Double>(); 


            //Check for each word and get the tf / tf_idf values
            for (String s: distinct) {

                double occur = Collections.frequency(docs.get(key), s);
                double listSize = docs.get(key).size();

                tf = occur / listSize;

                double counter = 0;

                for (String keys : docs.keySet()) {
                    if (docs.get(keys).contains(s)) {
                        counter++;
                    }
                }

                idf = Math.log(docs.size() / counter);
                tfidf = tf * idf;

                subcontainer.put(s, tfidf);

            }
            container.put(key, subcontainer);
        }
        return container;
    }


    /**
     * Build an inverted index consisting of a map of each tag term and a Collection (Java)
     * of Entry objects mapping a document with the TFIDF value of the term 
     * (for that document)
     * The Java collection (value) is sorted by reverse tag term TFIDF value 
     * (the document in which a term has the
     * highest TFIDF should be listed first).
     * 
     * 
     * @param index the index computed by {@buildIndex}
     * @return inverted index - a sorted Map of the documents in which term is a keyword
     */

    @Override
    public Map<?, ?> buildInvertedIndex(Map<String, Map<String, Double>> index) {

        //Create new comparator object and return the comparisons
        Comparator<Entry<String, Double>> compareThis = new Comparator<Entry<String, Double>>() {
            @Override
            public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }

        };

        Map<String, ArrayList<Entry<String, Double>>> bigMap = new 
                HashMap<String, ArrayList<Entry<String, Double>>>();


        for (String url: index.keySet()) {

            Map<String, Double> tmp = index.get(url);
            for (String term : tmp.keySet()) {

                if (!bigMap.containsKey(term)) {

                    ArrayList<Entry<String, Double>> newCollection = 
                            new ArrayList<Entry<String, Double>>();

                    newCollection.add(new 
                            AbstractMap.SimpleEntry<String, Double>(url, tmp.get(term)));

                    bigMap.put(term, newCollection);

                } else {

                    bigMap.get(term).add(new 
                            AbstractMap.SimpleEntry<String, Double>(url, index.get(url).get(term)));

                    Collections.sort(bigMap.get(term), compareThis);   
                }
            }

        }

        return bigMap;
    } 

    /**
     * @param invertedIndex
     * @return a sorted collection of terms and articles Entries are sorted by
     *         number of articles. If two terms have the same number of 
     *         articles, then they should be sorted by reverse lexicographic order.
     *         The Entry class is the Java abstract data type
     *         implementation of a tuple
     *         https://docs.oracle.com/javase/9/docs/api/java/util/Map.Entry.html
     *         One useful implementation class of Entry is
     *         AbstractMap.SimpleEntry
     *         https://docs.oracle.com/javase/9/docs/api/java/util/AbstractMap.SimpleEntry.html
     */

    @Override
    public Collection<Entry<String, List<String>>> buildHomePage(Map<?, ?> invertedIndex) {


        Comparator<Entry<String, List<String>>> compareThis = 
                new Comparator<Entry<String, List<String>>>() {
            @Override
            public int compare(Entry<String, List<String>> o1, Entry<String, List<String>> o2) {

                int tmp1 = o1.getValue().size();
                int tmp2 = o2.getValue().size();

                //Break tie 
                if (tmp1 == tmp2) {
                    return o2.getKey().compareTo(o1.getKey());
                }
                return tmp2 - tmp1;
            }

        };

 
//        Collection<Entry<String, List<String>>> newCollection =  
//                new ArrayList<Entry<String, List<String>>>();

        //intermediate container tag - empty 
        List<Entry<String, List<String>>> container = new ArrayList<>();

        //Populate container with <Term, empty List<String>

        for (Object word : invertedIndex.keySet()) { 

            ArrayList<Entry<String, Double>> tmp = 
                    (ArrayList<Entry<String, Double>>) invertedIndex.get(word);

            boolean ifStopWord = false;
            for (int i = 0; i < STOPW.length; i++) {
                if (STOPW[i].equals(word)) {
                    ifStopWord = true;
                    break;
                }

            }

            if (ifStopWord) {
                continue;
            }

            ArrayList<String> newList = new ArrayList<>();
            //put specific word 
            container.add(new 
                    AbstractMap.SimpleEntry<String, List<String>>((String) word, newList));
        }


        for (Entry<String, List<String>> word : container) {

            for (Object term : invertedIndex.keySet()) {         

                ArrayList<Entry<String, Double>> tmp = 
                        (ArrayList<Entry<String, Double>>) invertedIndex.get(term);

                for (Entry<String, Double> url : tmp) {
                    if (term.equals(word.getKey())) {
                        word.getValue().add((String) url.getKey());
                        
                        //Collections.sort(word.getValue());
                    }
                }
            }
        }

        Collections.sort(container, compareThis);
        return container;
    }


    /**
     * Create a file containing all the words in the inverted index. Each word
     * should occupy a line Words should be written in lexicographic order
     * assign a weight of 0 to each word. The method must store the words into a 
     * file named autocomplete.txt
     * 
     * @param homepage the collection used to generate the homepage (buildHomePage)
     * @return A collection containing all the words 
     * written into the file sorted by lexicographic order
     */

    @Override
    public Collection<?> createAutocompleteFile(Collection<Entry<String, List<String>>> homepage) {

        int num = homepage.size();
        String numString = String.valueOf(num) + "\n";

        //Does it matter if it's not an Collection?
        TreeSet<String> container = new TreeSet<String>();

        try {
            File outFile = new File("Autocomplete.txt");
            FileWriter r = new FileWriter(outFile);

            //Write numbers
            r.write(numString);

            List<Entry<String, List<String>>> tmp = 
                    (List<Entry<String, List<String>>>) homepage;

            //Write every word with weight of 0
            for (Entry<String, List<String>> term : tmp) {
                container.add(term.getKey());
                String tmpString = "0 " + term.getKey() + "\n";
                r.write(tmpString);

            }
            r.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return container;
    }


    /**
     * This method just returns a List<String> of related URLS based on the queryTerm. 
     * This method searches all words, including the stopwords from the invertedIndex
     * parameter 
     * @param queryTerm
     * @param invertedIndex
     * @return List<String> of URLS
     */

    @Override
    public List<String> searchArticles(String queryTerm, Map<?, ?> invertedIndex) {

        Map<?, ?>  tmp = invertedIndex;

        List<String> container = new ArrayList<String>();

        for (Object word: tmp.keySet()) {
            if (word.equals(queryTerm)) {

                List<Entry<String, Double>> subContainer = 
                        (List<Entry<String, Double>>) tmp.get(word);

                for (Entry<String, Double> url : subContainer) {
                    container.add(url.getKey());
                }
            }

        }
        return container;
    }

}