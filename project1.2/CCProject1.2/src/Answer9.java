import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Answer9 {

    public static void main(String[] args) {
        try {
            File file = new File("output");
            ArrayList<Integer> saveList = new ArrayList<Integer>();
            String input;
            BufferedReader br = new BufferedReader(new FileReader(file));
            int maxLength = -1;
            while ((input = br.readLine()) != null) {
                if (input != null && !input.isEmpty()) {
                    String[] columns = input.split("\\s+");
                    //current decreasing length
                    int currentLength = 1;
                    //the max decreasing length of this current title
                    int currentMaxLength = 1;
                    for (int i = 3; i < columns.length; ++i) {
                        
                        if (Integer.parseInt(columns[i - 1].split(":")[1]) > Integer
                                .parseInt(columns[i].split(":")[1])) {
                            currentLength++;
                            
                        } else {// not decresing
                            if (currentLength > maxLength) {
                                maxLength = currentLength;
                                
                            }
                            if (currentLength > currentMaxLength) {
                                currentMaxLength = currentLength;
                            }
                            currentLength = 1;
                        }
                    }
                    //if it is totally decreasing, we have to check at the end,
                    //or it will just not update the maxLength
                    if (currentLength > maxLength) {
                        maxLength = currentLength;
                        
                    }
                    if (currentLength > currentMaxLength) {
                        currentMaxLength = currentLength;
                    }
                    //save every max decreasing length of each title
                    saveList.add(currentMaxLength);
                }
            }
            br.close();
            int total = 0;
            for (Integer i : saveList) {
                //get how many titles' maxlength is the same as the max decreasing length
                if (i == maxLength)
                    ++total;
            }
            System.out.println(total);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
