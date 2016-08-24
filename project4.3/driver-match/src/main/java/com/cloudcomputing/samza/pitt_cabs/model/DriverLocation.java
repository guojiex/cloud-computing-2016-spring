package com.cloudcomputing.samza.pitt_cabs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverLocation {
    public int blockId;
    public int driverId;
    public String type;
    public int latitude;
    public int longitude;

    /**
     * @param blockId
     * @param driverId
     * @param type
     * @param latitude
     * @param longitude
     */
    public DriverLocation(int blockId, int driverId, String type, int latitude, int longitude) {
        super();
        this.blockId = blockId;
        this.driverId = driverId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public DriverLocation(List<String> input) {
        this.blockId = Integer.parseInt(input.get(0));
        this.driverId = Integer.parseInt(input.get(1));
        this.type = input.get(2);
        this.latitude = Integer.parseInt(input.get(3));
        this.longitude = Integer.parseInt(input.get(4));
    }

    public DriverLocation(Map<String, Object> map) {
        this.blockId = (Integer) map.get("blockId");
        this.driverId = (Integer) map.get("driverId");
        this.type = (String) map.get("type");
        this.latitude = (Integer) map.get("latitude");
        this.longitude = (Integer) map.get("longitude");
    }
    public Map<String,Object> toMap(){
        Map<String,Object> map=new HashMap<>();
        map.put("blockId", this.blockId);
        map.put("driverId", this.driverId);
        map.put("type", this.type);
        map.put("latitude", this.latitude);
        map.put("longitude", this.longitude);
        return map;
    }

    public List<String> toList() {
        List<String> res = new ArrayList<>();
        res.add(String.valueOf(this.blockId));
        res.add(String.valueOf(this.driverId));
        res.add(this.type);
        res.add(String.valueOf(this.latitude));
        res.add(String.valueOf(this.longitude));
        return res;
    }
}
