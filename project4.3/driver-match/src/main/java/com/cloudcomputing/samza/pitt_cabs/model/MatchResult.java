package com.cloudcomputing.samza.pitt_cabs.model;

public class MatchResult {
    public int clientId;
    public int driverId;
    /**
     * @param clientId
     * @param driverId
     */
    public MatchResult(int clientId, int driverId) {
        super();
        this.clientId = clientId;
        this.driverId = driverId;
    }
    
}
