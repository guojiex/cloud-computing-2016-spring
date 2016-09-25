import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.ini4j.Ini;
import org.ini4j.Wini;

public class AwsProject21 {
    private VmController vc;
    private HashMap<String, String> loadGenerator;
    private ArrayList<HashMap<String, String>> dataCenters = new ArrayList<>();
    private String testId;

    public AwsProject21() throws Exception {
        vc = new AwsVMController();
        this.initLG();
        Thread.sleep(1 * 60 * 1000);
        this.authenticateLoadGenerator();
        this.createDataCenter();
        Thread.sleep(2 * 60 * 1000);
        this.submitTheFirstDataCenterDNS();
        Thread.sleep(70 * 1000);//ensure the minute log is valid after the first minute
        for (int i = 0; i < 32; ++i) {
            System.out.println();
            if (this.needsToCreateANewDataCenter()) {
                this.createDataCenter();
                Thread.sleep(120 * 1000);// wait between a pair of data centers'
                                         // creation
                submitNewAddedDataCenterDNS();
            } else {// reach the rps of 4000,then exit
                break;
            }
        }
    }

    /**
     * Submit a new data center(besides the first one).
     * 
     * @throws InterruptedException
     */
    private void submitNewAddedDataCenterDNS() throws InterruptedException {
        String temp = String.format("http://%s/test/horizontal/add?dns=%s", loadGenerator.get("DNS"),
                dataCenters.get(dataCenters.size() - 1).get("DNS"));
        URL url = null;

        HttpURLConnection urlcon = null;
        try {
            url = new URL(temp);
            urlcon = (HttpURLConnection) url.openConnection();
            urlcon.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // print result
            System.out.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
            Thread.sleep(15 * 1000);
            this.submitNewAddedDataCenterDNS();
        }

    }

    /**
     * See if we need to create a new data center(see the RPS).
     * 
     * @return true if the rps is less than 3000 and needs to get new datacenter
     *         false if the rps is enough
     * @throws IOException
     * @throws InterruptedException
     */
    private boolean needsToCreateANewDataCenter() throws IOException, InterruptedException {
        String DNS = loadGenerator.get("DNS");
        String lgUrl = String.format("http://%s/log?name=test.%s.log", DNS, this.testId);
        System.out.println("fetching log from:" + lgUrl);
        URL url = new URL(lgUrl);
        HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
        urlcon.connect();
        InputStream is = urlcon.getInputStream();
        Wini ini = new Wini(is);
        Ini.Section s = null;
        while (s == null) {
            for (int i = 1; i < 33; i++) {
                if (ini.get(String.format("Minute %d", i)) == null) {
                    break;
                } else {
                    s = ini.get(String.format("Minute %d", i));
                }
            }
            if (s == null) {
                System.out.println("Minute log not valid, wait for 5s.");
                Thread.sleep(5 * 1000);
                is = urlcon.getInputStream();
                ini = new Wini(is);
            } else {
                break;
            }
        }
        // Sum the rps of per data center

        Double[] rps = this.parseLog(s.toString());
        double total = 0;
        for (Double d : rps)
        {
            total += d;
        }
        System.out.println(String.format("RPS:%.2f", total));
        if (total < 4000)
            return true;
        return false;

    }

    /**
     * Parse the log.
     * 
     * @param line
     * @return
     */
    private Double[] parseLog(String line) {
        // {dc-334206d0.eastus.cloudapp.azure.com=[444.51],
        // dc-473516d0.eastus.cloudapp.azure.com=[570.05],
        // dc-927703d1.eastus.cloudapp.azure.com=[544.56]}
        Double[] res;
        if (line.indexOf(',') == -1) {
            res = new Double[1];
            int left = line.toString().indexOf('[');
            int right = line.toString().indexOf(']');
            System.out.println(line.toString().substring(left + 1, right));
            res[0] = Double.parseDouble(line.toString().substring(left + 1, right));
        } else {
            String[] lines = line.split(",");
            res = new Double[lines.length];
            for (int i = 0; i < lines.length; ++i) {
                int left = lines[i].toString().indexOf('[');
                int right = lines[i].toString().indexOf(']');
                System.out.println(lines[i].toString().substring(left + 1, right));
                res[i] = Double.parseDouble(lines[i].toString().substring(left + 1, right));
            }
        }
        return res;
    }

    /**
     * Submit the first data center.
     * 
     * @throws InterruptedException
     */
    private void submitTheFirstDataCenterDNS() throws InterruptedException {
        String temp = String.format("http://%s/test/horizontal?dns=%s", loadGenerator.get("DNS"),
                dataCenters.get(dataCenters.size() - 1).get("DNS"));
        URL url = null;

        HttpURLConnection urlcon = null;
        try {
            url = new URL(temp);
            urlcon = (HttpURLConnection) url.openConnection();
            urlcon.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlcon.getInputStream()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // print result
            System.out.println(response.toString());
            this.testId = response.toString().substring(response.toString().indexOf("name=test")).split("\\.")[1];
            System.out.println(testId);
        } catch (IOException e) {
            e.printStackTrace();
            Thread.sleep(15 * 1000);
            this.submitTheFirstDataCenterDNS();
        }

    }

    /**
     * Authenticate the load generator with id and password.
     * 
     * @throws Exception
     */
    private void authenticateLoadGenerator() {
        String DNS = loadGenerator.get("DNS");
        String lgUrl = String.format("http://%s/password?passwd=%s&andrewid=%s", DNS,
                System.getenv("submission_password"), System.getenv("andrew_id"));
        System.out.println(lgUrl);
        URL url;

        InputStream is;
        try {
            url = new URL(lgUrl);
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            urlcon.connect();
            is = urlcon.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
            StringBuffer bs = new StringBuffer();
            String l = null;
            while ((l = buffer.readLine()) != null) {
                bs.append(l).append("/n");
            }
            System.out.println(bs.toString());
            buffer.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Thread.sleep(20*1000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            this.authenticateLoadGenerator();
        }
        
    }

    /**
     * Create a load generator.
     * 
     * @throws Exception
     */
    private void initLG() throws Exception {
        loadGenerator = vc.createLoadGenerator("ami-8ac4e9e0", "project2.1", "", "", "m3.medium", "");
        System.out.println(String.format("load generator DNS:%s", loadGenerator.get("DNS")));
    }

    /**
     * Create a data center.
     * 
     * @throws Exception
     */
    private void createDataCenter() throws Exception {
        dataCenters.add(vc.createDataCenter("ami-349fbb5e", "project2.1", "", "", "m3.medium", ""));
        System.out
                .println(String.format("datacenter dns label:%s", dataCenters.get(dataCenters.size() - 1).get("DNS")));
    }

    public static void main(String[] args) throws Exception {
        new AwsProject21();

    }

}
