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

public class LocalMapper3 {

    /**
     * @param args
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws MalformedURLException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String input;
            while ((input = br.readLine()) != null) {
                if (input.isEmpty() || input == null)
                    continue;
                String[] words = input.split("\t");
                String[] hashtags = words[1].split(";");
                for (String hashtag : hashtags) {
                    System.out.println(String.format("%s%s\t%s", words[0],hashtag,words[2]));
                }
            }
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }

    }
}
