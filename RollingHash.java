import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;


/*
 * This is the class that represents the Rolling Hash Object that does multiple various length pattern search
 * on a given text.
 * The bigO is O(m+n+p) n - length of text to be searched. m - length of shortest pattern. p - number of matches
 */
public class RollingHash {

    private Set<String> patternSet;
    private HashMap<String,String> patternHashTable;
    private Set patternHashCodeSet;
    private long patHash;    //pattern hash value
    private long MOD;          //a large prime. When mod by this MOD, we get a hashCode smaller enough to avoid memory overflow
    private int polynomialBase;
    private long CommonConstant;         // R^(M-1) % Q
    private int doubleCheckCount = 0;
    private int minPatternLength;
    private Map<Integer, String> results;

    /*
     * Constructor
     * It takes set of patterns passed in from GUI
     * it figures out the min length of the patterns and
     * creates a hash table that stored the hash code of each shortened pattern.
     */
	public RollingHash(Set<String> patternSet) {

        this.patternSet = patternSet;

        polynomialBase = 256;

        patternHashCodeSet = new HashSet<String>(); //create an empty set for storing the hashcode of all patterns

        patternHashTable = new HashMap<String,String>();

        //find shortest pattern length
        minPatternLength = findShortestPatternLength(patternSet);

        /*
         * loop thru each pattern in the set and build hashCode from each.
         * each hashCode is added to hashCode set for later matching
         */
        MOD = randomPrime();

        Iterator<String> it = patternSet.iterator();

        while(it.hasNext()) {

        	String pattern = it.next();

	        System.out.println("pattern & Length : " + pattern + " " + pattern.length() + " " + minPatternLength);

	        //only calculate the patten's hashcode up to the length of shortest pattern
	        patHash = hash(pattern.substring(0,minPatternLength), minPatternLength);

	        patternHashCodeSet.add(patHash);
	        patternHashTable.put(String.valueOf(patHash), pattern);

        }

        /*
         * This is the value in the hashCode formula that every calculation needs.
         * So we calculate it here once and store it for later use.
         * An dynamic programming concept - memorization
         */
        CommonConstant = 1;
        for (int i = 1; i <= minPatternLength - 1; i++)
        	CommonConstant = (polynomialBase * CommonConstant) % MOD;

    }

    /*
     * This is the helper method that calculates the hash code in polynomial format
     */
    private long hash(String key, int pattenLength) {
        long h = 0;
        for (int j = 0; j < pattenLength; j++) {
            h = (polynomialBase * h + key.charAt(j)) % MOD;
        }
        return h;
    }


    private boolean doubleCheckMultiple(String txt, int i, int length) {
    	doubleCheckCount++;
    	System.out.println("Deep check :" + doubleCheckCount);

    	String subString = txt.substring(i, i + length);
    	if (patternSet.contains(subString)) {
    		System.out.println("\n ------------> exact match " + subString);
    		System.out.println("\n");
    		return true;
    	}

        return false;
    }


    /*
     * This is the main method that does the multiple pattern match
     * @param text text to be searched for matching patterns
     * @return the hash table that stored the search result. it has the index location of the match and the pattern that matched at that
     * location
     */
    public Map<Integer, String> multiplePatternMatchSearch(String text) {
    	//create a hashmap to store the matched locations
    	results = new HashMap<Integer, String>();

        int textLength = text.length();

        if (textLength < minPatternLength) return null;

        //check for match at beginning of the text
        long textHash = hash(text, minPatternLength);
        //check if any of the pattern matches the substring

        if (patternHashTable.get(String.valueOf(textHash)) != null) {
        	if (doubleCheckMultiplePatternMatchWithDifferentPatternLength(text,0,minPatternLength, String.valueOf(textHash)) == true)

        		;
        }

        //check the match at text body

        for (int i = 1; i <= textLength - minPatternLength; i++) {

        	if (i + minPatternLength <= textLength) {
        	   int endIndex = i + minPatternLength;
        	}

        	int oldFirstCharIndex = i - 1;
        	int newLastCharIndex = i -1 +  minPatternLength;

        	/* doing hash code rolling. New hash is not derived from previous hash */
        	textHash = (textHash + MOD - CommonConstant   *  text.charAt(oldFirstCharIndex) % MOD) % MOD;  //subtract A to get BCb
        	//           old_hash    - base *  - A
        	textHash = (textHash * polynomialBase + text.charAt(newLastCharIndex)) % MOD;  //multiple by base + D part
        	//            above  * base           + D


            /*
             * match check by doing a lookup with pattern hash table. If return is not null then there is potential match.
             * invoke the deep match check to do character by character comparison with the original pattern to determine if it is a true match
             */

           if (patternHashTable.get(String.valueOf(textHash)) != null) {
            	if ((i + minPatternLength) < textLength) { //make sure don't go over the end of text
	            	if (doubleCheckMultiplePatternMatchWithDifferentPatternLength(text, i, minPatternLength, String.valueOf(textHash)) == true) {
	            	   ;
	            	}
            	}
            }
        }

        return results;
    }


