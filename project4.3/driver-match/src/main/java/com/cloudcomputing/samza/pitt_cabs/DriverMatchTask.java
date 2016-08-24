package com.cloudcomputing.samza.pitt_cabs;

import java.util.Map;
import java.util.HashMap;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.KeyValueStore;
import org.apache.samza.system.IncomingMessageEnvelope;
import org.apache.samza.system.OutgoingMessageEnvelope;
import org.apache.samza.task.InitableTask;
import org.apache.samza.task.MessageCollector;
import org.apache.samza.task.StreamTask;
import org.apache.samza.task.TaskContext;
import org.apache.samza.task.TaskCoordinator;
import org.apache.samza.task.WindowableTask;

/**
 * Consumes the stream of driver location updates and rider cab requests.
 * Outputs a stream which joins these 2 streams and gives a stream of rider to
 * driver matches.
 */
public class DriverMatchTask implements StreamTask, InitableTask, WindowableTask {

    private static final int MAX_DIST = 5000;
    /* Define per task state here. (kv stores etc) */
    private KeyValueStore<String, String> blockDriver;
    private KeyValueStore<String, Map<String, Object>> driverInformation;

    @Override
    @SuppressWarnings("unchecked")
    public void init(Config config, TaskContext context) throws Exception {
        blockDriver = (KeyValueStore<String, String>) context.getStore("driver-loc");
        driverInformation = (KeyValueStore<String, Map<String, Object>>) context.getStore("driver-list");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(IncomingMessageEnvelope envelope, MessageCollector collector, TaskCoordinator coordinator) {
        // The main part of your code. Remember that all the messages for a
        // particular partition
        // come here (somewhat like MapReduce). So for task 1 messages for a
        // blockId will arrive
        // at one task only, thereby enabling you to do stateful stream
        // processing.
        String incomingStream = envelope.getSystemStreamPartition().getStream();

        if (incomingStream.equals(DriverMatchConfig.DRIVER_LOC_STREAM.getStream())) {
            processDriverLocationEvent((Map<String, Object>) envelope.getMessage());
        } else if (incomingStream.equals(DriverMatchConfig.EVENT_STREAM.getStream())) {
            processEvent((Map<String, Object>) envelope.getMessage(), collector);
        } else {
            throw new IllegalStateException("Unexpected input stream: " + envelope.getSystemStreamPartition());
        }

    }

    private static String createDriverInforamtion(String latitude, String longitude, String gender, String rating,
            String salary) {
        return String.format("%s:%s:%s:%s:%s", latitude, longitude, gender, rating, salary);
    }

    private void processDriverLocationEvent(Map<String, Object> message) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String driverId = String.valueOf((int) message.get("driverId"));
        String latitude = String.valueOf((int) message.get("latitude"));
        String longitude = String.valueOf((int) message.get("longitude"));
        String combine = String.format("%s#%s", blockId, driverId);
        this.blockDriver.put(combine, String.format("%s:%s", latitude, longitude));

        Map<String, Object> drivers = driverInformation.get(blockId);
        if (drivers == null)
            return;

        if (drivers.containsKey(driverId)) {
            String[] parts = ((String) drivers.get(driverId)).split(":");
            String gender = parts[2];
            String rating = parts[3];
            String salary = parts[4];
            drivers.put(driverId, createDriverInforamtion(latitude, longitude, gender, rating, salary));
        }

        driverInformation.put(blockId, drivers);
    }

    private void processEvent(Map<String, Object> message, MessageCollector collector) {
        if (message.get("type").equals("DRIVER_LOCATION")) {
            throw new IllegalArgumentException("Unexpected DRIVER_LOCATION " + message.get("type"));
        }

        if (message.get("type").equals("LEAVING_BLOCK")) {
            processLeavingBlockingEvent(message);
        } else if (message.get("type").equals("ENTERING_BLOCK")) {
            processEnteringBlockEvent(message);
        } else if (message.get("type").equals("RIDE_REQUEST")) {
            processRequest(message, collector);
        } else {
            processRideCompleteEvent(message);
        }

    }

    private void processLeavingBlockingEvent(Map<String, Object> message) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String driverId = String.valueOf((int) message.get("driverId"));
        String status = (String) message.get("status");

        Map<String, Object> drivers = driverInformation.get(blockId);
        if (drivers == null)
            return;

        if (status.equals("AVAILABLE")) {
            drivers.remove(driverId);
        }

