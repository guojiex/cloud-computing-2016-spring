import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Answer6 {

    public static void main(String[] args) {
        try {
            ArrayList<String> q6List = new ArrayList<String>();
            File q6 = new File("q6");
            String input;
            BufferedReader br = new BufferedReader(new FileReader(q6));
            while ((input = br.readLine()) != null) {
                if (!input.isEmpty() && input != null)
                    q6List.add(input);
            }
            br.close();
            File file = new File("output");
            br = new BufferedReader(new FileReader(file));
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\t");
                    for (String title : q6List) {
                        //if the title is what we need
                        if (title.equals(columns[1])) {
                            int maxDailyView = -1;
                            for (int i = 2; i < columns.length; ++i) {
                                if (maxDailyView < Integer.parseInt(columns[i].split(":")[1]))
                                    maxDailyView = Integer.parseInt(columns[i].split(":")[1]);
                            }
                            //get the title and the max daily view of this title,output for bash to sort
                            System.out.println(String.format("%s\t%d", title, maxDailyView));
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
