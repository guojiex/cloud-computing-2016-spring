import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jiexin Guo
 *
 */
public class Reducer {
    private Map<String, Integer> AFINNDataset = null;
    private Set<String> stopWordSet = null;
    private Set<String> censorMap = null;
    private HashMap<Character, Character> lookupTable = null;// WashData.createLookupTable();
    public static String rot13(String input, Map<Character, Character> table) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            sb.append(table.get(input.charAt(i)));
        }
        return sb.toString();
    }

    public static String replaceAllCharExceptFirstAndLast(String s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.charAt(0));
        for (int i = 1; i < s.length() - 1; ++i)
            sb.append("*");
        sb.append(s.charAt(s.length() - 1));
        return sb.toString();
    }
    private String censorText(String text) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9]");
        Matcher m = p.matcher(text);
        int start = 0;
        int end = 0;
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (m.start() - start > 0) {
                String temp=text.substring(start, m.start());
                if (censorMap.contains(temp.toLowerCase())) {
                    sb.append(replaceAllCharExceptFirstAndLast(temp));
                } else {
                    sb.append(temp);
                }
            }
            sb.append(text.substring(m.start(), m.end()));
            start = m.end();
            end = m.end();
        }
        if (end < text.length()) {
            String temp=text.substring(end);
            if (censorMap.contains(temp.toLowerCase())) {
                sb.append(replaceAllCharExceptFirstAndLast(temp));
            } else {
                sb.append(temp);
            }
        }
        return sb.toString();
    }


    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            String currentTweetId = null;
            while ((input = br.readLine()) != null) {
                if(input.isEmpty()||input==null)
                    continue;
                
                String[] key_value_pair = input.split("\t");

                String tweetId = key_value_pair[0];
                if (currentTweetId != null && !currentTweetId.equals(tweetId)) {// change
                                                                              // a
                                                                              // word
                    //printAMapAsOneLineOutput(input);
                    System.out.println(input);
                    //System.out.println(String.format("%s,%s", key_value_pair[0],key_value_pair[1]));
                    currentTweetId = tweetId;
                } else if (currentTweetId == null) {// the first word
                    currentTweetId = tweetId;
                    System.out.println(input);
                }else if(currentTweetId==tweetId){
                    //printAMapAsOneLineOutput(input);
                    continue;
                }
            }
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

}
