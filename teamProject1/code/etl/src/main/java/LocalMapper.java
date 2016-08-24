import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LocalMapper {
    private Map<String, Integer> AFINNDataset = null;
    private Set<String> stopWordSet = null;
    private Set<String> censorMap = null;
    private HashMap<Character, Character> lookupTable = null;// WashData.createLookupTable();
    public LocalMapper() throws MalformedURLException{
      lookupTable = createLookupTable();
      this.AFINNDataset = this
              .getMapFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/afinn.txt");
      this.stopWordSet = this
              .getSetFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/commonenglishword.txt");
      this.censorMap = this
              .getRotSetFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/banned.txt");
    }
    public Set<String> getRotSetFromFile(String file) throws MalformedURLException {
        URL u = new URL(file);
        Set<String> set = new HashSet<>();
        BufferedReader reader = null;
        try {
            InputStreamReader inr = new InputStreamReader(u.openStream());
            reader = new BufferedReader(inr);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                 set.add(Mapper.rot13(tempString.toLowerCase(), this.lookupTable));
            }
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
    public Set<String> getSetFromFile(String file) throws MalformedURLException {
        URL u = new URL(file);
        Set<String> set = new HashSet<>();
        BufferedReader reader = null;
        try {
            InputStreamReader inr = new InputStreamReader(u.openStream());
            reader = new BufferedReader(inr);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] parts = tempString.split(",");
                for (String part : parts)
                    if(!part.isEmpty())
                        set.add(part.toLowerCase());
            }
            reader.close();
            inr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

    public Map<String, Integer> getMapFromFile(String file) throws MalformedURLException {
        URL u = new URL(file);
        Map<String, Integer> map = new HashMap<>();
        BufferedReader reader = null;
        try {
            InputStreamReader inr = new InputStreamReader(u.openStream());
            reader = new BufferedReader(inr);
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] parts = tempString.split("\\s+");
                if (parts.length == 2)
                    map.put(parts[0], Integer.parseInt(parts[1]));
                else {// there is a chance that some words contains space,
                      // so we need to connect them together
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < parts.length - 1; ++i) {
                        sb.append(parts[i]);
                        if (i < parts.length - 2)
                            sb.append(" ");
                    }
                    map.put(sb.toString(), Integer.parseInt(parts[parts.length - 1]));
                }
            }
            reader.close();
            inr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
    public HashMap<Character, Character> createLookupTable() {
        String input = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String output = "NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm0123456789";
        HashMap<Character, Character> table = new HashMap<>();
        for (int i = 0; i < input.length(); i++)
            table.put(input.charAt(i), output.charAt(i));
        return table;
    }
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
    private int getEffectiveWordCount(String[] words) {
        int count = 0;
        int wordCount = 0;
        for (String word : words) {
            if (word.matches("[a-zA-Z0-9]+")) {
                wordCount++;
            }
            if (this.stopWordSet.contains(word.toLowerCase())) {
                count++;
            }
        }
        return wordCount - count;
    }
    private String getSentimentDensity(String text) {
        String[] words = text.split("[^a-zA-Z0-9]");
        int score = 0;
        for (String word : words) {
            if (this.AFINNDataset.containsKey(word.toLowerCase())) {
                score += this.AFINNDataset.get(word.toLowerCase());
            }
        }
        int EWC = this.getEffectiveWordCount(words);
        double result = (EWC == 0 ? 0 : score / (double) EWC);
        return String.format("%.3f", result);
    }
    /**
     * @param args
     * @throws MalformedURLException 
     */
    public static void main(String[] args) throws MalformedURLException {
        LocalMapper localMapper=new LocalMapper();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            String currentTweetId = null;
            while ((input = br.readLine()) != null) {
                if(input.isEmpty()||input==null)
                    continue;
                
                String[] key_value_pair = input.split("\t");
                String twitterId=key_value_pair[0];
                String hashtags=key_value_pair[1];
                String date=key_value_pair[2];
                String userId=key_value_pair[3];
                String text=key_value_pair[4];
                System.out.println(String.format("%s\t%s\t%s:%s:%s:%s",userId,
                        hashtags,localMapper.getSentimentDensity(text.replaceAll("\\\\n", "\n")),
                        date,twitterId,
                        localMapper.censorText(text.replaceAll("\\\\n", "\n"))).replaceAll("\\n", "\\\\n"));
            }
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }
}
