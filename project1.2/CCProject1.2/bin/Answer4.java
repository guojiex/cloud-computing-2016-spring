import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Answer4 {
    public static void main(String[] args) {
        try {
            File file = new File("output");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String input;
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\t");
                    //if the current passage is 0 view on the first day of this month
                    if (columns[2].split(":")[1].equals("0")) {
                        //output the title and month view for bash to sort
                        System.out.println(String.format("%s\t%s", columns[1],columns[0]));
                    }
                }
            }
            
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
