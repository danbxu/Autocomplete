package autocomplete;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Autocomplete implements IAutocomplete {


    private Node rootNode;
    private int numSuggest;
    
    /*
     * This the constructor for the class
     */

    public Autocomplete() {
        this.numSuggest = 0;
        this.rootNode = new Node("" , 0);
    }
 
    /**
     * Adds a new word with its associated weight to the Trie, by calling 
     * addHelper(), a recursive function that checks every letter of the
     * word
     * 
     * @param word the word to be added to the Trie
     * @param weight the weight of the word
     */
    @Override
    public void addWord(String word, long weight) {
        
        
        if (word == "" || !(word instanceof String)) {
            return;
        }
        
        //check a - z
        for (int i = 0; i < word.length(); i++) {
            int tmp = word.charAt(i);
            if (tmp < 97 || tmp > 122) {
                return;
            }
        }
        
        Node tmpNode = this.getRootNode();
        tmpNode.setPrefixes(tmpNode.getPrefixes() + 1);

        if (word.length() > 0) {
            //process letter by letter
            addHelper(word, weight, tmpNode.getReference(), word);
        }
    }
    
    /*
     * This is a helper function for addWord() method. This will take in the word,
     * weight of the word, an array of nodes, and the actual word for creating the term
     * 
     * @param word that will be traversed
     * @param weight of the word
     * @param array of the current node 
     * @param actual word 
     *
     */
    private void addHelper(String word, long weight, Node[] array, String actualWord) {
        if (word.length() > 0) {

            //index 
            int letterIndex =  word.charAt(0) - 97;
            //If array spot is null 
            if (array[letterIndex] == null) {
                //TWO CASES

                //case 1 last letter, then create Node with (word,weight) 
                if (word.length() == 1) {
                    Node lastNode = new Node(actualWord, weight);
                    lastNode.setPrefixes(1);
                    lastNode.setWords(1);
                    array[letterIndex] = lastNode;

                    //whole word  is: " + lastNode.getTerm().getTerm());

                    //case 2 not last letter, create a Node with term = null, word = 0, p = 1    
                } else {
                    Node tmpNode = new Node();
                    tmpNode.setPrefixes(1);
                    tmpNode.setWords(0);
                    array[letterIndex] = tmpNode;

                    addHelper(word.substring(1), weight, tmpNode.getReference(), actualWord); 
                }
                //If array contains something     
            } else { 

                //If not null

                //existing node that did not store, now must store word since word.length == 1
                if (word.length() == 1) {
                    Term newTerm = new Term(actualWord, weight);
                    array[letterIndex].setTerm(newTerm);

                    //update prefix
                    int prefix = array[letterIndex].getPrefixes();
                    array[letterIndex].setPrefixes(prefix + 1);

                    //update words
                    int words = array[letterIndex].getWords();
                    array[letterIndex].setPrefixes(words + 1); //words should be zero 


                    //Exising node and word.length > 1
                } else {
                    int prefix = array[letterIndex].getPrefixes();
                    array[letterIndex].setPrefixes(prefix + 1);
                    addHelper(word.substring(1), weight, 
                            array[letterIndex].getReference(), actualWord);
                }
            }  
        }
    }

    /**
     * Initializes the Trie and calls parseAndInsert() to break line read in
     * to obtain the weight converted into int and the string word 
     *
     * @param filename the file to read all the autocomplete data from each line
     *                 contains a word and its weight This method will call the
     *                 addWord method
     * @param k the maximum number of suggestions that should be displayed 
     * @return the root of the Trie You might find the readLine() method in
     *         BufferedReader useful in this situation as it will allow you to
     *         read a file one line at a time.
     */

    @Override
    public Node buildTrie(String filename, int k) {
        this.numSuggest = k;
        
        File file = new File(filename);
        long weight;
        String query;

        try {
            Reader reader = new FileReader(file);
            BufferedReader a = new BufferedReader(reader);
            String hold;
            try {
                hold = a.readLine();
                while (hold != null) {

                    if (!hold.isEmpty()) {
                        String[] thisLine = parseAndInsert(hold);

                        if (thisLine != null) {
                            weight = Long.parseLong(thisLine[0]);
                            query = thisLine[1].trim().toLowerCase();

                          
                            //==============
                            //add words here 
                            addWord(query, weight);
                            //==============
                        }
                    }
                    hold = a.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (FileNotFoundException e) {
            System.exit(-1);        
        }
        return this.rootNode;
    }

    /*
     * This is a helper function for buildTrie(). This method basically 
     * reads in the line and if it contains a tab or "\t", it will parse it, 
     * remove the spaces and return the strings. 
     * 
     * @param line, the read in line
     * @return String[] containing the strings of the current line
     */
    private String[] parseAndInsert(String line) {
        
        if (line.contains("\t")) {
            
            line = line.trim();
           
            String[] dump = line.split("\t");
            return dump;
        }
        return null;

    }
 

    /**
     * @return k the the maximum number of suggestions that should be displayed 
     */
    
    @Override
    public int numberSuggestions() {
        return this.numSuggest;
    }

    /**
     * This method returns the root that has the last letter of the prefix. This method 
     * will call getHelper(), which recursively traverses the trie until it gets
     * to the last letter of the prefix and gets that corresponding root  
     * 
     * @param prefix
     * @return the root of the subTrie corresponding to the last character of
     *         the prefix.
     */  
    @Override
    public Node getSubTrie(String prefix) { 
        if (prefix == null) {
            return null;
        }
        
        if (prefix == "") {
            return this.rootNode;
        }
         
        //check a - z
        for (int i = 0; i < prefix.length(); i++) {
            int tmp = prefix.charAt(i);
            if (tmp < 97 || tmp > 122) {
                return null;
            }
        } 
        
        Node root = this.rootNode;        
        Node subRootPrefix = getHelper(prefix, root);

        return subRootPrefix;
    }

    /*
     * This helper function is used to traverse the trie until it gets
     * to the last letter of the prefix and gets that corresponding root  
     * 
     * @param prefix the actual string of the prefix 
     * @param the node to check for its array of nodes for the next letter of prefix 
     * @return the root that has the last letter as the prefix 
     */
    
    private Node getHelper(String prefix, Node node) {
        if (node == null) {
            return null;
        }
        if (prefix.length() == 1) {
            return node.getReference()[prefix.charAt(0) - 97];
        } else {
            return getHelper(prefix.substring(1), node.getReference()[prefix.charAt(0) - 97]);
        }
    }
 
    /**
     * This method will basically retrieve the count or the number of words that share
     * that prefix 
     * 
     * @param prefix
     * @return the number of words that start with prefix.
     */
    @Override
    public int countPrefixes(String prefix) {
        //Originally had this prefix "" return 0;
        
        if (prefix == null) {
            return 0;
        }
         
        //check a - z
        for (int i = 0; i < prefix.length(); i++) {
            int tmp = prefix.charAt(i);
            if (tmp < 97 || tmp > 122) {
                return 0;
            }
        }  
        
        Node prefixNode = getSubTrie(prefix);
       
        if (prefixNode == null) {
            return 0;
        } 
        
        int tmp = prefixNode.getPrefixes();
        return tmp;
    }
  
    
    /**
     * This method returns a list of words that start with the given prefix by calling
     * getSubTrie to get the node that has the last letter of the prefix and adds
     * every word to the List if subsequent nodes have a prefix. This method has a helper
     * function, which is suggestHelper() 
     * 
     * @param prefix
     * @return a List containing all the ITerm objects with query starting with
     *         prefix. Return an empty list if there are no ITerm object starting
     *         with prefix.
     */

    @Override
    public List<ITerm> getSuggestions(String prefix) {
        
        List<ITerm> container = new ArrayList<ITerm>();
        //empty string
       
        if (prefix == null) {
            return container;
        }
        
        //check a - z
        for (int i = 0; i < prefix.length(); i++) {
            int tmp = prefix.charAt(i);
            if (tmp < 97 || tmp > 122) {
                return container;
            }
        }  
        
        Node tmp = getSubTrie(prefix);
        
        if (tmp != null) {
            
            if (tmp.getTerm() != null && !tmp.getTerm().getTerm().equals("")) {
                container.add(tmp.getTerm());
            }
            
            for (int i = 0; i < 26; i++) {
                if (tmp.getReference()[i] != null) {
                    suggestHelper(tmp.getReference()[i], container);
                }
            } 
        }

        Collections.sort(container);

        return container;
    }
    
    /*
     * This method is a helper function for getSuggestions()
     * 
     * @param the node that has the last letter of the prefix 
     * @param container is the list that stores all the string words of the terms
     */
    private void suggestHelper(Node node, List<ITerm> container) {
        if (node.getTerm() != null) {
            container.add(node.getTerm());
        }

        for (int i = 0; i <= 25; i++) {
            if (node.getReference()[i] != null) {
                suggestHelper(node.getReference()[i], container);
            }
        } 
    }
    
    /*
     * This returns the root Node 
     * @return root node of the trie
     */
    protected Node getRootNode() {
        return rootNode;
    }

    /*
     * This method sets the root Node
     * 
     * @param root node of the trie 
     */
    protected void setRootNode(Node rootNode) {
        this.rootNode = rootNode;
    }

}