    /*
     * This is helper method that does the character by character comparision.
     * It is called when there is hash code match on the shortened pattern.
     *
     */
    private boolean doubleCheckMultiplePatternMatchWithDifferentPatternLength(String txt, int i, int length, String hashCode) {
    	doubleCheckCount++;

    	//First, find out which pattern's hashcode is matched.
    	String patternMatched = patternHashTable.get(hashCode);

    	//create a substring that matches the real length of the pattern, not the shortened pattern.
    	int lengthDifference = patternMatched.length() - minPatternLength;
    	int newStringLength = length + lengthDifference;
    	String newSubString = txt.substring(i, i + newStringLength);

    	if (newSubString.equalsIgnoreCase(patternMatched)) {
    		results.put(i, newSubString);
    		System.out.println("------------> exact match :" + newSubString);

    		return true;
    	}

        return false;
    }

    /*
     * A helper method that generates a random 31-bit prime.
     * The hashCode can be very large and may cause overflow.
     * So we mod(%) the hashCode by this large prime number
     * to get it smaller.
     * This method is called by CommonConstant calculation
     */
    private static long randomPrime() {
        BigInteger prime = new BigInteger(31, new Random());
        return prime.longValue();
    }

    /*
     * A helper method that figures out the shortest string length
     * of all patterns.
     */
    private int findShortestPatternLength(Set<String> patternSet) {
    	int shortestLength = Integer.MAX_VALUE;

    	Iterator<String> it = patternSet.iterator();

        while(it.hasNext()) {
        	String pattern = it.next();
        	if ( pattern.length() > 0 && pattern.length() < shortestLength)
        		shortestLength = pattern.length();
        }

        System.out.println("shortest pattern length :" + shortestLength);
    	return shortestLength;
    }

    /*
     * Test driver.
     * It pre-loads text to be searched and patterns and run the search and match
     * method.
     * It prints out the match results. The matched patterns are displayed as
     * CAPITAL characters in the original text so we can tell what have matched.
     */
    public static void main(String[] args) {


        /*********** multiple pattern match test *******************/

    	//prepare some text to be searched
        String sb = new StringBuilder()
        .append("It was the best of times, it was the worst of times,\n")
        .append("it was the age of wisdom, it was the age of foolishness,\n")
        .append("it was the epoch of belief, it was the epoch of incredulity,\n")
        .append("it was the season of Light, it was the season of Darkness,\n")
        .append("it was the spring of hope, it was the winter of despair,\n")
        .append("we had everything before us, we had nothing before us")
        .toString();

        String text = sb.toString().toLowerCase();

        //prepare some patterns to be searched by
        String pat1 = "times";
        String pat2 = "wisdom";
        String pat3 = "belief";
        String pat4 = "before";
        String pat5 = "the";
        String pat6 = "age";

        //put patterns into a HashSet
        Set<String> patternSet = new HashSet<String>();
        patternSet.add(pat1);
        patternSet.add(pat2);
        patternSet.add(pat3);
        patternSet.add(pat4);
        patternSet.add(pat5);
        patternSet.add(pat6);


        //call constructor to create a match searcher

        RollingHash multiplePatternSearch = new RollingHash(patternSet);

        //call search on the entire text

        Map<Integer, String> results =  multiplePatternSearch.multiplePatternMatchSearch(text);


        //print out match result to check if everything works

        printMatchResult(results, text);

    }

    public static void printMatchResult(Map<Integer, String> results, String originalText) {

    	//check matching results
        Iterator it = results.entrySet().iterator();
        while (it.hasNext()) {
        	Map.Entry<Integer, String> result = (Entry<Integer, String>) it.next();
        	System.out.println("match " + " " + result.getKey() +  " " + result.getValue());
        }

        System.out.println("\n--------- Dislay match results -------------\n");

       //loop thru entire string and compare with the result has table to find the matched words' location and turn the font to red
        int jump = 0;
        for (int i = 0; i < originalText.length(); i++) {
        	String matchedWord = results.get(i);
        	if ( matchedWord != null) {
        		System.out.print(matchedWord.toUpperCase());
        		jump = matchedWord.length();
        	} else {
        		if (jump > 0 )
        		   i = i + jump - 1;

        		System.out.print(originalText.charAt(i));
        		jump = 0;
        	}
        }
    }


    /*
     * This method is used by the GUI to display match results
     *
     */



     public Map<Integer, String> getResults() {
 		return results;
 	}
}

