import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.ini4j.Ini;
import org.ini4j.Wini;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.InstanceMonitoring;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.Tag;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricAlarm;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckResult;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Instance;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerResult;
import com.amazonaws.services.route53.model.CreateHealthCheckRequest;

/**
 * @author Jiexin Guo
 *
 */
public class awsAutoScaling {
    private VmController vc;
    private HashMap<String, String> loadGenerator;
    private AmazonEC2Client ec2;
    private final String elbSecurityGroupName = "elbSecurityGroup";
    private final String autoScalingSecurityGroupName = "asSecurityGroup";
    private String elbGroupId = null;
    private String autoScalingGroupId = null;
    private final String autoScalingGroupName = "autoScalingGroup";
    private final String loadBalancerName = "loadBalancerJie2";
    private final String scaleOutPolicyName = "scaleOutPolicy";
    private AmazonAutoScalingClient asClient;
    private final String confName = "jiexingELBConf";
    private final String scaleInstanceSize = "m3.medium";
    private final String scaleImageId = "ami-349fbb5e";
    // AmazonAutoScalingClient asClient;
    private AmazonCloudWatchClient cloudWatchClient;
    private AWSCredentials credentials = null;
    private AmazonElasticLoadBalancingClient myELB = null;
    private String testId;

    public awsAutoScaling() throws Exception {
        vc = new AwsVMController();
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                    + "Please make sure that your credentials file is at the correct "
                    + "location (/Users/apple/.aws/credentials), and is in valid format.", e);
        }
        ec2 = new AmazonEC2Client(credentials);
        cloudWatchClient = new AmazonCloudWatchClient(credentials);
        asClient = new AmazonAutoScalingClient(credentials);
        myELB = new AmazonElasticLoadBalancingClient(credentials);
        if (avZones == null) {
            avZones = new ArrayList<>();
        }
        avZones.add("us-east-1b"); // or whatever you need
        this.createSecurityGroups();
        this.initLG();
