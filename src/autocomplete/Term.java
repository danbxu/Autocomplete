package autocomplete;
public class Term implements ITerm {

    
    private String word;
    private long weight;
    
    /*
     * This the constructor for Term
     * 
     * @param the word string
     * @param the weight of the word
     */
    Term(String term, long weight) {
        if (term == null || weight < 0) {
            throw new IllegalArgumentException();
        }
        this.word = term;
        this.weight = weight;
    }
 
    /*
     * @return word's weight
     */
    @Override
    public long getWeight() {
        return this.weight;
    }

    /*
     * @param this sets the word weight
     */
    public void setWeight(long weight) {
        this.weight = weight;
    }

    /*
     * @return this returns the word of this Term
     */
    @Override
    public String getTerm() {
        return word;
    }
    
    /*
     * @param this sets the word of this Term
     */
    public void setTerm(String term) {
        this.word = term;
    }


    /*
     * This method is used for testing and comparing 
     * two Term objects
     * 
     * @param the other Term object
     */
    @Override
    public int compareTo(ITerm that) {
        return this.getTerm().
               compareToIgnoreCase(that.getTerm());
    }
    
    /*
     * @return this returns the Term to a string 
     */
    @Override
    public String toString() {
        return this.getWeight() + "\t" + this.getTerm();
    }

}
