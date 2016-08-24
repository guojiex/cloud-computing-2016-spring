import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Answer5 {

    public static void main(String[] args) {
        int[] firstDays = new int[31];
        int[] secondDays = new int[31];
        String first = null;
        String second = null;
        try {
            File q5 = new File("q5");
            String input;
            BufferedReader br = new BufferedReader(new FileReader(q5));
            //read the first and second title that we need to consider
            while ((input = br.readLine()) != null) {
                if (first == null) {
                    first = input;
                } else {
                    second = input;
                }
            }
            br.close();

            File file = new File("output");
            br = new BufferedReader(new FileReader(file));

            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\t");
                    //if this is the passage for the first title
                    if (columns[1].equals(first)) {
                        for (int i = 2; i < columns.length; ++i) {
                            firstDays[i - 2] += Integer.parseInt(columns[i].split(":")[1]);
                        }
                    }
                    //if this is the passage for the second title
                    if (columns[1].equals(second)) {
                        for (int i = 2; i < columns.length; ++i) {
                            secondDays[i - 2] += Integer.parseInt(columns[i].split(":")[1]);
                        }
                    }
                }
            }
            int total = 0;
            for (int i = 0; i < 31; i++) {
                //see how much day is that the first one grater than the second one
                if (firstDays[i] > secondDays[i]) {
                    total++;
                }
            }
            System.out.println(total);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
