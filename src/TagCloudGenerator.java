import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Program to generate the html for a tag cloud from a given input text file
 * into a given output file. Tag cloud will be case insensitive, and the words
 * appearing in the tag cloud will be lowercase. The tag cloud will contain the
 * number of words entered by the user that appear most frequently. If the
 * number entered by the user is greater than the number of distinct words in
 * the file, the tag cloud will contain all of the distinct words in the file.
 *
 * @author Aishwarya Srivastava
 * @author Grace Rhodes
 */

public final class TagCloudGenerator {

    /**
     * Characters that will be defined as separators.
     */
    public static final String SEPARATORS = " \t\n\r,-.!?[]';:/()";

    /**
     * Comparator that will sort map pairs in decreasing order of their values.
     */
    public static final Comparator<Map.Entry<String, Integer>> COUNT_ORDER = new ValueOrder();

    /**
     * Comparator that will sort map pairs in alphabetical order of their keys.
     */
    public static final Comparator<Map.Entry<String, Integer>> WORD_ORDER = new KeyOrder();

    /**
     * stylesheet that will be used to format the tag cloud.
     */
    private static final String CSS = "http://web.cse.ohio-state.edu/software/2231/"
            + "web-sw2/assignments/projects/tag-cloud-generator/data/tagcloud.css";

    /**
     * Maximum and minimum font sizes defined in the stylesheet.
     */
    public static final int MAX_FONT_SIZE = 48, MIN_FONT_SIZE = 11;

