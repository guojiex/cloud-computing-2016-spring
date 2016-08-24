import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadBalancer {
    private static final int THREAD_POOL_SIZE = 4;
    private final ServerSocket socket;
    private final DataCenterInstance[] instances;
    private boolean[] instanceAlive;

    public LoadBalancer(ServerSocket socket, DataCenterInstance[] instances) {
        this.socket = socket;
        this.instances = instances;
        instanceAlive = new boolean[instances.length];
    }

    public void start() throws IOException {
        // this.roundRobin();
        //this.betterLoadBalance();
        this.withHealthCheck();
    }

    private void checkHealth() throws IOException {
        for (int i = 0; i < instances.length; i++) {
            if (instances[i].isHealth()) {
                instanceAlive[i] = true;
            } else {
                instanceAlive[i] = false;
                System.out.println(String.format("%d dc is down.", i));
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

    public void withHealthCheck() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        double minCpuUsage = 0;
        int minCpuIndex = 0;
        int count = 0;
        this.checkHealth();
        while (true) {
            if (count % 5 == 0) {// every 5 time we select the one with the
                                 // least
                // cpu usage to start our Round Robin
                minCpuUsage = -1;
                minCpuIndex = -1;
                for (int i = 0; i < instances.length; i++) {
                    if (!instanceAlive[i]) {
                        continue;
                    }
                    if (minCpuIndex == -1) {
                        minCpuUsage = instances[i].getCpuUsage();
                        minCpuIndex = i;
                        continue;
                    }
                    double temp = instances[i].getCpuUsage();
                    if (temp >= 0 && temp < minCpuUsage) {
                        minCpuUsage = temp;
                        minCpuIndex = i;
                    }
                }
            }
            if (count == 10) {
                this.checkHealth();
            }
            int start=minCpuIndex;
            while (!instanceAlive[minCpuIndex]) {
                minCpuIndex++;
                if (minCpuIndex == instances.length) {
                    minCpuIndex = 0;
                }
                if(start==minCpuIndex){
                    break;
                }
                this.checkHealth();
            }
            Runnable requestHandler = new RequestHandler(socket.accept(), instances[minCpuIndex]);
            minCpuIndex = (minCpuIndex == instances.length - 1 ? 0 : minCpuIndex + 1);
            executorService.execute(requestHandler);
            count++;
            if (count == 20) {
                count = 0;
            }
        }
    }
}
