import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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

public class LocalMapper2 {

    /**
     * @param args
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws MalformedURLException {
        //LocalMapper2 localMapper = new LocalMapper2();
        long rowkey = 1;
        try {
            File writename = new File("tweetrawtextwithrow.csv");
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input.isEmpty() || input == null)
                    continue;

                String[] words = input.split("\t");
                String[] hashtags = words[1].split(";");
                out.write(String.format("%d\t%s\n", rowkey, words[2]));
                for (String hashtag : hashtags) {
                    System.out.println(String.format("%s%s\t%d", words[0], hashtag, rowkey));
                }
                rowkey++;
                if(rowkey%500==0){
                    out.flush();
                }
            }
            out.close();
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }
}
