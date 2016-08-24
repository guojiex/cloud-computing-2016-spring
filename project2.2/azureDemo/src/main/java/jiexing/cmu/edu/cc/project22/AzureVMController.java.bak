package jiexing.cmu.edu.cc.project22;

import java.util.HashMap;

import com.microsoft.azure.utility.ResourceContext;
/**
 * Azure java API encapsulation.
 * @author Jiexin Guo
 *
 */
public class AzureVMController implements VmController {

    private static HashMap<String, String> tagsValue = new HashMap<>();
    private static int dataCenterNumber = 0;

    /**
     * @return the dataCenterNumber
     */
    public static int getDataCenterNumber() {
        return dataCenterNumber;
    }
    /**
     * Set project tag
     * 
     * @param tenantID
     * @param applicationID
     * @param applicationKey
     * @throws Exception
     */
    public AzureVMController(String subscriptionId, String tenantID, String applicationID, String applicationKey,
            String resourceGroupName) throws Exception {
        new AzureVMApiDemo(subscriptionId, tenantID, applicationID, applicationKey,
                resourceGroupName);
        tagsValue.put("Project", "2.1");
        System.out.println(tagsValue);
    }

    private static int loadGeneratorNumber = 0;

    /**
     * @return the loadGeneratorNumber
     */
    public static int getLoadGeneratorNumber() {
        return loadGeneratorNumber;
    }

    @Override
    public HashMap<String, String> createDataCenter(String resourceGroupName, String resourceGroupNameWithVhd,
            String vhdName, String instanceSize, String subscriptionId, String storageAccountName) throws Exception {
        String seed = String.format("%d%d", (int) System.currentTimeMillis() % 1000, (int) (Math.random() * 1000));
        String vmName = String.format("dc%sd%d", seed, dataCenterNumber);
        dataCenterNumber++;
        String sourceVhdUri = String.format("https://%s.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/%s",
                storageAccountName, vhdName.trim());
        System.out.println("Starting Data Center:");
        return this.createVM(resourceGroupName+"dc7", vmName, resourceGroupNameWithVhd, sourceVhdUri, instanceSize,
                subscriptionId, storageAccountName);

    }

    @Override
    public HashMap<String, String> createLoadGenerator(String resourceGroupName, String resourceGroupNameWithVhd,
            String vhdName, String instanceSize, String subscriptionId, String storageAccountName) throws Exception {
        String seed = String.format("%d%d", (int) System.currentTimeMillis() % 1000, (int) (Math.random() * 1000));
        String vmName = String.format("lg%sl%d", seed, loadGeneratorNumber);
        loadGeneratorNumber++;
        System.out.println("Starting Load Generator:");
        String sourceVhdUri = String.format("https://%s.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/%s",
                storageAccountName, vhdName.trim());
        return this.createVM(resourceGroupName+"lg2", vmName, resourceGroupNameWithVhd, sourceVhdUri, instanceSize,
                subscriptionId, storageAccountName);
    }
    
    @Override
    public HashMap<String, String> createVM(String resourceGroupName, String vmName, String resourceGroupNameWithVhd,
            String sourceVhdUri, String instanceSize, String subscriptionId, String storageAccountName)
                    throws Exception {

        ResourceContext context = AzureVMApiDemo.createVM(resourceGroupName, vmName, resourceGroupNameWithVhd,
                sourceVhdUri, instanceSize, subscriptionId, storageAccountName, tagsValue);
        System.out.println("Initializing Azure virtual machine:");
        System.out.println("Source VHD URL: " + sourceVhdUri);
        System.out.println("Storage account: " + storageAccountName);
        System.out.println("VM Name: " + vmName);
        HashMap<String, String> res = new HashMap<>();
        res.put("vmName", vmName);
        res.put("PublicIP", AzureVMApiDemo.checkVM(context, vmName));
        res.put("DNS", String.format("%s.eastus.cloudapp.azure.com", vmName));
        return res;
    }
    /* (non-Javadoc)
     * @see jiexing.cmu.edu.cc.project22.VmController#createLoadBalancer(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public HashMap<String, String> createLoadBalancer(String resourceGroupName, String resourceGroupNameWithVhd,
            String vhdName, String instanceSize, String subscriptionId, String storageAccountName) throws Exception {
        String seed = String.format("%d%d", (int) System.currentTimeMillis() % 1000, (int) (Math.random() * 1000));
        String vmName = String.format("lb%sl%d", seed, loadGeneratorNumber);
        loadGeneratorNumber++;
        System.out.println("Starting Load Balancer:");
        String sourceVhdUri = String.format("https://%s.blob.core.windows.net/system/Microsoft.Compute/Images/vhds/%s",
                storageAccountName, vhdName.trim());
        return this.createVM(resourceGroupName+"lb2", vmName, resourceGroupNameWithVhd, sourceVhdUri, instanceSize,
                subscriptionId, storageAccountName);
    }

//    public static void main(String[] args) throws Exception {
//        String str="<!DOCTYPE html><html><head><title>MSB Data Center</title></head><body>0.00</body></html>";
//        System.out.println(str);
//        System.out.println(str.substring(str.indexOf("<body>")).split(">")[1].split("<")[0]);
//    }
}
