package model;

/**
 * One planned delivery trip: from a relief hub to an affected area.
 * Example: UPM -> SK Sri Serdang. The controller runs Dijkstra for each request.
 */
public class DeliveryRequest {

    private final String hubId;          // starting base, e.g. "UPM"
    private final String destinationId;  // target place, e.g. "SKSS"

    public DeliveryRequest(String hubId, String destinationId) {
        this.hubId = hubId;
        this.destinationId = destinationId;
    }

    /** ID of the relief hub where the truck leaves from. */
    public String getHubId() {
        return hubId;
    }

    /** ID of the affected area that needs supplies. */
    public String getDestinationId() {
        return destinationId;
    }
}
