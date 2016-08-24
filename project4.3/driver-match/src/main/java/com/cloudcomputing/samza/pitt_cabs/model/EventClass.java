package com.cloudcomputing.samza.pitt_cabs.model;

import java.util.Map;

public class EventClass {
    public EventClass(Map<String, Object> value) {
        this.blockId=(Integer) value.get("blockId");
        this.driverId=(Integer) (value.get("driverId")==null?0:value.get("driverId"));
        this.status=(String) (value.get("status")==null?"":value.get("status"));
        this.gender=(String) (value.get("gender")==null?"":value.get("gender"));
        this.rating=(Double) (value.get("rating")==null?0.0:value.get("rating"));
        this.salary=(Integer) (value.get("salary")==null?0:value.get("salary"));
        this.clientId=(Integer) (value.get("clientId")==null?0:value.get("clientId"));
        this.gender_preference=(String) (value.get("gender_preference")==null?"":value.get("gender_preference"));
        this.type=(String)(value.get("type")==null?"":value.get("type"));
    }
    public int blockId;
    public int clientId;
    public int driverId;
    public int latitude;
    public int longitude;
    public String gender;
    public String gender_preference;
    public double rating;
    public int salary;
    public String status;
    public String type;
}
