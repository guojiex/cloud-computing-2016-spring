import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Jiexin Guo
 *
 */
public class Reducer {
    private static final String OUTPUT_VIEW_FORMAT = "201512%02d:%d";

    public static void printAMapAsOneLineOutput(String title, int[] dateMap) {
        StringBuilder sb = new StringBuilder();
        long total = 0;
        //count the total month view
        for (int i = 0; i < 31; ++i) {
            total += dateMap[i];
        }
        //if the month view is less than 100000,clear the map and do not output
        if (total <= 10000) {
            java.util.Arrays.fill(dateMap, 0);
            return;
        }

        sb.append(String.format("%d\t%s\t", total, title));
        for (int i = 0; i < 31; ++i) {
            sb.append(String.format(OUTPUT_VIEW_FORMAT, i + 1, dateMap[i]));
            if (i != 30)// not the last day
                sb.append("\t");
        }

        System.out.println(sb.toString());
        java.util.Arrays.fill(dateMap, 0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            String currentTitle = null;
            int[] dateMap = new int[31];
            java.util.Arrays.fill(dateMap, 0);
            while ((input = br.readLine()) != null) {
                String[] key_value_pair = input.split("\t");

                String pageTitle = key_value_pair[0];
                String value = key_value_pair[1];

                String[] values = value.split(",");// values[0] is the day,and
                                                   // values[1] is the view
                if (currentTitle != null && !currentTitle.equals(pageTitle)) {// change
                                                                              // a
                                                                              // word
                    printAMapAsOneLineOutput(currentTitle, dateMap);
                    currentTitle = pageTitle;
                } else if (currentTitle == null) {// the first word
                    currentTitle = pageTitle;
                }
                // not matter change a word or remain or whatever,we should
                // store the day view data
                dateMap[Integer.parseInt(values[0]) - 1] += Integer.parseInt(values[1]);
            }
            // for the last line
            printAMapAsOneLineOutput(currentTitle, dateMap);
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }

}
