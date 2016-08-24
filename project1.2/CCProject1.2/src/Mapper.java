import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Mapper {
    private final static String[] exclude_list = { "Media:", "Special:", "Talk:", "User:", "User_talk:", "Project:",
            "Project_talk:", "File:", "File_talk:", "MediaWiki:", "MediaWiki_talk:", "Template:", "Template_talk:",
            "Help:", "Help_talk:", "Category:", "Category_talk:", "Portal:", "Wikipedia:", "Wikipedia_talk:" };
    private final static String[] extension_list = { ".jpg", ".gif", ".png", ".JPG", ".GIF", ".PNG", ".txt", ".ico" };
    private final static String[] special_list = { "404_error/", "Main_Page", "Hypertext_Transfer_Protocol", "Search" };
    public static String input_file_name;
    private final static String MAPPER_OUTPUT_FORMAT = "%s\t%s,%s\n";

    public static String filter(String line, String day) {
        // use space to split for performance
        String[] columns = line.split(" ");
        // is col[1] is empty means the title is missing
        if (columns[1].isEmpty())
            return null;
        // rules
        if (!columns[0].equals("en"))
            return null;

        for (String element : exclude_list) {
            if (columns[1].startsWith(element))
                return null;
        }
        if (Character.isLetter(columns[1].charAt(0)) && Character.isLowerCase(columns[1].charAt(0)))
            return null;

        for (String element : extension_list) {
            if (columns[1].endsWith(element))
                return null;
        }
        for (String element : special_list) {
            if (columns[1].equals(element))
                return null;
        }

        // uses article name as the key, value contains date and access_time
        // only save the day of the date
        // like pagecounts-20151231-230000.gz will become 31
        // MAPPER_OUTPUT_FORMAT="%s\t%s,%s\n";
        // save this format as constant for easily change in format
        return String.format(MAPPER_OUTPUT_FORMAT, columns[1], day, columns[2]);
    }

    /**
     * buffer the output lines for better performance
     */
    private static final int BUFFER_LINE_NUMBER = 350;

    public static void main(String[] args) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            input_file_name = System.getenv("mapreduce_map_input_file");
            input_file_name="s3://cmucc-datasets/wikipediatraf/201512/pagecounts-20151201-000000";
            // String[] date = input_file_name.split("-");

            // find the index of pagecounts- in the file name
            // then we can get the day of the file
            int index = input_file_name.indexOf("pagecounts-");
            String date = input_file_name.substring(index + 17, index + 19);
            String input;
            StringBuilder sb = new StringBuilder();
            int count = 0;
            while ((input = br.readLine()) != null) {
                if (input == null || input.isEmpty())
                    continue;
                String output = filter(input, date);

                if (output != null) {
                    sb.append(output);
                    count++;
                    if (count == BUFFER_LINE_NUMBER) {
                        count = 0;
                        System.out.print(sb.toString());
                        sb.setLength(0);
                    }
                }
            }
            // if there are lines rest(line number less than the buffer line
            // number), print them
            System.out.print(sb.toString());
            br.close();
        } catch (IOException io) {
            io.printStackTrace();
        }
    }
}
