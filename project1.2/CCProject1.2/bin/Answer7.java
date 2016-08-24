import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Answer7 {
    public static void main(String[] args) {
        try {
            ArrayList<String> q7List = new ArrayList<String>();
            //get titles we need
            File q7 = new File("q7");
            String input;
            BufferedReader br = new BufferedReader(new FileReader(q7));
            while ((input = br.readLine()) != null) {
                if (!input.isEmpty() && input != null)
                    q7List.add(input);
            }
            br.close();

            File file = new File("output");
            br = new BufferedReader(new FileReader(file));
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\\s+");
                    for (String title : q7List) {
                        //if this is with the title we need
                        //output the title and monthview for bash to sort
                        if (title.equals(columns[1])) {
                            System.out.println(String.format("%s\t%s", title, columns[0]));
                        }
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
