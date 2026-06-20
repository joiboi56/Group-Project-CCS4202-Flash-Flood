package model;

public class DeliveryRequest {

    private final String hubId;
    private final String destinationId;

    public DeliveryRequest(String hubId, String destinationId) {
        this.hubId = hubId;
        this.destinationId = destinationId;
    }

    public String getHubId() {
        return hubId;
    }

    public String getDestinationId() {
        return destinationId;
    }
}
