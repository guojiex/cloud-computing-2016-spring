import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Answer3 {
    public static void main(String[] args) {
        try {
            File file = new File("output");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String input;
            int maxViewOn18 = -1;
            String title = null;
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\t");
                    //get the data of December 18, 2015 
                    int temp = Integer.parseInt(columns[19].split(":")[1]);
                    //if it is larger than the current max, the nupdate the max
                    if (temp > maxViewOn18) {
                        maxViewOn18 = temp;
                        title=columns[1];
                    }
                }
            }
            System.out.println(title + "\t" + maxViewOn18);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
