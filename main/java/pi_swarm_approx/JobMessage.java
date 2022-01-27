package pi_swarm_approx;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;


public class JobMessage implements Serializable {
    private String payload;
    
    
    public JobMessage(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
   
}