package com.cloudcomputing.samza.pitt_cabs;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.HashMap;

import org.apache.samza.config.Config;
import org.apache.samza.storage.kv.Entry;
import org.apache.samza.storage.kv.KeyValueIterator;
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

    private KeyValueStore<String, String> driverLocation;
    private KeyValueStore<String, String> driverInformation;

    @Override
    @SuppressWarnings("unchecked")
    public void init(Config config, TaskContext context) throws Exception {
        // Initialize stuff (maybe the kv stores?)
        driverLocation = (KeyValueStore<String, String>) context.getStore("driver-loc");
        driverInformation = (KeyValueStore<String, String>) context.getStore("driver-list");
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

    void processDriverLocationEvent(Map<String, Object> message) {
        if (!message.get("type").equals("DRIVER_LOCATION")) {
            throw new IllegalStateException("Unexpected event type on follows stream: " + message.get("event"));
        }
        updateDriverLocation(message);
    }

    void updateDriverLocation(Map<String, Object> message) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String driverId = String.valueOf((int) message.get("driverId"));
        String latitude = String.valueOf((int) message.get("latitude"));
        String longitude = String.valueOf((int) message.get("longitude"));
        driverLocation.put(blockId + "#" + driverId, latitude + "#" + longitude);
    }

    void updateDriverInformation(Map<String, Object> message) {
        String blockId = String.valueOf((int) message.get("blockId"));
        String driverId = String.valueOf((int) message.get("driverId"));
        // String latitude = String.valueOf((int) message.get("latitude"));
        // String longitude = String.valueOf((int) message.get("longitude"));
        String gender = (String) message.get("gender");
        String rating = String.valueOf((double) message.get("rating"));
        String salary = String.valueOf((int) message.get("salary"));

        this.updateDriverLocation(message);
        driverInformation.put(blockId + "#" + driverId, gender + "#" + rating + "#" + salary);
    }

    private double getDistanceBetweenTwoVertices(int x1, int y1, int x2, int y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    void processEvent(Map<String, Object> message, MessageCollector collector) {
        switch ((String) message.get("type")) {
        case "LEAVING_BLOCK":
            String blockId = String.valueOf((int) message.get("blockId"));
            String driverId = String.valueOf((int) message.get("driverId"));
            driverLocation.delete(blockId + ":" + driverId);
            driverInformation.delete(blockId + ":" + driverId);
            break;
        case "ENTERING_BLOCK":
            if (((String) message.get("status")).equals("AVAILABLE")) {
                updateDriverInformation(message);
            }
            break;
        case "RIDE_COMPLETE":
            updateDriverInformation(message);
            break;
        case "RIDE_REQUEST":
            processRideRequest(message, collector);
            break;
        }
    }

    private void processRideRequest(Map<String, Object> message, MessageCollector collector) {
        int clientId = (int) message.get("clientId");
        int clientLatitude = (int) message.get("latitude");
        int clientLongitude = (int) message.get("longitude");
        int clientBlockId = (int) message.get("blockId");
        String gender_preference = (String) message.get("gender_preference");

        KeyValueIterator<String, String> drivers = driverInformation.range(clientBlockId + "#", clientBlockId + ";");
        String driverIdStr = "";
        String combineKey = "";
        double best = 0.0;

        while (drivers.hasNext()) {
            Entry<String, String> driver = drivers.next();
            String[] information = driver.getValue().split("#");

            String[] location = driverLocation.get(driver.getKey()).split("#");
            int driverLatitude = Integer.parseInt(location[0]);
            int driverLongitude = Integer.parseInt(location[1]);

            String gender = information[0];
            double rating = Double.parseDouble(information[1]);
            int salary = Integer.parseInt(information[2]);

            double distance_score = 1
                    - getDistanceBetweenTwoVertices(driverLatitude, clientLatitude, driverLongitude, clientLongitude)
                            / 500.0;
            double rating_score = rating / 5.0;
            double salary_score = 1 - salary / 100.0;
            double gender_score = 0.0;

            if (gender_preference.equals("N") || gender_preference.equals(gender)) {
                gender_score = 1.0;
            }
            double match_score = distance_score * 0.4 + gender_score * 0.2 + rating_score * 0.2 + salary_score * 0.2;

            if (match_score > best) {
                best = match_score;
                driverIdStr = driver.getKey().split("#")[1];
                combineKey = driver.getKey();
            }

        }
        drivers.close();
        Map<String, Object> matchMessage = new HashMap<String, Object>();
        if ((!driverIdStr.isEmpty()) && (!combineKey.isEmpty())) {
            matchMessage.put("driverId", Integer.parseInt(driverIdStr));
            matchMessage.put("clientId", clientId);
            driverLocation.delete(combineKey);
            driverInformation.delete(combineKey);
            collector.send(new OutgoingMessageEnvelope(DriverMatchConfig.MATCH_STREAM, null, null, matchMessage));
        }
    }

    public void window(MessageCollector collector, TaskCoordinator coordinator) {
        // this function is called at regular intervals, not required for this
        // project
    }
}