//        this.elbDNSName="loadBalancerJie2-1009152092.us-east-1.elb.amazonaws.com";
//        this.loadGenerator = new HashMap<>();
//        this.loadGenerator.put("DNS", "ec2-54-173-165-170.compute-1.amazonaws.com");
//        System.out.println("Wait 2mintues until the lg is ready.");
//        this.testId="1454633551588";
        Thread.sleep(2 * 60 * 1000);
        this.initLoadBalancer();
        this.initLaunchConfiguration();
        this.initAutoScalingGroup();
        this.defineScalingOutPolicy();
        this.defineScalingInPolicy();
        Thread.sleep(2 * 60 * 1000);
        this.authenticateLoadGenerator();
        this.warmUp();
        Thread.sleep(60 * 1000);
        for (int minute = 1; minute <= 15; minute++) {
            System.out.println(this.getLogRps());
            Thread.sleep(60*1000);
        }
        Thread.sleep(60*1000);
        this.startTest();
        Thread.sleep(60*1000);
        for (int minute = 1; minute <= 48; minute++) {
            System.out.println(this.getLogRps());
            Thread.sleep(60*1000);
        }
        //this.deleteSecurityGroups();
        this.shutDown();
    }

    private Double parseLog(String line) {
        Double res;
        int left = line.toString().indexOf('[');
        int right = line.toString().indexOf(']');
        System.out.println(line.toString().substring(left + 1, right));
        res = Double.parseDouble(line.toString().substring(left + 1, right));
        return res;
    }

    public String sendGet(String inputUrl) {
        System.out.println(String.format("GET from: %s", inputUrl));
        URL url;
        InputStream is;
        StringBuffer bs = new StringBuffer();
        try {
            url = new URL(inputUrl);
            HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
            urlcon.connect();
            is = urlcon.getInputStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is));

            String l = null;
            while ((l = buffer.readLine()) != null) {
                bs.append(l).append("/n");
            }
            System.out.println(bs.toString());
            buffer.close();
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return bs.toString();
    }

    /**
     * Authenticate the load generator with id and password.
     * 
     * @throws InterruptedException
     * 
     * @throws Exception
     */
    private void authenticateLoadGenerator() throws InterruptedException {
        String DNS = loadGenerator.get("DNS");
        String lgUrl = String.format("http://%s/password?passwd=%s&andrewid=%s", DNS,
                System.getenv("submission_password"), System.getenv("andrew_id"));
        String response = null;
        while (true) {
            response = this.sendGet(lgUrl);
            if (response == null) {
                System.out.println("No response from Load Generator, wait for 10s.");
                Thread.sleep(10 * 1000);
            } else {
                System.out.println(response);
                break;
            }
        }

    }

    // private void
    private void warmUp() throws InterruptedException {
        String url = String.format("http://%s/warmup?dns=%s", this.loadGenerator.get("DNS"), this.elbDNSName);
        String response = null;
        while (true) {
            response = this.sendGet(url);
            if (response == null) {
                System.out.println("No response from Load Generator, wait for 10s.");
                Thread.sleep(20 * 1000);
            } else {
                System.out.println(response);
                break;
            }
        }
        this.testId = response.substring(response.toString().indexOf("name=test")).split("\\.")[1];
        System.out.println(String.format("Get test id success: %s", this.testId));
    }

    public void startTest() throws InterruptedException {
        String url = String.format("http://%s/junior?dns=%s", this.loadGenerator.get("DNS"), this.elbDNSName);
        String response = null;
        while (true) {
            response = this.sendGet(url);
            if (response == null) {
                System.out.println("No response from Load Generator, wait for 15s.");
                Thread.sleep(15 * 1000);
            } else {
                System.out.println(response);
                
                break;
            }
        }
        this.testId = response.toString().substring(response.toString().indexOf("name=test")).split("\\.")[1];
        System.out.println(testId);
    }

    public double getLogRps() throws InterruptedException, IOException {
        String url2 = String.format("http://%s/log?name=test.%s.log", this.loadGenerator.get("DNS"), testId);
        System.out.println("fetching log from:" + url2);
        URL url = new URL(url2);
        HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
        urlcon.connect();
        InputStream is = urlcon.getInputStream();
        Wini ini = new Wini(is);
        Ini.Section s = null;
        while (s == null) {
            for (int i = 1; i < 60; i++) {
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
        double rps = this.parseLog(s.toString());

        return rps;
    }

    private void createSecurityGroups() {
        this.elbGroupId = this.createElbSecurityGroup();
        this.autoScalingGroupId = this.createAutoScalingSecurityGroup();
    }

    /**
     * Create a load generator.
     * 
     * @throws Exception
     */
    private void initLG() throws Exception {
        loadGenerator = vc.createLoadGenerator("ami-8ac4e9e0", "loadGeneratorSecurityGroup", "lg", "", "m3.medium", "");
        System.out.println(String.format("load generator DNS:%s", loadGenerator.get("DNS")));
    }

    public String createElbSecurityGroup() {
        return this.createSecurityGroup(elbSecurityGroupName);
    }

    public String createAutoScalingSecurityGroup() {
        return this.createSecurityGroup(autoScalingSecurityGroupName);
    }

    private String createSecurityGroup(String securityGroup) {
        CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
        csgr.withGroupName(securityGroup).withDescription("allow all incoming and all outgoing traffic on all ports");
        CreateSecurityGroupResult createSecurityGroupResult = ec2.createSecurityGroup(csgr);

        IpPermission ipPermission = new IpPermission();
        ipPermission.withIpRanges("0.0.0.0/0").withFromPort(-1).withIpProtocol("-1").withToPort(-1);

        AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();

        authorizeSecurityGroupIngressRequest.withGroupName(securityGroup).withIpPermissions(ipPermission);
        ec2.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
        System.out.println(createSecurityGroupResult.getGroupId());
        return createSecurityGroupResult.getGroupId();
    }

    public void initLaunchConfiguration() {
        CreateLaunchConfigurationRequest lcRequest = new CreateLaunchConfigurationRequest();
        lcRequest.setLaunchConfigurationName(this.confName);
        lcRequest.setImageId(this.scaleImageId);
        lcRequest.setInstanceType(this.scaleInstanceSize);

        /**
         * EC2 security groups use the friendly name VPC security groups use the
         * identifier
         */
        List<String> securityGroups = new ArrayList<>();
        securityGroups.add(this.elbSecurityGroupName);
        lcRequest.setSecurityGroups(securityGroups);

        InstanceMonitoring monitoring = new InstanceMonitoring();
        monitoring.setEnabled(Boolean.TRUE);
        lcRequest.setInstanceMonitoring(monitoring);

        asClient.createLaunchConfiguration(lcRequest);

    }

    public void initLoadBalancer() {
        Listener Test_AS_Listener = new Listener().withInstancePort(80).withInstanceProtocol("HTTP")
                .withLoadBalancerPort(80).withProtocol("HTTP");

        Collection<com.amazonaws.services.elasticloadbalancing.model.Tag> list = new ArrayList<>();
        com.amazonaws.services.elasticloadbalancing.model.Tag t = new com.amazonaws.services.elasticloadbalancing.model.Tag()
                .withKey("Project").withValue("2.1");
        //t.setKey("Project");
        //t.setValue("2.1");
        list.add(t);

        CreateLoadBalancerRequest lbReq = new CreateLoadBalancerRequest().withListeners(Test_AS_Listener)
                .withLoadBalancerName(this.loadBalancerName).withSecurityGroups(this.elbGroupId).withTags(list)
                .withAvailabilityZones(this.avZones);

        HealthCheck healthCK = new HealthCheck().withHealthyThreshold(2).withInterval(30)
                .withTarget(String.format("HTTP:80/heartbeat?lg=%s", this.loadGenerator.get("DNS"))).withTimeout(10)
                .withUnhealthyThreshold(2);

        ConfigureHealthCheckRequest healthCheckReq = new ConfigureHealthCheckRequest().withHealthCheck(healthCK)
                .withLoadBalancerName(this.loadBalancerName);
        CreateLoadBalancerResult result = myELB.createLoadBalancer(lbReq);
        ConfigureHealthCheckResult healthResult = myELB.configureHealthCheck(healthCheckReq);
        // RegisterInstancesWithLoadBalancerRequest regInst = new
        // RegisterInstancesWithLoadBalancerRequest();
        //
        // Instance inst = new
        // Instance("i-11111111").withInstanceId(this.loadBalancerName);
        // List<Instance> ins = new ArrayList<>();
        // ins.add(inst);
        // regInst.setInstances(ins);
        // RegisterInstancesWithLoadBalancerResult registerResult =
        // myELB.registerInstancesWithLoadBalancer(regInst);

        this.elbDNSName = result.getDNSName();
    }

    private String elbDNSName = null;
    private List<String> avZones;

    public void initAutoScalingGroup() {
        CreateAutoScalingGroupRequest asgRequest = new CreateAutoScalingGroupRequest();
        asgRequest.setAutoScalingGroupName(this.autoScalingGroupName);
        asgRequest.setLaunchConfigurationName(this.confName); // as above

        Collection<Tag> list2 = new ArrayList<>();
        Tag t = new Tag().withKey("Project").withValue("2.1");
        //t.setKey("Project");
        //t.setValue("2.1");
        list2.add(t);
        asgRequest.setTags(list2);

        asgRequest.setAvailabilityZones(avZones);
        asgRequest.setDesiredCapacity(3);
        asgRequest.setMinSize(2);
        asgRequest.setMaxSize(7);

        List<String> elbs = new ArrayList<>();
        elbs.add(this.loadBalancerName);
        asgRequest.setLoadBalancerNames(elbs);

        asgRequest.setHealthCheckType("ELB");
        asgRequest.setHealthCheckGracePeriod(300);
        asgRequest.setDefaultCooldown(500);
        asClient.createAutoScalingGroup(asgRequest);
    }

    public void defineScalingOutPolicy() {
        PutScalingPolicyRequest request = new PutScalingPolicyRequest();
        request.setAutoScalingGroupName(this.autoScalingGroupName);
        request.setPolicyName(scaleOutPolicyName); // This scales up so I've put
                                                   // up at the end.
        request.setScalingAdjustment(1); // scale up by one
        request.setAdjustmentType("ChangeInCapacity");

        PutScalingPolicyResult result = asClient.putScalingPolicy(request);
        String upArn = result.getPolicyARN();

        // Scale Out
        PutMetricAlarmRequest upRequest = new PutMetricAlarmRequest();
        upRequest.setAlarmName("Alarm-ScaleOut");
        upRequest.setMetricName("CPUUtilization");

        List<Dimension> dimensions = new ArrayList<>();
        Dimension dimension = new Dimension();
        dimension.setName("AutoScalingGroupName");
        dimension.setValue(this.autoScalingGroupName);
        dimensions.add(dimension);
        upRequest.setDimensions(dimensions);

        upRequest.setNamespace("AWS/EC2");
        upRequest.setComparisonOperator(ComparisonOperator.GreaterThanThreshold);
        upRequest.setStatistic(Statistic.Average);
        upRequest.setUnit(StandardUnit.Percent);
        upRequest.setThreshold(75d);
        upRequest.setPeriod(200);
        upRequest.setEvaluationPeriods(2);

        List<String> actions = new ArrayList<>();
        actions.add(upArn); // This is the value returned by the ScalingPolicy
                            // request
        upRequest.setAlarmActions(actions);
        cloudWatchClient.putMetricAlarm(upRequest);
    }

    private final String scaleInPolicyName = "scaleInPolicy";

    private void deleteSecurityGroups() {
        this.deleteSecurityGroupByName(this.elbSecurityGroupName);
        this.deleteSecurityGroupByName(this.autoScalingGroupName);
    }

    private void shutDown() {
        this.deleteSecurityGroups();
    }

    /**
     * @param sg
     */
    private void deleteSecurityGroupByName(String sg) {
        System.out.println("DELETING SECURITY GROUP " + sg);
        DeleteSecurityGroupRequest delReq = new DeleteSecurityGroupRequest().withGroupName(sg);
        try {
            this.ec2.deleteSecurityGroup(delReq);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void defineScalingInPolicy() {
        PutScalingPolicyRequest request = new PutScalingPolicyRequest();
        request.setAutoScalingGroupName(this.autoScalingGroupName);
        request.setPolicyName(scaleInPolicyName); // This scales up so I've put
                                                  // up at the end.
        request.setScalingAdjustment(-1); // scale up by one
        request.setAdjustmentType("ChangeInCapacity");

        PutScalingPolicyResult result = asClient.putScalingPolicy(request);
        String downArn = result.getPolicyARN();

        // Scale In
        PutMetricAlarmRequest downRequest = new PutMetricAlarmRequest();
        downRequest.setAlarmName("Alarm-ScaleIn");
        downRequest.setMetricName("CPUUtilization");

        List<Dimension> dimensions = new ArrayList<>();
        Dimension dimension = new Dimension();
        dimension.setName("AutoScalingGroupName");
        dimension.setValue(this.autoScalingGroupName);
        dimensions.add(dimension);
        downRequest.setDimensions(dimensions);

        downRequest.setNamespace("AWS/EC2");
        downRequest.setComparisonOperator(ComparisonOperator.LessThanThreshold);
        downRequest.setStatistic(Statistic.Average);
        downRequest.setUnit(StandardUnit.Percent);
        downRequest.setThreshold(40d);
        downRequest.setPeriod(120);
        downRequest.setEvaluationPeriods(2);

        List<String> actions = new ArrayList<>();
        actions.add(downArn); // This is the value returned by the ScalingPolicy
                              // request
        downRequest.setAlarmActions(actions);
        cloudWatchClient.putMetricAlarm(downRequest);
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        new awsAutoScaling();
    }

}
