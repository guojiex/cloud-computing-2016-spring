import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/**
 * A program that reads lines in and output as only one line 
 * @author Jiexin Guo
 */
public class Answer6_2 {

    public static void main(String[] args) {
        try {
            File file = new File("temp.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String input;
            StringBuilder sb=new StringBuilder();
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\t");
                    sb.append(columns[0]).append(",");
                }
            }
            //get rid of the last comma that is not fit for the formatting
            System.out.println(sb.toString().substring(0, sb.length()-1));
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
