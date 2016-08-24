/**
 * 
 */
package jiexing.cmu.edu.cc.project22;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author apple
 */
public class AzureProject22 {
    private final String subscriptionID;
    private final String tenantID;
    private String applicationID;
    private String applicationKey;
    private VmController vc;
    HashMap<String, String> loadGenerator;
    HashMap<String, String> loadBalancer;
    private String resourceGroupName;
    private String storageAccount;
    ArrayList<HashMap<String, String>> dataCenters = new ArrayList<>();

    public AzureProject22(String resourceGroupName, String storageAccount) throws Exception {
        this.subscriptionID = System.getenv("subscriptionID");
        this.tenantID = System.getenv("tenantID");
        this.applicationID = System.getenv("applicationID");
        this.applicationKey = System.getenv("applicationKey");
        this.resourceGroupName = resourceGroupName;
        this.storageAccount = storageAccount;
        vc = new AzureVMController(subscriptionID, tenantID, applicationID, applicationKey, resourceGroupName);
        System.out.println(String.format("subscriptionID:%s\ntenantID:%s\n", subscriptionID, tenantID));
        //this.initLG();
       // for (int i = 0; i < 3; i++) {
           // this.initDC();
//        }
        //this.initLB();
    }

    /**
     * Create a load generator.
     * 
     * @throws Exception
     */
    private void initLG() throws Exception {
        loadGenerator = vc.createLoadGenerator(resourceGroupName, resourceGroupName.trim(),
                "cc15619p22lgv7-osDisk.c0410b8f-821e-4de3-b725-2a834fd10060.vhd", "Standard_D1", subscriptionID,
                storageAccount);
        System.out.println(String.format("load generator DNS:%s", loadGenerator.get("DNS")));
    }

    private void initDC() throws Exception {
        dataCenters.add(vc.createDataCenter(resourceGroupName, resourceGroupName.trim(),
                "cc15619p22dcv6-osDisk.b0c453f3-f75f-4a2d-bd9c-ae055b830124.vhd", "Standard_A1", subscriptionID,
                storageAccount));
        System.out
                .println(String.format("datacenter dns label:%s", dataCenters.get(dataCenters.size() - 1).get("DNS")));
    }

    private void initLB() throws Exception {
        this.loadBalancer = vc.createLoadBalancer(resourceGroupName, resourceGroupName.trim(),
                "cc15619p22lbv2-osDisk.1cf68388-ac67-4165-bec0-67341257d50a.vhd", "Standard_D1", subscriptionID,
                storageAccount);
        System.out.println(String.format("load balancer DNS:%s", this.loadBalancer.get("DNS")));
    }

    /**
     * args[0] resourceGroupName args[1] storageAccount
     */
    public static void main(String[] args) {
        try {
            new AzureProject22(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
