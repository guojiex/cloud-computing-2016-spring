import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Iterator;

public class Q8Answer {
	private static HashSet<String> film = new HashSet<String>();
	private static HashSet<String> tvSeries = new HashSet<String>();

	public static void main(String[] args) {
		try {
			// Construct BufferedReader from InputStreamReader
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			String line = null;

			while ((line = br.readLine()) != null) {
				handleLine(line);
			}

			br.close();
			
			int count = 0;
            for(String s1:film){
                for(String s2:tvSeries){
                    if(s1.split("_\\(")[0].equals(s2.split("_\\(")[0]))
                    {
                        System.out.println(s1);
                        count++;
                    }
                }
            }
			System.out.println(count);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void handleLine(String line) {
		String[] words = line.split("\t");

		if (words[1].endsWith("film)")) {
			film.add(words[1]);//words[1].split("_\\(")[0]);
			return;
		}

		if (words[1].endsWith("TV_series)")) {
			tvSeries.add(words[1]);//.split("_\\(")[0]);
			return;
		}
		return;
	}
}
