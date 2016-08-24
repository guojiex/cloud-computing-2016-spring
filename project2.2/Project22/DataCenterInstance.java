import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.lang.StringBuilder;

public class DataCenterInstance {
    private final String name;
    private final String url;
    private boolean isHealthy;

    public DataCenterInstance(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Execute the request on the Data Center Instance
     * 
     * @param path
     * @return URLConnection
     * @throws IOException
     */
    public URLConnection executeRequest(String path) throws IOException {
        URLConnection conn = openConnection(path);
        return conn;
    }
    /**
     * See if this instance is health
     * @return True if it is health.
     * @throws IOException
     */
    public boolean isHealth() throws IOException {
        String path = String.format("%s:8080/lookup/random", this.url);
        URL url = new URL(path);
        HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
        try {
            urlcon.connect();
            return urlcon.getResponseCode()==200;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return the cpu usage of an instance.
     * @throws IOException
     */
    public double getCpuUsage() throws IOException {
        String path = String.format("%s:8080/info/cpu", this.url);
        URLConnection urlcon = this.executeRequest(path);
        try {
            urlcon.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            String temp=response.toString().substring(response.toString().indexOf("<body>")).split(">")[1].split("<")[0];
            if(temp.isEmpty()){
                System.out.println(-1);
                return -1;
            }
            Double result = Double.parseDouble(temp);
            System.out.println(temp);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Open a connection with the Data Center Instance
     * 
     * @param path
     * @return URLConnection
     * @throws IOException
     */
    private URLConnection openConnection(String path) throws IOException {
        URL url = new URL(path);
        URLConnection conn = url.openConnection();
        conn.setDoInput(true);
        conn.setDoOutput(false);
        conn.setConnectTimeout(1000);
        conn.setReadTimeout(1000);
        return conn;
    }
}
