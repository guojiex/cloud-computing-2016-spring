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
    /**
     * Set all these vars as class var, to retain the type persistence in all
     * storage
     */
    private int driverId;
    private int latitude;
    private int longitude;
    private int blockId;
    private int salary;
    private double rating;
    private String gender;

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
            processDriverLoc((Map<String, Object>) envelope.getMessage());
        } else if (incomingStream.equals(DriverMatchConfig.EVENT_STREAM.getStream())) {
            processEvent((Map<String, Object>) envelope.getMessage(), collector);
        } else {
            throw new IllegalStateException("Unexpected input stream: " + envelope.getSystemStreamPartition());
        }
    }

    void processDriverLoc(Map<String, Object> message) {
        if (!message.get("type").equals("DRIVER_LOCATION")) {
            throw new IllegalStateException("Unexpected event type on follows stream: " + message.get("event"));
        }

        setLocation(message);
    }

    void setLocation(Map<String, Object> message) {
        driverId = (int) message.get("driverId");
        latitude = (int) message.get("latitude");
        longitude = (int) message.get("longitude");
        blockId = (int) message.get("blockId");
        driverLocation.put(this.combineBlockIdDriverId(blockId, driverId), String.format("%d#%d", latitude, longitude));
    }

    void setDriver(Map<String, Object> message) {

        driverId = (int) message.get("driverId");
        latitude = (int) message.get("latitude");
        longitude = (int) message.get("longitude");
        blockId = (int) message.get("blockId");
        gender = (String) message.get("gender");
        salary = (int) message.get("salary");
        rating = (double) message.get("rating");
        // value: lat, long, gender, salary, rating
        driverInformation.put(combineBlockIdDriverId(blockId, driverId),
                latitude + "#" + longitude + "#" + gender + "#" + salary + "#" + rating);
    }

    private String combineBlockIdDriverId(int blockId, int driverId) {
        return String.format("%d#%d", blockId, driverId);
    }

    void processEvent(Map<String, Object> message, MessageCollector collector) {
        blockId = (int) message.get("blockId");
        driverId = (int) message.get("driverId");

        // Leaving Block Event
        switch ((String) message.get("type")) {
        case "LEAVING_BLOCK":
            driverLocation.delete(combineBlockIdDriverId(blockId, driverId));
            driverInformation.delete(combineBlockIdDriverId(blockId, driverId));
            break;
        case "ENTERING_BLOCK":
            if (message.get("status").equals("AVAILABLE")) {
                setLocation(message);
                setDriver(message);
            }
            break;
        case "RIDE_COMPLETE":
            setDriver(message);
            break;
        case "RIDE_REQUEST":
            int clientId = (int) message.get("clientId");
            int clientLatitude = (int) message.get("latitude");
            int clientLongitude = (int) message.get("longitude");
            int clientBlockId = (int) message.get("blockId");
            String gender_preference = (String) message.get("gender_preference");

            // from FanOutTask.java in Example
            KeyValueIterator<String, String> drivers = driverInformation.range(clientBlockId + "#", clientBlockId + ";");
            String driver = "";
            String key = "";
            double best = 0.0;
            try {
                while (drivers.hasNext()) {
                    Entry<String, String> cur = drivers.next();

                    String curkey = cur.getKey();
                    String curvalue = cur.getValue();
                    String[] arr = curvalue.split("#");
                    if ((arr.length == 5)) {
                        int lat = Integer.parseInt(arr[0]);
                        int lon = Integer.parseInt(arr[1]);

                        if (driverLocation.get(curkey) != null) {
                            String[] t = driverLocation.get(curkey).split("#");
                            lat = Integer.parseInt(t[0]);
                            lon = Integer.parseInt(t[1]);
                        }

                        String gen = arr[2];
                        int sal = Integer.parseInt(arr[3]);
                        double r = Double.parseDouble(arr[4]);

                        double distance_score = Math.pow((lat - clientLatitude), 2) + Math.pow((lon - clientLongitude), 2);
                        distance_score = Math.sqrt(distance_score);
                        distance_score = 1 - distance_score / 500.0;
                        double rs = r / 5.0;
                        double ss = 1 - sal / 100.0;
                        double gs = 0.0;

                        if (gender_preference.equals("N")) {
                            gs = 1.0;
                        } else {
                            if (gender_preference.equals(gen)) {
                                gs = 1.0;
                            } else {
                                gs = 0.0;
                            }
                        }
                        double ms = distance_score * 0.4 + gs * 0.2 + rs * 0.2 + ss * 0.2;

                        if (ms > best) {
                            best = ms;
                            driver = curkey.split("#")[1];
                            key = curkey;
                        }
                    }
                }
            } catch (NoSuchElementException e) {
                System.out.println("No Such Element!");
            }
            drivers.close();

            // finish scan and send out driverid and riderid
            HashMap<String, Object> match = new HashMap<>();
            if ((!driver.equals("")) && (!key.equals(""))) {
                match.put("driverId", driver);
                match.put("clientId", clientId);
                driverLocation.delete(key);
                driverInformation.delete(key);
                collector.send(new OutgoingMessageEnvelope(DriverMatchConfig.MATCH_STREAM, null, null, match));
            }
            break;
        }
    }

    public void window(MessageCollector collector, TaskCoordinator coordinator) {
        // this function is called at regular intervals, not required for this
        // project
    }
}
