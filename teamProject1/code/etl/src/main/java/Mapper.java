import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Mapper {
    private Map<String, Integer> AFINNDataset = null;
    private Set<String> stopWordSet = null;
    private Set<String> censorMap = null;
    private HashMap<Character, Character> lookupTable = null;// WashData.createLookupTable();

    public Mapper() throws MalformedURLException {
//        lookupTable = createLookupTable();
//        this.AFINNDataset = this
//                .getMapFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/afinn.txt");
//        this.stopWordSet = this
//                .getSetFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/commonenglishword.txt");
//        this.censorMap = this
//                .getRotSetFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/banned.txt");
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

    private boolean isLineValid(JSONObject target) {
        Object id = target.get("id");
        Object id_str = target.get("id_str");
        if ((id == null) && (id_str == null))
            return false;
        if ((id != null) && id.toString().isEmpty()) {
            if ((id_str != null) && id_str.toString().isEmpty()) {
                return false;
            }
        }
//        if (id != null)
//            if (id.toString().isEmpty())
//                target.remove("id");
//        if (id_str != null)
//            if (id_str.toString().isEmpty())
//                target.remove("id");

        if (target.get("created_at") == null || target.get("created_at").toString().isEmpty())
            return false;
        if (target.get("text") == null || target.get("text").toString().isEmpty())
            return false;
        if (target.get("entities") == null || target.get("entities").toString().isEmpty())
            return false;
        return true;
    }

    private String parseALine(String line) {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(line);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (obj == null)
            return "";
        JSONObject target = (JSONObject) obj;
        if (!this.isLineValid(target))
            return "";

        String sb = null;
        String hashtags = this.getHashTags(this.getEntities(target));
        if (hashtags.isEmpty())
            return "";

        if (!this.getId(target).isEmpty()) {
            //sb.append(this.getId(target)).append("\t");
            sb=this.getId(target);
        } else {
            //sb.append(this.getIdStr(target)).append("\t");
            sb=this.getIdStr(target);
        }
        return String.format("%s\t%s\t%s\t%s\t%s", sb,hashtags,
                this.getDate(target),this.getUserId(target),getText(target).toString());
        //return String.format("%s\t%s\t%s\t%s\t%s\t%s",sb, hashtags,this.getSentimentDensity(target),
                //this.getDate(target),this.getUserId(target),getText(target).toString());
    }

    private String getText(JSONObject target) {
        String result = target.get("text").toString();
        result = result.replace("\t", " ");
        result = result.replaceAll("\\\\", "\\\\\\\\");
        result = result.replaceAll("\"", "\\\\\"");
        result = result.replaceAll("\r", "\\\\r");
        return result.replaceAll("\\n", "\\\\n");
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

    private String getHashTags(String entities) {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(entities);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        JSONObject hashtags = (JSONObject) obj;
        JSONArray hashtagArray = (JSONArray) hashtags.get("hashtags");

        if (hashtagArray.isEmpty())
            return "";
        Iterator i = hashtagArray.iterator();
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        while (i.hasNext()) {
            JSONObject res = (JSONObject) i.next();
            if (first) {
                sb.append((String) res.get("text"));
                first = false;
            } else
                sb.append(";").append((String) res.get("text"));
        }

        if (sb.toString().isEmpty())
            return "";
        return sb.toString();
    }

    private String getEntities(JSONObject target) {
        return target.get("entities").toString();
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

    private String getSentimentDensity(JSONObject target) {
        String[] words = target.get("text").toString().split("[^a-zA-Z0-9]");
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

    private String getUserId(JSONObject target) {
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(target.get("user").toString());
            obj = parser.parse(((JSONObject) obj).get("id_str").toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        return obj.toString();
    }

    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy",
            Locale.ENGLISH);
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
    private final TimeZone pst = TimeZone.getTimeZone("Etc/GMT+0");

    private String getDate(JSONObject target) {
        outputDateFormat.setTimeZone(pst);
        String result = target.get("created_at").toString();
        try {
            Date temp = inputDateFormat.parse(result);
            result = outputDateFormat.format(temp);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    private String getId(JSONObject target) {
        if (target.get("id") != null)
            return target.get("id").toString();
        return null;// must be null
    }

    private String getIdStr(JSONObject target) {
        if (target.get("id_str") != null)
            return target.get("id_str").toString();
        return null;
    }

    public static void main(String[] args) {
        Mapper mapper = null;
        try {
            mapper = new Mapper();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input == null || input.isEmpty())
                    continue;
                String output = mapper.parseALine(input);
                if (output != null&&!output.isEmpty()) {
                    System.out.println(output);
                }
            }
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
