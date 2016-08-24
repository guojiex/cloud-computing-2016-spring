package jiexing.cmu.edu.cc.project21;

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

public class AzureProject21 {
    private VmController vc;
    HashMap<String, String> loadGenerator;
    ArrayList<HashMap<String, String>> dataCenters = new ArrayList<>();
    String subscriptionId;
    String tenantID;
    String applicationID;
    String applicationKey;
    String resourceGroupName;
    String storageAccount;
    private String testId;

    public AzureProject21(String subscriptionId, String tenantID, String applicationID, String applicationKey,
            String resourceGroupName, String storageAccount) throws Exception {
        this.subscriptionId = subscriptionId;
        this.tenantID = tenantID;
        this.applicationID = applicationID;
        this.applicationKey = applicationKey;
        this.resourceGroupName = resourceGroupName;
        this.storageAccount = storageAccount;
        vc = new AzureVMController(subscriptionId, tenantID, applicationID, applicationKey, resourceGroupName);
        this.initLG();
//        this.createDataCenter();
////
////        // wait for the load generator to set up
//        Thread.sleep(3 * 60 * 1000);
//        this.authenticateLoadGenerator();
//        this.submitTheFirstDataCenterDNS();
//        Thread.sleep(65 * 1000);// wait for the first minute's log
//        for (int i = 0; i < 32; ++i) {
//            System.out.println();
//            if (this.needsToCreateANewDataCenter()) {
//                this.createDataCenter();
//                Thread.sleep(120 * 1000);// wait between a pair of data centers'
//                                         // creation
//                submitNewAddedDataCenterDNS();
//            }else{//reach the rps of 3000,then exit
//                break;
//            }
//        }
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
     * See if we need to create a new data center(see the RPS).
     * 
     * @return true if the rps is less than 3000 and needs to get new datacenter
     *         false if the rps is enough
     * @throws IOException
     */
    private boolean needsToCreateANewDataCenter() throws IOException {
        String DNS = loadGenerator.get("DNS");
        String lgUrl = String.format("http://%s/log?name=test.%s.log", DNS, this.testId);
        System.out.println("fetching log from:" + lgUrl);
        URL url = new URL(lgUrl);
        HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
        urlcon.connect();
        InputStream is = urlcon.getInputStream();
        Wini ini = new Wini(is);
        Ini.Section s = null;
        for (int i = 1; i < 33; i++) {
            if (ini.get(String.format("Minute %d", i)) == null) {
                break;
            } else {
                s = ini.get(String.format("Minute %d", i));
            }
        }
        //Sum the rps of per data center
        Double[] rps = this.parseLog(s.toString());
        double total = 0;
        for (Double d : rps) {
            total += d;
        }
        System.out.println(String.format("RPS:%.2f", total));
        if (total < 3000)
            return true;
        return false;
    }

    /**
     * Create a load generator.
     * @throws Exception
     */
    private void initLG() throws Exception {
        loadGenerator = vc.createLoadGenerator(resourceGroupName, resourceGroupName.trim(),
                "cc15619p22lgv7-osDisk.c0410b8f-821e-4de3-b725-2a834fd10060.vhd", "Standard_D1", subscriptionId,
                storageAccount);
        System.out.println(String.format("load generator DNS:%s", loadGenerator.get("DNS")));
    }
    /**
     * Create a data center.
     * @throws Exception
     */
    private void createDataCenter() throws Exception {
        dataCenters.add(vc.createDataCenter(resourceGroupName, resourceGroupName.trim(),
                "cc15619p21dcv5-osDisk.e27faca3-f177-40ea-a740-9a1838326ae6.vhd", "Standard_A1", subscriptionId,
                storageAccount));
        System.out
                .println(String.format("datacenter dns label:%s", dataCenters.get(dataCenters.size() - 1).get("DNS")));
    }
    /**
     * Submit a new data center(besides the first one).
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
     * Submit the first data center.
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
     * @throws Exception
     */
    private void authenticateLoadGenerator() throws Exception {
        String DNS = loadGenerator.get("DNS");
        String lgUrl = String.format("http://%s/password?passwd=%s&andrewid=%s", DNS,
                System.getenv("submission_password"), System.getenv("andrew_id"));
        System.out.println(lgUrl);
        URL url = new URL(lgUrl);
        HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
        urlcon.connect();

        InputStream is = urlcon.getInputStream();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(is));
        StringBuffer bs = new StringBuffer();
        String l = null;
        while ((l = buffer.readLine()) != null) {
            bs.append(l).append("/n");
        }
        System.out.println(bs.toString());
        buffer.close();
        is.close();
        urlcon.disconnect();
    }

    /**
     * args0: resource group 
     * args1: storage account 
     * args2: image name
     * args3: subscription ID 
     * args4: tenant ID 
     * args5: application ID 
     * args6: application
     * Key
     * 
     * @throws Exception
     */
    //jiexingproject21 jiexingpro21 cc15619p21dcv5-osDisk.e27faca3-f177-40ea-a740-9a1838326ae6.vhd de4983a8-1d3c-4331-9e43-0c4b19c10735 922d5c54-3308-4a57-b2d7-60f0ddc530c3 5773cd66-5d44-492e-be73-28bf8b8494fe
    //temp
    public static void main(String[] args) throws Exception {
        new AzureProject21(args[3], args[4], args[5], args[6], args[0], args[1]);
    }

}