    /**
     * Compares the integer values of the Map.Entrys according to decreasing
     * numerical order.
     */
    private static class ValueOrder
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> p1,
                Map.Entry<String, Integer> p2) {
            return p2.getValue().compareTo(p1.getValue());
        }
    }

    /**
     * Compares the String keys of the Map.Entrys according to alphabetical
     * order, disregarding case.
     */
    private static class KeyOrder
            implements Comparator<Map.Entry<String, Integer>> {
        @Override
        public int compare(Map.Entry<String, Integer> p1,
                Map.Entry<String, Integer> p2) {
            return p1.getKey().compareToIgnoreCase(p2.getKey());
        }
    }

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private TagCloudGenerator() {
    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or next single separator in the given {@code text}
     * starting at the given {@code position}.
     *
     * @param text
     *            StringBuilder representation of the text from the input file
     * @param position
     *            the starting index
     * @return the first word or single separator string found in {@code text}
     *         starting at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures
     *
     *          <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   nextWordOrSeparator is a single separator
     *          </pre>
     */
    private static String nextWordOrSeparator(String text, int position) {
        assert text != null : "Violation of: text is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int i = position;
        //if character a 'i' is a separator, return the separator
        if (SEPARATORS.contains(text.charAt(position) + "")) {
            return text.charAt(position) + "";
        } else {
            //else return the next word
            while ((i < text.length())
                    && !SEPARATORS.contains(text.charAt(i) + "")) {
                i++;
            }
        }
        return (text.substring(position, i));
    }

    /**
     * Returns a List containing a Map.Entry for each distinct word read in
     * {@code in} and its corresponding count.
     *
     * @param in
     *            the stream from which the text will be read
     * @requires {@code in} is open
     * @return a List of Map.Entrys mapping the distinct words read from
     *         {@code in} to their number of occurrences
     * @throws IOException
     *             (caught in printWordsAndCountsInHTML)
     * @ensures countMap is a List containing a Map.Entry for each distinct,
     *          case-insensitive word read from {@code in}. The key of each
     *          Map.Entry is a distinct, case-insensitive word, and the value is
     *          the word's number of occurrences.
     */
    public static List<Map.Entry<String, Integer>> countMap(BufferedReader in)
            throws IOException {

        // create map containing distinct words and their counts
        Map<String, Integer> countMap = new HashMap<>();

        String text = in.readLine();
        while (text != null) {
            int position = 0;
            while (position < text.length()) {
                String next = nextWordOrSeparator(text, position).toLowerCase();
                position = position + next.length();
                if (!SEPARATORS.contains(next)) {
                    if (countMap.containsKey(next)) {
                        countMap.put(next, countMap.get(next) + 1);
                    } else {
                        countMap.put(next, 1);
                    }
                }

            }
            text = in.readLine();
        }

        // create & return List of Map.Entries
        Set<Map.Entry<String, Integer>> countSet = countMap.entrySet();
        List<Map.Entry<String, Integer>> wordList = new LinkedList<>(countSet);

        return wordList;
    }

    /**
     * Returns a List of Map.Entrys containing the {@code numWords} words with
     * the highest counts in {@code wordList}.
     *
     * @param wordList
     *            a List of Map.Entrys mapping words to their number of
     *            occurrences, sorted in decreasing order of the words'
     *            occurrences
     * @param numWords
     *            the number of words to be included in the returned List
     * @updates wordList
     * @requires wordList is sorted in decreasing numerical order of the
     *           Map.Entries' values
     * @return a List of Map.Entrys containing the words with the highest counts
     *         in at {@code wordList}. |findTopN| = min(numWords,
     *         wordList.size()).
     */
    public static List<Map.Entry<String, Integer>> findTopN(
            List<Map.Entry<String, Integer>> wordList, int numWords) {

        List<Map.Entry<String, Integer>> topN = new LinkedList<>();

        while ((topN.size() < numWords) && (wordList.size() > 0)) {
            topN.add(wordList.remove(0));
        }

        return topN;
    }

    /**
     * Prints the HTML code for a tag cloud containing the numWords most
     * frequent words in a file named {@code input} to {@code output}.
     *
     * @param input
     *            Name of the input file from which the words are to be
     *            extracted
     * @param output
     *            Name of the output file to which the HTML code is to be
     *            printed
     * @param numWords
     *            number of distinct words to be included in the generated tag
     *            cloud
     * @ensures HTML code for a tag cloud containing the min(numWords, 'number
     *          of distinct words in input file') most frequent words in a file
     *          named {@code input} is printed to {@code output}. Each word is
     *          printed in lowercase, and its font size is proportional to its
     *          number of occurrences.
     */
    public static void printWordsAndCountsInHTML(String input, String output,
            int numWords) {
        // create output stream to output file
        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(output)));
        } catch (IOException e) {
            System.err.println("Error opening output file.");
            return;
        }

        // create input stream from input file
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(input));
        } catch (IOException e) {
            System.err.println("Error opening input file.");
            out.close();
            return;
        }

        // Create List containing Map.Entries of distinct words and their counts
        List<Map.Entry<String, Integer>> wordList;
        try {
            wordList = countMap(in);
        } catch (IOException e1) {
            System.err.println("Error reading from input file.");
            out.close();
            try {
                in.close();
            } catch (IOException e) {
                System.err.println("Error closing input file.");
            }
            return;
        }

        /*
         * If numWords is greater than the unique number of words in the file,
         * number of words in generated tag cloud = number of unique words
         */
        int titleNum = numWords;
        if (numWords > wordList.size()) {
            titleNum = wordList.size();
            System.out.println(
                    "The number of tags you entered exceeds the number of distinct words("
                            + wordList.size() + ") in " + input + ".\n" + output
                            + " now displays a tag cloud with "
                            + wordList.size() + " tags.");
        }

        // output html code
        out.println("<html>");
        out.println("<head>");
        out.println(
                "<title>Top " + titleNum + " words in " + input + "</title>");
        out.println("<link href=\"" + CSS
                + "\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("<style type=\"text/css\"></style>");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Top " + titleNum + " words in " + input + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

        if (wordList.size() > 0) {
            /*
             * Sort wordList according to decreasing numerical order of the
             * words' counts, & remove the word with the highest count, which
             * will be used later to calculate the font of each word
             */
            wordList.sort(COUNT_ORDER);
            Map.Entry<String, Integer> maxCount = wordList.remove(0);
            wordList.add(0, maxCount);

            // Extract numWords-1 most popular words from wordList
            List<Map.Entry<String, Integer>> topN = findTopN(wordList,
                    numWords);

            while (topN.size() > 0) {
                // sort topN in alphabetical order according to keys
                topN.sort(WORD_ORDER);
                Map.Entry<String, Integer> word = topN.remove(0);
                int font = ((word.getValue() * (MAX_FONT_SIZE - MIN_FONT_SIZE))
                        / maxCount.getValue()) + MIN_FONT_SIZE;
                // output word in font proportional to its count
                out.println("<span style=\"cursor:default\" class=\"f" + font
                        + "\" title=\"count: " + word.getValue() + "\">"
                        + word.getKey() + "</span>");
            }
        }
        // output closing html tags
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");

        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing input file.");
        }

        out.close();
    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */

    public static void main(String[] args) {
        // create input stream from console
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        try {
            System.out.print("Enter an input file name: ");
            String input = in.readLine();

            System.out.print("Enter an output file name: ");
            String output = in.readLine();

            System.out.print(
                    "Enter the number of words to be included in the tag cloud: ");
            String sNumWords = in.readLine();

            try {
                int numWords = Integer.parseInt(sNumWords);
                printWordsAndCountsInHTML(input, output, numWords);
            } catch (NumberFormatException e) {
                System.err.println(
                        "Error: number of words to be included in the tag cloud"
                                + " must be an integer.");
            }
        } catch (IOException e) {
            System.err.println("Error reading from file.");
        }

        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream.");
        }
    }

}
