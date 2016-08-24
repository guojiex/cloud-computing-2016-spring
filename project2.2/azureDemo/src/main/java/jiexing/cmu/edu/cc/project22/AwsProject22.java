package jiexing.cmu.edu.cc.project22;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class AwsProject22 {
    private VmController vc;
    private HashMap<String, String> loadGenerator;
    private ArrayList<HashMap<String, String>> dataCenters = new ArrayList<>();
    private String testId;
    private HashMap<String, String> loadBalancer;
    public AwsProject22() throws Exception {
        vc = new AwsVMController();
//        this.initLG();
//        this.initLB();
//        for(int i=0;i<2;i++){
            this.initDataCenter();
//        }
       
    }


    /**
     * Create a load generator.
     * 
     * @throws Exception
     */
    private void initLG() throws Exception {
        loadGenerator = vc.createLoadGenerator("ami-0d4e6067", "project2.2", "", "", "m3.medium", "");
        System.out.println(String.format("load generator DNS:%s", loadGenerator.get("DNS")));
    }
    private void initLB() throws Exception{
        this.loadBalancer=vc.createLoadBalancer("ami-f44c629e", "project2.2", "", "", "m3.medium", "");
        System.out.println(String.format("load balancer DNS:%s", loadGenerator.get("DNS")));
    }
    /**
     * Create a data center.
     * 
     * @throws Exception
     */
    private void initDataCenter() throws Exception {
        dataCenters.add(vc.createDataCenter("ami-6f486605", "project2.2", "", "", "m3.medium", ""));
        System.out
                .println(String.format("datacenter dns label:%s", dataCenters.get(dataCenters.size() - 1).get("DNS")));
    }

    public static void main(String[] args) throws Exception {
//        new AwsProject22();

    }

}
