import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Answer8 {

    public static void main(String[] args) {
        try {
            File file = new File("output");
            ArrayList<String> filmList = new ArrayList<String>();
            ArrayList<String> tvList = new ArrayList<String>();
            String input;
            BufferedReader br = new BufferedReader(new FileReader(file));
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\t");
                    // find if it is title for tv
                    // if so, store it
                    if (columns[1].endsWith("TV_series)")) {
                        tvList.add(columns[1]);
                    }
                    // find if it is title for film
                    // if so, store it
                    if (columns[1].endsWith("film)")) {
                        filmList.add(columns[1]);
                    }
                }
            }
            br.close();
            int total = 0;
            for (String tv : tvList) {
                for (String film : filmList) {
                    //if the title of tv has cosresponding for film, add a count
                    if (tv.split("_\\(")[0].equals(film.split("_\\(")[0])) {
                        System.out.println(tv);
                        ++total;
                    }
                }
            }
            System.out.println(total);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
