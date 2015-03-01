import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Program that counts the number of occurrences of the same word in a text file
 * and then generates a tag cloud showing the top N words that appeared in the
 * text
 * @author Jimmy Kang
 */
public class TagCloudGenerator {

    /**
     * Generates the set of characters to be used for determining separators.
     */
    private static void generateSeparators(String str, Set<Character> sepSet) {
        int index = 0;
        while (index < str.length()) {
            char sep = str.charAt(index);
            if (!sepSet.contains(sep)) {
                sepSet.add(sep);
            }
            index++;
        }
    }

    /**
     * Determines if the next word is a string or seperator
     */
    private static String nextWordOrSeparator(String text, int pos,
            Set<Character> separators) {

        int start = pos;
        char firstChar = text.charAt(pos);
        if (separators.contains(firstChar)) {
            while (pos < text.length() && separators.contains(text.charAt(pos))) {
                pos = pos + 1;
            }
        } else {
            while (pos < text.length()
                    && !separators.contains(text.charAt(pos))) {
                pos = pos + 1;
            }
        }
        return text.substring(start, pos);
    }

    /**
     * Simply prints the html headings for less clutter in the method.
     */
    public static void generateHeading(PrintWriter out, String fileName,
            int nWords) {

        out.println("<html>");
        out.println("<head>");
        out.println("<title> Top " + nWords + " words in data/" + fileName
                + "</title>");
        out.println("<link href=\"tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Top " + nWords + " words in data/" + fileName
                + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");
    }

    /**
     * Generates the body for the tag cloud html file.
     */
    public static void generateBody(PrintWriter out,
            ArrayList<Map.Entry<String, Integer>> sortedWords, int maxCount,
            int minCount) {
        for (int pos = 0; pos < sortedWords.size(); pos++) {
            Map.Entry<String, Integer> currentPair = sortedWords.remove(pos);
            int currentPairCount = currentPair.getValue();
            int fontSize = 11;
            if (currentPairCount > minCount) {
                //font size equation that we took from wikipedia
                //since this generates a font starting with size 1 we add 11 to it to get it f11 or higher
                fontSize = ((48 * (currentPairCount - minCount)) / (maxCount - minCount)) + 11;
                //since we can generate fontsizes greater than 48 we subtract 11 if its more than 48
                if (fontSize > 48) {
                    fontSize = fontSize - 11;
                }
            }
            //body html to generate the word, its count and its fontsize
            out.println("<span style=\"cursor:default\" class=\"f" + fontSize
                    + "\" title=\"count: " + currentPairCount + "\">"
                    + currentPair.getKey() + "</span>");
        }
    }

    /**
     * Generates the closing of the html file.
     */
    public static void generateClose(PrintWriter out) {
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");
    }

    /**
     * Compare strings in lexicographic order ignoring capitalization.
     */
    private static class StringLT implements
            Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            return o1.getKey().compareToIgnoreCase(o2.getKey());
        }
    }

    /**
     * Compare integers in decreasing order.
     */
    private static class IntegerLT implements
            Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> o1,
                Map.Entry<String, Integer> o2) {
            int returnCompare;
            if (o1.getValue() < o2.getValue()) {
                returnCompare = 1;
            } else if (o1.getValue() > o2.getValue()) {
                returnCompare = -1;
            } else {
                returnCompare = 0;
            }
            return returnCompare;
        }
    }

    /**
     * Takes the empty map and replaces it with the words from the text file,
     * but unsorted. This method will also generate the necessary separators to
     * ensure that the map will not contain any characters considered
     * separators
     */

    public static void generateUnsortedMap(Map<String, Integer> cloud,
            BufferedReader textFile) {

        String strSep = " ,.?/<>;:\"\"'[]{}!~`()*&^%$#@_-+=0123456789";
        Set<Character> separators = new HashSet<>();
        generateSeparators(strSep, separators);
        String currentTxtLine = "";
        try {
            currentTxtLine = textFile.readLine();
        } catch (IOException e) {
        	System.err.println("Error opening file");
        	return;
        }
        while (currentTxtLine != null) {
            int pos = 0;
            while (pos < currentTxtLine.length()) {
                String wordOrSep = nextWordOrSeparator(currentTxtLine, pos,
                        separators);
                if (!separators.contains(wordOrSep.charAt(0))) {
                    wordOrSep = wordOrSep.toLowerCase();
                    if (cloud.containsKey(wordOrSep)) {
                        cloud.put(wordOrSep, cloud.get(wordOrSep) + 1);
                    } else {
                        cloud.put(wordOrSep, 1);
                    }
                }
                pos = wordOrSep.length() + pos;
            }
            try {
            	   currentTxtLine = textFile.readLine();
            } catch (IOException e) {
            	System.err.println("No more lines");
            	return;
            }
        }
    }

    /**
     * Asks the user to input an existing text file, where the program will read
     * the text file and count up the number of instances of all the words in
     * the text file and output those results into a html file as a table.
     */
    public static void main(String[] args) throws IOException {

        Scanner keyboard = new Scanner(System.in);
        BufferedReader textFile;
        PrintWriter htmlOut;
        System.out.println("Please enter the name of the text.");
        String fileName = keyboard.nextLine();
        System.out
                .println("Enter the name of the html file you wish to import the word count to.");
        String htmlName = keyboard.nextLine();
        System.out
                .println("Please enter the number of words to be included in the tag cloud");
        int numWords = keyboard.nextInt();
        try {
        	textFile = new BufferedReader(new FileReader(fileName));
        }catch (IOException e) {
        	System.err.println("File does not exist");
        	return;
        }
              	htmlOut = new PrintWriter(new BufferedWriter(
                    new FileWriter(htmlName)));
        generateHeading(htmlOut, fileName, numWords);

        Map<String, Integer> wordAndCount = new HashMap<String, Integer>();
        generateUnsortedMap(wordAndCount, textFile);

        Comparator<Map.Entry<String, Integer>> descOrder = new IntegerLT();
        ArrayList<Map.Entry<String, Integer>> sortValue = new ArrayList<Map.Entry<String, Integer>>();
        Comparator<Map.Entry<String, Integer>> alphaB = new StringLT();

        Iterator itrMap = wordAndCount.entrySet().iterator();
        //iterates the map and adds the pairs to a arraylist
        while (itrMap.hasNext()) {
            Map.Entry<String, Integer> holdPair = (Map.Entry<String, Integer>) itrMap
                    .next();
            sortValue.add(holdPair);
            itrMap.remove();
        }
        //sorts the values in descending order
        Collections.sort(sortValue, descOrder);
        //gets the maximum and minimum counts of the list
        int maxCount = sortValue.get(0).getValue();
        int minCount = sortValue.get(sortValue.size() - 1).getValue();
        
        ArrayList<Map.Entry<String, Integer>> ltdAlphaB = new ArrayList<Map.Entry<String, Integer>>();
        //limits the number of words to what the user specified
        for (int pos = 0; pos < numWords; pos++) {
            ltdAlphaB.add(sortValue.get(pos));
        }
        //sorts the words in alphabetical order
        Collections.sort(ltdAlphaB, alphaB);

        generateBody(htmlOut, ltdAlphaB, maxCount, minCount);
        generateClose(htmlOut);
        
        try {
        	  keyboard.close();
              textFile.close();
              htmlOut.close();
        } catch (IOException e) {
        	System.err.println("Could not close file");
        	return;
        }
      
    }
}
