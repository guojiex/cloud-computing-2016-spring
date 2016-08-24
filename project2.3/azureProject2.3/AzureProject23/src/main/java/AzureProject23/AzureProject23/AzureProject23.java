package AzureProject23.AzureProject23;

import java.util.ArrayList;
import java.util.HashMap;

public class AzureProject23 {
    private static final String LOAD_GENERATOR_INSTANCE_SIZE = "Standard_D1";
    private static final String DATA_CENTER_INSTANCE_SIZE= "Standard_D1";
    private static final String FRONT_END_INSTANCE_SIZE = "Standard_A0";
    private static final String DATA_CENTER_VHD_NAME = "cc15619p23dcv5-osDisk.dc552bc1-518d-451e-b856-c0419a6adcdb.vhd";
    private static final String loadGeneratorVhdName = "cc15619p23lgv4-osDisk.40d2443e-9f8c-41ce-9826-e0d7792a6c27.vhd";
    private static final String frontEndVhdName = "cc15619p23fe-osDisk.8d5f0df8-c94d-43e0-8a11-77ba440e0d8f.vhd";
    private final String subscriptionID;
    private final String tenantID;
    private final String applicationID;
    private final String applicationKey;
    private final String resourceGroupName;
    private final String storageAccount;
    private VmController vc;
    private ArrayList<HashMap<String, String>> dataCenters = new ArrayList<>();
    private HashMap<String, String> loadGenerator;

    public AzureProject23(String resourceGroupName, String storageAccount) throws Exception {
        this.subscriptionID = System.getenv("subscriptionID");
        this.tenantID = System.getenv("tenantID");
        this.applicationID = System.getenv("applicationID");
        this.applicationKey = System.getenv("applicationKey");
        this.resourceGroupName = resourceGroupName;
        this.storageAccount = storageAccount;
        vc = new AzureVMController(subscriptionID, tenantID, applicationID, applicationKey, this.resourceGroupName);
        System.out.println(String.format("subscriptionID:%s\ntenantID:%s\n", subscriptionID, tenantID));
        //this.initFrontEnd();
        this.initLG();
        this.initDataCenter();
        this.initDataCenter();
    }
    private HashMap<String, String> frontEnd;
    private void initDataCenter() throws Exception {
        dataCenters.add(vc.createDataCenter(resourceGroupName, resourceGroupName.trim(),
                DATA_CENTER_VHD_NAME, DATA_CENTER_INSTANCE_SIZE, subscriptionID,
                storageAccount));
        System.out
                .println(String.format("datacenter dns label:%s", dataCenters.get(dataCenters.size() - 1).get("DNS")));
    }

    private void initLG() throws Exception {
        loadGenerator = (vc.createLoadGenerator(resourceGroupName, resourceGroupName.trim(),
                loadGeneratorVhdName, LOAD_GENERATOR_INSTANCE_SIZE, subscriptionID,
                storageAccount));
        System.out.println(
                String.format("Load Generator dns label:%s", loadGenerator.get("DNS")));
    }
    private void initFrontEnd() throws Exception{
        this.frontEnd = (vc.createFrontEnd(resourceGroupName, resourceGroupName.trim(),
                frontEndVhdName, FRONT_END_INSTANCE_SIZE, subscriptionID,
                storageAccount));
        System.out.println(
                String.format("FrontEnd dns label:%s", this.frontEnd.get("DNS")));
    }

    public static void main(String[] args) {
        try {
            new AzureProject23(args[0], args[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
