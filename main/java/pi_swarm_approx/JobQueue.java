package pi_swarm_approx;

import java.util.LinkedList;
import java.util.Queue;

public class JobQueue {
	private Queue<JobMessage> pendingSensorData = new LinkedList<>();
	
	public void updateState(JobMessage msg) {
		pendingSensorData.add(msg);
	}
	
	public void removeFromQueue(JobMessage msg) {
		pendingSensorData.remove(msg);
	}
	
	public int getPendingSensorData() {
	   return pendingSensorData.size();
	}
	  
    public Boolean hasPendingSensorData() {
        return !pendingSensorData.isEmpty();
    }
    
    public JobMessage nextSensorData() {
        return pendingSensorData.poll();
    }
    
    public Boolean contains(JobMessage msg) {
        return pendingSensorData.contains(msg);
    }
}