        driverInformation.put(blockId, drivers);
    }

    private void processEnteringBlockEvent(Map<String, Object> message) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String driverId = String.valueOf((int) message.get("driverId"));
        String latitude = String.valueOf((int) message.get("latitude"));
        String longitude = String.valueOf((int) message.get("longitude"));
        String gender = (String) message.get("gender");
        String rating = String.valueOf((double) message.get("rating"));
        String salary = String.valueOf((int) message.get("salary"));
        String status = (String) message.get("status");

        String combine = String.format("%s#%s", blockId, driverId);
        this.blockDriver.put(combine, String.format("%s:%s", latitude, longitude));

        Map<String, Object> drivers = driverInformation.get(blockId);
        if (drivers == null) {
            driverInformation.put(blockId, new HashMap<String, Object>());
            drivers = driverInformation.get(blockId);
        }

        if (status.equals("AVAILABLE")) {
            drivers.put(driverId, createDriverInforamtion(latitude, longitude, gender, rating, salary));
        }

        driverInformation.put(blockId, drivers);
    }

    private double getDistanceBetweenTwoVertice(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    private void processRequest(Map<String, Object> message, MessageCollector collector) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String clientId = String.valueOf((int) message.get("clientId"));

        int clientLatitude = (int) message.get("latitude");
        int clientLongitude = (int) message.get("longitude");

        Map<String, Object> driverList = driverInformation.get(blockId);
        if (driverList == null)
            return;

        double maxScore = 0;
        int driverId = -1;

        for (Map.Entry<String, Object> entry : driverList.entrySet()) {
            String[] driverInformation = ((String) entry.getValue()).split(":");

            int latitude = Integer.parseInt(driverInformation[0]);
            int longitude = Integer.parseInt(driverInformation[1]);
            String parts = this.blockDriver.get(String.format("%s#%s", blockId, entry.getKey()));
            if (parts != null) {
                latitude = Integer.parseInt(parts.split(":")[0]);
                longitude = Integer.parseInt(parts.split(":")[1]);
            }
            String gender = driverInformation[2];
            double rating = Double.parseDouble(driverInformation[3]);
            int salary = Integer.parseInt(driverInformation[4]);
            double distance_score = 1
                    - (getDistanceBetweenTwoVertice(latitude, clientLatitude, longitude, clientLongitude)) / MAX_DIST;
            double rating_score = rating / 5.0;
            double gender_score = 0.0;
            if (((String) (message.get("gender_preference"))).equals("N"))
                gender_score = 1;
            else if (((String) (message.get("gender_preference"))).equals(gender))
                gender_score = 1;
            double salary_score = 1 - salary / 100.0;
            double match_score = distance_score * 0.4 + gender_score * 0.2 + rating_score * 0.2 + salary_score * 0.2;
            if (match_score > maxScore) {
                driverId = Integer.parseInt(entry.getKey());
            }
        }

        driverList.remove(String.valueOf(driverId));

        driverInformation.put(blockId, driverList);

        Map<String, Object> matchMessage = new HashMap<String, Object>();
        matchMessage.put("driverId", driverId);
        matchMessage.put("clientId", (int) message.get("clientId"));

        collector.send(new OutgoingMessageEnvelope(DriverMatchConfig.MATCH_STREAM, matchMessage));

    }

    private void processRideCompleteEvent(Map<String, Object> message) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String driverId = String.valueOf((int) message.get("driverId"));
        String latitude = String.valueOf((int) message.get("latitude"));
        String longitude = String.valueOf((int) message.get("longitude"));
        String gender = (String) message.get("gender");
        String rating = String.valueOf((double) message.get("rating"));
        String salary = String.valueOf((int) message.get("salary"));

        String combine = String.format("%s#%s", blockId, driverId);
        this.blockDriver.put(combine, String.format("%s:%s", latitude, longitude));

        Map<String, Object> drivers = driverInformation.get(blockId);
        if (drivers == null) {
            driverInformation.put(blockId, new HashMap<String, Object>());
            drivers = driverInformation.get(blockId);
        }

        drivers.put(driverId, createDriverInforamtion(latitude, longitude, gender, rating, salary));

        driverInformation.put(blockId, drivers);
    }

    @Override
    public void window(MessageCollector collector, TaskCoordinator coordinator) {
        // this function is called at regular intervals, not required for this
        // project
    }
}
