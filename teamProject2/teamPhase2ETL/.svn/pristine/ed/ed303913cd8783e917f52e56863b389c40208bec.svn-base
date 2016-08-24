import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.TreeMap;

import org.json.simple.JSONObject;

public class WashAfterReducer {
    public static void addWordCountToHashMap(TreeMap<String, Integer> map, String line) {
        String[] wordCountPair = line.split(",");
        for (String pair : wordCountPair) {
            if (pair != null && !pair.isEmpty()) {
                String[] wordAndCount = pair.split(":");
                if (map.containsKey(wordAndCount[0])) {
                    map.put(wordAndCount[0], map.get(wordAndCount[0]) + Integer.parseInt(wordAndCount[1]));
                } else {
                    map.put(wordAndCount[0], Integer.parseInt(wordAndCount[1]));
                }
            }
        }
    }

    public static String getResponseFromMap(TreeMap<String, Integer> map) {
        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            sb.append(String.format("%s:%d,", key, map.get(key)));
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }

    private final static SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyyMMdd");
    private final static TimeZone pst = TimeZone.getTimeZone("Etc/GMT+0");

    private static String getDate(String date) {
        String result = null;
        try {
            Date temp = inputDateFormat.parse(date);
            result = outputDateFormat.format(temp);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        outputDateFormat.setTimeZone(pst);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            String currentUserIdAndDate = null;
            TreeMap<String, Integer> map = new TreeMap<>();
            while ((input = br.readLine()) != null) {
                if (input.isEmpty() || input == null)
                    continue;
                // example input:
                // 304622720:2014-05-15
                // girl:1,hiding:1,kittycharnock:1,poor:1,wondered:1
                String[] key_value_pair = input.split("\t");
                String userIdAndDate = key_value_pair[0];

                if (currentUserIdAndDate != null && !currentUserIdAndDate.equals(userIdAndDate)) {// change
                    // a
                    // word
                    String[] useridAndDate = currentUserIdAndDate.split(":");
                    System.out.println(
                            String.format("%s\t%s\t%s", useridAndDate[0], getDate(useridAndDate[1]), getResponseFromMap(map)));
                    map.clear();
                    currentUserIdAndDate = userIdAndDate;
                    addWordCountToHashMap(map, key_value_pair[1]);
                } else if (currentUserIdAndDate == null) {// the first word
                    currentUserIdAndDate = userIdAndDate;
                    addWordCountToHashMap(map, key_value_pair[1]);
                } else if (currentUserIdAndDate.equals(userIdAndDate)) {
                    addWordCountToHashMap(map, key_value_pair[1]);
                }

            }
            if (!map.isEmpty()) {
                String[] useridAndDate = currentUserIdAndDate.split(":");
                System.out.println(
                        String.format("%s\t%s\t%s", useridAndDate[0], getDate(useridAndDate[1]), getResponseFromMap(map)));
            }
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }

}
