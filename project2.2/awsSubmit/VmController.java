package jiexing.cmu.edu.cc.project22;

import java.util.HashMap;

/**
 * The interface of describing a vm create controller.
 * @author Jiexin Guo
 */
public interface VmController {
    /**
     * Create a vm, basically according to azure api.
     * 
     * @param resourceGroupName
     * @param vmName
     * @param resourceGroupNameWithVhd
     * @param sourceVhdUri
     * @param instanceSize
     * @param subscriptionId
     * @param storageAccountName
     * @return the properties of this vm
     * @throws Exception
     */
    public HashMap<String, String> createVM(String resourceGroupName, String vmName, String resourceGroupNameWithVhd,
            String sourceVhdUri, String instanceSize, String subscriptionId, String storageAccountName)
                    throws Exception;
    /**
     * Create a DataCenter vm, basically it just different from the vmName
     * @param resourceGroupName
     * @param resourceGroupNameWithVhd
     * @param vhdName
     * @param instanceSize
     * @param subscriptionId
     * @param storageAccountName
     * @return  the properties of this vm
     * @throws Exception
     */
    public HashMap<String, String> createDataCenter(String resourceGroupName, String resourceGroupNameWithVhd,
            String vhdName, String instanceSize, String subscriptionId, String storageAccountName) throws Exception;
    /**
     * Create a LoadGenerator vm, basically it just different from the vmName
     * @param resourceGroupName
     * @param resourceGroupNameWithVhd
     * @param vhdName
     * @param instanceSize
     * @param subscriptionId
     * @param storageAccountName
     * @return  the properties of this vm
     * @throws Exception
     */
    public HashMap<String, String> createLoadGenerator(String resourceGroupName, String resourceGroupNameWithVhd,
            String vhdName, String instanceSize, String subscriptionId, String storageAccountName) throws Exception;
    public HashMap<String, String> createLoadBalancer(String resourceGroupName, String resourceGroupNameWithVhd,
            String vhdName, String instanceSize, String subscriptionId, String storageAccountName) throws Exception;
}
