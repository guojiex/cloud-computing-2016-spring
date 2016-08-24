import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

public class Reducer {
    private static Set<String> stopWordSet = null;
    private static Set<String> censorSet = null;

    /**
     * The words are separated by comma,
     * 
     * @param file
     * @return
     * @throws MalformedURLException
     */
    public Set<String> getSetFromFile(String file) throws MalformedURLException {
        URL u = new URL(file);
        Set<String> set = new HashSet<>();
        BufferedReader reader = null;
        try {
            InputStream inS=u.openStream();
            InputStreamReader inr = new InputStreamReader(inS);
            reader = new BufferedReader(inr);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                set.add(tempString.toLowerCase());
            }
            inS.close();
            reader.close();
            inr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    /**
     * The words are separated by comma,
     * 
     * @param file
     * @return
     * @throws MalformedURLException
     */
    public Set<String> getCommaSetFromFile(String file) throws MalformedURLException {
        URL u = new URL(file);
        Set<String> set = new HashSet<>();
        BufferedReader reader = null;
        try {
            InputStream inS=u.openStream();
            InputStreamReader inr = new InputStreamReader(inS);
            reader = new BufferedReader(inr);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] parts = tempString.split(",");
                for (String part : parts)
                    if (!part.isEmpty())
                        set.add(part.toLowerCase());
            }
            inS.close();
            reader.close();
            inr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }
    
    /**
     * @throws MalformedURLException
     * 
     */
    public Reducer() throws MalformedURLException {
        if (stopWordSet == null)
            stopWordSet = this.getCommaSetFromFile(
                    "https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/commonenglishword.txt");
        if (censorSet == null)
            censorSet = this.getSetFromFile(
                    "https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/banneddecryptword.txt");
    }

    public static String wordCount(String line) {
        StringBuilder sb = new StringBuilder();
        String[] words = line.split(" ");
        TreeMap<String, Integer> countMap = new TreeMap<>();
        for (String word : words) {
            if (stopWordSet.contains(word.toLowerCase()) || censorSet.contains(word.toLowerCase())) {
                continue;
            }
            if (countMap.containsKey(word.toLowerCase())) {
                countMap.put(word.toLowerCase(), countMap.get(word.toLowerCase()) + 1);
            } else {
                countMap.put(word.toLowerCase(), 1);
            }
        }
        for (String word : countMap.keySet()) {
            sb.append(String.format("%s:%d,", word, countMap.get(word)));
        }
        if (sb.toString().isEmpty()) {
            return "";
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    public static void main(String[] args) throws MalformedURLException {
        Reducer re = new Reducer();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            String currentTweetId = null;
            while ((input = br.readLine()) != null) {
                if (input.isEmpty() || input == null)
                    continue;
                // example input:
                // 466866157941960705 304622720:2014-05-15 Wondered
                // kittycharnock hiding poor girl
                String[] key_value_pair = input.split("\t");
                // String[] keys=key_value_pair[0].split(":");
                String tweetId = key_value_pair[0];
                if (currentTweetId != null && !currentTweetId.equals(tweetId)) {// change
                                                                                // a
                                                                                // word
                    String wordCount = wordCount(key_value_pair[2]);
                    if (!wordCount.isEmpty())
                        System.out.println(String.format("%s\t%s", key_value_pair[1], wordCount));
                    currentTweetId = tweetId;
                } else if (currentTweetId == null) {// the first word
                    currentTweetId = tweetId;
                    String wordCount = wordCount(key_value_pair[2]);
                    if (!wordCount.isEmpty())
                        System.out.println(String.format("%s\t%s", key_value_pair[1], wordCount));
                } else if (currentTweetId == tweetId) {
                    continue;
                }
            }
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
