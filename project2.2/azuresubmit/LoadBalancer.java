import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jiexing.cmu.edu.cc.project22.AzureVMController;
import jiexing.cmu.edu.cc.project22.VmController;

public class LoadBalancer {
    public class startANewDC implements Runnable {
        private VmController vc;
        String DNS = null;
        private int instanceIndex = 0;

        public startANewDC(VmController vc, int instanceIndex) {
            this.vc = vc;
            this.instanceIndex = instanceIndex;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                this.initDC();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void initDC() throws Exception {
            System.out.println("Start creating DC");
            HashMap<String, String> temp = vc.createDataCenter(resourceGroupName, resourceGroupName.trim(),
                    "cc15619p22dcv6-osDisk.b0c453f3-f75f-4a2d-bd9c-ae055b830124.vhd", "Standard_A1", subscriptionID,
                    storageAccount);

            Thread.sleep(100 * 1000);

            this.DNS = temp.get("DNS");

            DataCenterInstance te = new DataCenterInstance("new", String.format("http://%s", this.DNS));
            System.out.println(String.format("datacenter dns label:%s", this.DNS));
            instances[this.instanceIndex] = te;
            instanceStatus[this.instanceIndex] = -1;
        }
    }

    private static final int THREAD_POOL_SIZE = 4;
    private final ServerSocket socket;
    private final DataCenterInstance[] instances;
    private int[] instanceStatus;

    private final String subscriptionID;
    private final String tenantID;
    private String applicationID;
    private String applicationKey;
    private VmController vc;
    private String resourceGroupName;
    private String storageAccount;

    public LoadBalancer(ServerSocket socket, DataCenterInstance[] instances) throws Exception {
               System.out.println(String.format("subscriptionID:%s\ntenantID:%s\n", subscriptionID, tenantID));
        this.vc = new AzureVMController(subscriptionID, tenantID, applicationID, applicationKey, resourceGroupName);

        this.socket = socket;
        this.instances = instances;
        instanceStatus = new int[instances.length];
        for (int i = 0; i < instances.length; i++) {
            instanceStatus[i] = -1;
        }
    }

    public void start() throws IOException {
        // this.roundRobin();
        // this.betterLoadBalance();
        // this.withHealthCheck();
        this.withHealthCheckCpu();
    }

    private final int failThreshold = 3;

    private boolean checkHealth(int instanceIndex) {
        return this.instances[instanceIndex].isHealth();
    }

    private void checkHealth() throws IOException {
        for (int i = 0; i < instances.length; i++) {
            if (this.instanceStatus[i] == -2) {// launching new instance
                continue;
            }
            if (checkHealth(i)) {// health
                this.instanceStatus[i] = 0;
            } else {
                if (this.instanceStatus[i] == -1) {// instance not initialized
                    continue;
                } else {

                    this.instanceStatus[i]++;// count failure time

                    if (this.instanceStatus[i] >= this.failThreshold) {// if the
                                                                       // fail
                                                                       // time
                                                                       // more
                                                                       // than
                                                                       // threshold
                                                                       // ,then
                                                                       // launch
                                                                       // a
                                                                       // new DC
                        Thread t = new Thread(new startANewDC(this.vc, i));
                        t.start();
                        this.instanceStatus[i] = -2;
                    }
                }
            }
        }
    }

    public void roundRobin() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int index = 0;
        while (true) {
            // By default, it will send all requests to the first instance
            Runnable requestHandler = new RequestHandler(socket.accept(), instances[index]);
            index++;
            if (index == 3) {
                index = 0;
            }
            executorService.execute(requestHandler);
        }
    }

    public void betterLoadBalance() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        double minCpuUsage = 0;
        int minCpuIndex = 0;
        int count = 0;
        while (true) {
            if (count == 0) {// every 5 time we select the one with the least
                             // cpu usage to start our Round Robin
                minCpuUsage = instances[0].getCpuUsage();
                minCpuIndex = 0;
                for (int i = 1; i < instances.length; i++) {// find the instace
                                                            // with the least
                    // cpu
                    // usage.
                    double temp = instances[i].getCpuUsage();
                    if (minCpuUsage < 0 && temp >= 0) {// if the first machine
                                                       // is
                                                       // down
                        minCpuUsage = temp;
                        minCpuIndex = i;
                    } else if (temp >= 0 && temp < minCpuUsage) {
                        minCpuUsage = temp;
                        minCpuIndex = i;
                    }
                }
            }
            Runnable requestHandler = new RequestHandler(socket.accept(), instances[minCpuIndex]);
            minCpuIndex = (minCpuIndex == instances.length - 1 ? 0 : minCpuIndex + 1);
            executorService.execute(requestHandler);
            count++;
            if (count == 5) {
                count = 0;
            }
        }
    }

    private int getLowestUsedCpuIndex() throws IOException {
        double minCpuUsage = -1;
        int minCpuIndex = -1;
        minCpuUsage = -1;
        for (int i = 0; i < instances.length; i++) {// find the instace
                                                    // with the least
            // cpu
            // usage.
            if (this.instanceStatus[i] != 0) {
                continue;
            }
            double temp = instances[i].getCpuUsage();
            if (temp < 0) {
                continue;
            }
            if (minCpuUsage < 0 || temp < minCpuUsage) {

                minCpuUsage = temp;
                minCpuIndex = i;
            }
        }
        return minCpuIndex;
    }

    public void withHealthCheck() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int index = 0;
        int count = 0;
        int size = this.instances.length;
        this.checkHealth();
        while (true) {
            if (count == 100) {
                this.checkHealth();
                count = 0;
            }
            do {
                index = (index + 1) % size;
            } while (this.instanceStatus[index] != 0);
            Runnable requestHandler = new RequestHandler(socket.accept(), instances[index]);
            executorService.execute(requestHandler);
            count++;
        }
    }

    /**
     * Use health check and cpu usage check
     * 
     * @throws IOException
     */
    public void withHealthCheckCpu() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int index = 0;
        int count = 0;
        int size = this.instances.length;
        this.checkHealth();
        while (true) {
            if (count == 100) {
                this.checkHealth();
                count = 0;
            }

            do {
                index = (index + 1) % size;
            } while (this.instanceStatus[index] != 0);
            if (count == 25) {
                index = this.getLowestUsedCpuIndex();
            }
            Runnable requestHandler = new RequestHandler(socket.accept(), instances[index]);
            executorService.execute(requestHandler);
            count++;
        }
    }
}
