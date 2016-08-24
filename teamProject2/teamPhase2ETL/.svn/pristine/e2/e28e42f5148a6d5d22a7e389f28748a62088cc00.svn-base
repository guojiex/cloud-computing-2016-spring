import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MapperLocalWash {
    private final SimpleDateFormat inputDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.ENGLISH);
    private final SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final TimeZone pst = TimeZone.getTimeZone("Etc/GMT+0");
    private Set<String> stopWordSet = null;
    private Set<String> censorSet = null;

    public MapperLocalWash() throws MalformedURLException {
        this.stopWordSet = this.getCommaSetFromFile(
                "https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/commonenglishword.txt");
        this.censorSet = this
                .getSetFromFile("https://s3.amazonaws.com/mylittlerpony/teamproject/phase1/java/banneddecryptword.txt");
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
                set.add(tempString.toLowerCase());
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
    public Set<String> getCommaSetFromFile(String file) throws MalformedURLException {
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
                    if (!part.isEmpty())
                        set.add(part.toLowerCase());
            }
            reader.close();
            inr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }

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
        MapperLocalWash mapper = null;
        try {
            mapper = new MapperLocalWash();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
//        int count = 0;
//        int line = 0;
//        try (BufferedReader br = new BufferedReader(new FileReader("./src/main/resources/part-00000"))) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            // BufferedReader br = new BufferedReader(new
            // InputStreamReader(System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input == null || input.isEmpty())
                    continue;
                String output = mapper.parseALine(input);
                if (output != null && !output.isEmpty()) {
//                    count++;
//                    System.out.println(count);
//                    System.out.println(line);
                    System.out.println(output);
//                    if (count == 2)
//                        break;
                }
//                line++;
            }
        } catch (IOException io) {
            io.printStackTrace();
        }
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

    private boolean isLineValid(JSONObject target) {
        Object id = target.get("id");
        Object id_str = target.get("id_str");
        Object lang = target.get("lang");
        if ((id == null) && (id_str == null))
            return false;
        if ((id != null) && id.toString().isEmpty()) {
            if ((id_str != null) && id_str.toString().isEmpty()) {
                return false;
            }
        }
        if (lang == null || !lang.toString().equals("en"))
            return false;
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

        if (!this.getId(target).isEmpty()) {
            // sb.append(this.getId(target)).append("\t");
            sb = this.getId(target);
        } else {
            // sb.append(this.getIdStr(target)).append("\t");
            sb = this.getIdStr(target);
        }
        String text=getText(target).toString();
        if(text.isEmpty())
            return "";
        return String.format("%s\t%s\t%s\t%s", sb, this.getUserId(target), this.getDate(target),
                text);
    }

    private String getText(JSONObject target) {
        String result = target.get("text").toString();
        result = result.replace("\t", " ");
        result = result.replaceAll("\\\\", "\\\\\\\\");
        result = result.replaceAll("\"", "\\\\\"");
        result = result.replaceAll("\r", "\\\\r");
        result = result.replaceAll("\\\\r", " ");
        result = result.replaceAll("(https?|ftp):\\/\\/[^\\s/$.?#][^\\s]*", "");
        String[] words = result.split("[^a-zA-Z0-9]");
        TreeMap<String, Integer> countMap = new TreeMap<>();
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.matches("[a-zA-Z0-9]+") || this.stopWordSet.contains(word.toLowerCase())
                    || this.censorSet.contains(word.toLowerCase())) {
                continue;
            }
            
            if (countMap.containsKey(word.toLowerCase())) {
                countMap.put(word.toLowerCase(), countMap.get(word.toLowerCase()) + 1);
            } else {
                countMap.put(word.toLowerCase(), 1);
            }
        }
        for (String word : countMap.keySet()) {
            sb.append(String.format("%s:%d\t", word, countMap.get(word)));
        }
        // result = result.replace("\t", " ");
        // result = result.replaceAll("\\\\", "\\\\\\\\");
        // result = result.replaceAll("\"", "\\\\\"");
        // result = result.replaceAll("\r", "\\\\r");
        if(sb.toString().isEmpty()){
            return "";
        }
        return sb.toString().substring(0,sb.toString().length() - 1);// .replaceAll("\\n",
                                                                   // "\\\\n");
    }
}
