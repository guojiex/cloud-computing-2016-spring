package com.cloudcomputing.samza.pitt_cabs.model;

import java.util.Map;

public class ClientInformation {
    public int blockId;
    public int clientId;
    public int latitude;
    public int longitude;
    public String gender_preference;

    /**
     * @param blockId
     * @param clientId
     * @param latitude
     * @param longitude
     * @param gender_preference
     */
    public ClientInformation(int blockId, int clientId, int latitude, int longitude, String gender_preference) {
        super();
        this.blockId = blockId;
        this.clientId = clientId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.gender_preference = gender_preference;
    }

    public ClientInformation(Map<String, Object> message) {
        this.blockId=(int) message.get("blockId");
        this.clientId=(int) message.get("clientId");
        this.latitude=(int) message.get("latitude");
        this.longitude=(int) message.get("longitude");
        this.gender_preference=(String) message.get("gender_preference");
    }
}
