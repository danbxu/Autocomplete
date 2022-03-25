package autocomplete;

/**
 * ==== Attributes ====
 * - words: number of words
 * - term: the ITerm object
 * - prefixes: number of prefixes 
 * - references: Array of references to next/children Nodes
 * 
 * ==== Constructor ====
 * Node(String word, long weight)
 * 
 * @author Daniel Xu 
 */

public class Node {
    
    private Term thisTerm;
    //private String word;
    private Node[] childRefs;
    private int words;
    private int prefixes;

   
    /*
     * This is a node constructor when Term is null
     */
    public Node() {
        thisTerm = null;
        this.prefixes = 0; 
        this.words = 0; 
        this.childRefs = new Node[26];
    }
     
    
    /*
     * This is a node constructor when there should be a term 
     */
    public Node(String word, long weight) {
        
        thisTerm = new Term(word, weight);
        this.prefixes = 0; //This term is a prefix of how many words
        this.words = 0; //
        this.childRefs = new Node[26];
    }


    /*
     * @return the number of prefixes
     */
    protected int getPrefixes() {
        return prefixes;
    }
    
    /*
     * @param sets the number prefixes 
     */
    protected void setPrefixes(int prefixes) {
        this.prefixes = prefixes;
    }

    /*
     * @return the number of words
     */
    protected int getWords() {
        return words;
    }

    /*
     *@param sets the number of associated words
     */
    protected void setWords(int words) {
        this.words = words;
    }

    /*
     * @return this returns the Term stored in this Node object
     */
    protected Term getTerm() {
        return thisTerm;
    }
    
    /*
     *@param sets the term stored in this node 
     */
    protected void setTerm(Term wordRef) {
        this.thisTerm = wordRef;
    }
    
    /*
     * @return this returns the node array stored in this Node object 
     */
    protected Node[] getReference() {
        return this.childRefs;
    }
    
    /*
     * This sets the node array stored in this Node object
     */
    protected void setReference(Node[] array) {
        this.childRefs = array;
    }
    
    /*
     * This method is overridden for testing. 
     * 
     * @param the other object
     * @param boolean if two Nodes are the same or equal 
     */
    @Override
    public boolean equals(Object that) {
        if (that instanceof Node) {
            Node thatObject = ((Node) that);
            return this.getTerm().getTerm().
                    equals(thatObject.getTerm().getTerm()) &&
                    this.getTerm().getWeight() == 
                        thatObject.getTerm().getWeight() &&
                        this.getPrefixes() == thatObject.getPrefixes() &&
                            this.getWords() == thatObject.getWords();
        }
        return false;
        
    }

}

