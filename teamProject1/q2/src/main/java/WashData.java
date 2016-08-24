
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class WashData {
    //private static final String[] needList = { "id", "id_str", "created_at", "text", "entities" };
    private Map<String,Integer> AFINNDataset=null;
    private Set<String> stopWordSet=null;
    //private Set<String> censorSet=null;
    private HashMap<String,String> censorMap=new HashMap<>();
    private HashMap<Character,Character> lookupTable=this.createLookupTable();
    public WashData(String AFINNFilePath,String stopWordFile, String censorFile) {
        this.AFINNDataset=this.getMapFromFile(AFINNFilePath);
        this.stopWordSet=this.getSetFromFile(stopWordFile);
        //this.censorSet=this.getSetFromFile(censorFile);
        this.censorMap=censorTextHashMapCreate(this.getSetFromFile(censorFile));
    }
    public static HashMap<Character,Character> createLookupTable(){
        String input="ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String output="NOPQRSTUVWXYZABCDEFGHIJKLMnopqrstuvwxyzabcdefghijklm0123456789";
        HashMap<Character,Character> table=new HashMap<>();
        for(int i=0;i<input.length();i++)
            table.put(input.charAt(i), output.charAt(i));
        return table;
    }
    public static String rot13(String input,Map<Character,Character> table){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<input.length();i++){
            sb.append(table.get(input.charAt(i)));
        }
        return sb.toString();
    }
    public static String replaceAllCharExceptFirstAndLast(String s){
        StringBuilder sb=new StringBuilder();
        sb.append(s.charAt(0));
        for(int i=1;i<s.length()-1;++i)
            sb.append("*");
        sb.append(s.charAt(s.length()-1));
        return sb.toString();
    }
    public HashMap<String,String> censorTextHashMapCreate(Set<String> censorSet){
        HashMap<String,String> res=new HashMap<>();
        for(String s:censorSet){
            res.put(rot13(s,this.lookupTable), replaceAllCharExceptFirstAndLast(rot13(s,this.lookupTable)));
        }
        //System.out.println(res);
        return res;
    }
    public void loadJsonFile(String file) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 0;
            while ((tempString = reader.readLine()) != null) {
                
                line++;
                //if (line == 5) {
                    System.out.println(parseALine(tempString));
                  //  break;
                //}
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean isLineValid(JSONObject target){
        Object id=target.get("id");
        Object id_str=target.get("id_str");
        if((id==null)&&(id_str==null))
            return false;
        if((id!=null)&&id.toString().isEmpty()){
            if((id_str!=null)&&id_str.toString().isEmpty()){
                return false;
            }
        }
        if(id!=null)
            if(id.toString().isEmpty())
                target.remove("id");
        if(id_str!=null)
            if(id_str.toString().isEmpty())
                target.remove("id");
        
        if(target.get("created_at")==null||target.get("created_at").toString().isEmpty())
            return false;
        if(target.get("text")==null||target.get("text").toString().isEmpty())
            return false;
        if(target.get("entities")==null||target.get("entities").toString().isEmpty())
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
            return "";
        }
        if (obj == null)
            return "";
        JSONObject target = (JSONObject) obj;
        if(!this.isLineValid(target))
            return "";
        
        StringBuilder sb = new StringBuilder();
        if(!this.getId(target).isEmpty()){
            sb.append(this.getId(target)).append("\t");
        }else{
            sb.append(this.getIdStr(target)).append("\t");
        }
        sb.append(this.getUserId(target)).append("\t");
        sb.append(this.getDate(target)).append("\t");
        sb.append(this.getSentimentDensity(target)).append("\t");
        sb.append(this.getText(target).toString()).append("\t");
        sb.append(this.getHashTags(this.getEntities(target)));
        return sb.toString();
    }
    private String getText(JSONObject target){
        String result=target.get("text").toString();
        result=this.censorText(result);
        result=result.replaceAll("\\t", " ");
        return result.replaceAll("\\n", "\\\\n");
    }
    private String censorText(String text){
        StringBuilder sb=new StringBuilder();
        String[] parts=text.split("\\s+");
        for(String part:parts){
            if(this.censorMap.get(part.toLowerCase())!=null)
                part=replaceAllCharExceptFirstAndLast(part);
            sb.append(part).append(" ");
        }
        return sb.toString().substring(0, sb.toString().length()-1);
    }
    private String getHashTags(String entities){
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(entities);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        JSONObject hashtags=(JSONObject) obj;
        JSONArray hashtagArray = (JSONArray) hashtags.get("hashtags");
        Iterator i = hashtagArray.iterator();
        StringBuilder sb=new StringBuilder();
        while (i.hasNext()) {
            JSONObject res=(JSONObject) i.next();
            sb.append((String) res.get("text")).append("\t");
        }
        
        if(sb.toString().isEmpty())
            return "";
        if(sb.toString().length()<=2)
            return sb.toString();
        return sb.toString().substring(0, sb.toString().length()-1);
    }
    private String getEntities(JSONObject target){
            return target.get("entities").toString();
    }
    public Set<String> getSetFromFile(String file){
        Set<String> set=new HashSet<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
               String[] parts=tempString.split(",");
               for(String part:parts)
                   set.add(part);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return set;
    }
    public Map<String,Integer> getMapFromFile(String file){
        Map<String,Integer> map=new HashMap<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
               String[] parts=tempString.split("\\s+");
               if(parts.length==2)
                   map.put(parts[0], Integer.parseInt(parts[1]));
               else{//there is a chance that some words contains space,
                   //so we need to connect them together
                   StringBuilder sb=new StringBuilder();
                   for(int i=0;i<parts.length-1;++i){
                       sb.append(parts[i]);
                       if(i<parts.length-2)
                           sb.append(" ");
                   }
                   map.put(sb.toString(),  Integer.parseInt(parts[parts.length-1]));
               }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
    private int getEffectiveWordCount(String[] words){
        int count=0;
        for(String word:words)
            if(this.stopWordSet.contains(word))
                count++;
        return count;
    }
    private String getSentimentDensity(JSONObject target){
        String[] words=target.get("text").toString().split("[^a-zA-Z0-9]");
        int score=0;
        for(String word:words){
            if(this.AFINNDataset.containsKey(word))
                score+=this.AFINNDataset.get(word);
        }
        int EWC=this.getEffectiveWordCount(words);
        double result=(EWC==0?0:score/(double)EWC);
        return String.format("%.3f", result);//String.valueOf(score);
    }
    private String getUserId(JSONObject target){
        JSONParser parser = new JSONParser();
        Object obj = null;
        try {
            obj = parser.parse(target.get("user").toString());
            obj=parser.parse(((JSONObject)obj).get("id_str").toString());
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
        return obj.toString();
    }
    private String getDate(JSONObject target){
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy",
                Locale.ENGLISH);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        TimeZone pst = TimeZone.getTimeZone("Etc/GMT+0");
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
    private String getId(JSONObject target){
        if(target.get("id")!=null)
            return target.get("id").toString();
        return null;//must be null
    }
    private String getIdStr(JSONObject target){
        if(target.get("id_str")!=null)
            return target.get("id_str").toString();
        return null;
    }

    public static void main(String[] args) {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        WashData test=new WashData("./src/main/resources/afinn.txt"
                ,"./src/main/resources/common-english-word.txt"
                ,"./src/main/resources/banned.txt");
        test.loadJsonFile("./src/main/resources/part-00000");
    }

}
