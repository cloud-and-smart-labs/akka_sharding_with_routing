package actor;

import java.util.ArrayList;
import java.util.List;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import pi_swarm_approx.JobMessage;

public class WorkAggregator extends AbstractActor{
	private int numOfModelsToCompute;
	private final ActorRef workerRef;
	//private List<WorkerProtocol.WorkProcessed> jobsProcessed;
	
	public static Props props(int modelsToCompute, ActorRef workerRef) {
        return Props.create(WorkAggregator.class, modelsToCompute, workerRef);
    }
	
	public WorkAggregator(int numOfModelsToCompute, ActorRef workerRef) {
        this.numOfModelsToCompute = numOfModelsToCompute;
        this.workerRef = workerRef;
//        this.jobsProcessed = new ArrayList<>();
    }

	@Override
	public Receive createReceive() {
		return receiveBuilder()
            .match(WorkerProtocol.WorkProcessed.class, this::handleModelProcessed)
            .build();
	}
	
	private void handleModelProcessed(WorkerProtocol.WorkProcessed work) {
        System.out.println("Work Aggregator >>> Received request");
                
        numOfModelsToCompute -= 1;
        if (numOfModelsToCompute == 0) {    
            System.out.println("Work Aggregator >>> Finished a job >> Sending results back to Worker ");
            workerRef.tell(work.getResult(), self());
        }
        
    }
}