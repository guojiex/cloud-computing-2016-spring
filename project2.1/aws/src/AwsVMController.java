import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

/**
 * 
 */

/**
 * @author apple
 *
 */
public class AwsVMController implements VmController {
    private AmazonEC2Client ec2;

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private void init() throws Exception {
        /*
         * The ProfileCredentialsProvider will return your [default] credential
         * profile by reading from the credentials file located at
         * (/Users/apple/.aws/credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/apple/.aws/credentials), and is in valid format.", e);
        }
        ec2 = new AmazonEC2Client(credentials);
        // ec2.setEndpoint("");
    }

    public AwsVMController() throws Exception {
        this.init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see VmController#createVM(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public HashMap<String, String> createVM(String amiName, String securityGroupName, String vmName,
            String sourceVhdUri, String instanceSize, String subscriptionId, String storageAccountName)
                    throws Exception {
        RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
        runInstancesRequest.withImageId(amiName).withInstanceType(instanceSize).withMinCount(1).withMaxCount(1)
                .withSecurityGroups(securityGroupName);
        RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);

        List<Instance> instances = runInstancesResult.getReservation().getInstances();
        String instanceId = instances.get(0).getInstanceId();

        for (Instance instance : instances) {
            CreateTagsRequest createTagsRequest = new CreateTagsRequest();
            createTagsRequest.withResources(instance.getInstanceId()).withTags(new Tag("Project", "2.1"));
            ec2.createTags(createTagsRequest);

            CreateTagsRequest createTagsRequest2 = new CreateTagsRequest();
            createTagsRequest2.withResources(instance.getInstanceId()).withTags(new Tag("Name", vmName));
            ec2.createTags(createTagsRequest2);
        }

        HashMap<String, String> res = null;
        while (true) {
            res = this.getRunningInstance(instanceId);
            if (res == null) {
                System.out.println(String.format("Instance %s not running,wait 15s to get DNS.", instanceId));
                Thread.sleep(15 * 1000);
            } else {
                break;
            }
        }
        return res;
    }

    private HashMap<String, String> getRunningInstance(String instanceId) throws InterruptedException {
        HashMap<String, String> res = null;
        DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
        List<Reservation> reservations = describeInstancesRequest.getReservations();
        Set<Instance> instances1 = new HashSet<Instance>();

        for (Reservation reservation : reservations) {
            instances1.addAll(reservation.getInstances());
        }
        for (Instance i : instances1) {
            if (i.getInstanceId().equals(instanceId)) {
                if (!i.getState().getName().equals("running")) {
                    return null;
                } else {
                    res = new HashMap<>();
                    res.put("DNS", i.getPublicDnsName());
                    System.out.println(i.getPublicDnsName());
                    break;
                }
            }
        }
        return res;
    }

    private static int dataCenterNumber = 0;
    private static int loadGeneratorNumber = 0;

    /*
     * (non-Javadoc)
     * 
     * @see VmController#createDataCenter(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public HashMap<String, String> createDataCenter(String amiName, String securityGroupName, String vmName,
            String sourceVhdUri, String instanceSize, String subscriptionId) throws Exception {
        vmName = String.format("dc%d", dataCenterNumber);
        dataCenterNumber++;
        return this.createVM(amiName, securityGroupName, vmName, "", instanceSize, "", "");
    }

    /*
     * (non-Javadoc)
     * 
     * @see VmController#createLoadGenerator(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public HashMap<String, String> createLoadGenerator(String amiName, String securityGroupName, String vmName,
            String sourceVhdUri, String instanceSize, String subscriptionId) throws Exception {
        vmName = String.format("lg%d", loadGeneratorNumber);
        loadGeneratorNumber++;
        return this.createVM(amiName, securityGroupName, vmName, "", instanceSize, "", "");
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        //new AwsVMController().createVM("ami-8ac4e9e0", "project2.1", "testVM", "", "m3.medium", "", "");
    }

}
