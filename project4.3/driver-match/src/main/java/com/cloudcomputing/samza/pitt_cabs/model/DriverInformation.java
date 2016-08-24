package com.cloudcomputing.samza.pitt_cabs.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DriverInformation {
    public int blockId;
    public int driverId;
    public String status;
    public String gender;
    public double rating;
    public int salary;

    /**
     * @param blockId
     * @param driverId
     * @param gender
     * @param rating
     * @param salary
     */
    public DriverInformation(int blockId, int driverId, String status, String gender, double rating, int salary) {
        super();
        this.blockId = blockId;
        this.driverId = driverId;
        this.status = status;
        this.gender = gender;
        this.rating = rating;
        this.salary = salary;
    }
    public DriverInformation(List<String> input){
        this.blockId=Integer.parseInt(input.get(0));
        this.driverId=Integer.parseInt(input.get(1));
        this.status=input.get(2);
        this.gender=input.get(3);
        this.rating=Double.parseDouble(input.get(4));
        this.salary=Integer.parseInt(input.get(5));
    }
    public DriverInformation(Map<String, Object> value) {
        this.blockId=(Integer) value.get("blockId");
        this.driverId=(Integer) value.get("driverId");
        this.status=(String) value.get("status");
        this.gender=(String) value.get("gender");
        this.rating=(Double) value.get("rating");
        this.salary=(Integer) value.get("salary");
    }
    public Map<String,Object> toMap(){
        Map<String,Object> map=new HashMap<>();
        map.put("blockId", this.blockId);
        map.put("driverId", this.driverId);
        map.put("status", this.status);
        map.put("gender", this.gender);
        map.put("rating", this.rating);
        map.put("salary", this.salary);
        return map;
    }
    public List<String> toList(){
        List<String> res=new ArrayList<>();
        res.add(String.valueOf(this.blockId));
        res.add(String.valueOf(this.driverId));
        res.add(this.status);
        res.add(this.gender);
        res.add(String.valueOf(this.rating));
        res.add(String.valueOf(this.salary));
        return res;
    }
}
