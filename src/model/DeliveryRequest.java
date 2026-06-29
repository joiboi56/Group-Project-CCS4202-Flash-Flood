package model;


public class DeliveryRequest {

    //store starting base id
    private final String hubId; 
    //store destination id         
    private final String destinationId;  
    //like fill a form, need to fill this info
    public DeliveryRequest(String hubId, String destinationId) {
        this.hubId = hubId;
        this.destinationId = destinationId;
    }

    //can know where the truck leaves from
    public String getHubId() {
        return hubId;
    }

    // can know the truck destination
    public String getDestinationId() {
        return destinationId;
    }
}
