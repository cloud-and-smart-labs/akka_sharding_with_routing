package actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.typesafe.config.ConfigFactory;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;
import akka.cluster.Member;
import pi_swarm_approx.JobMessage;
import pi_swarm_approx.JobQueue;
import scala.concurrent.duration.FiniteDuration;

public class Master extends AbstractActor{	
	private boolean hasBackends;
    private Cluster cluster = Cluster.get(getContext().system());
    private final WorkerRegionState workerRegionState;
    private static final MasterWorkerProtocol.WorkerRegionAck WORKER_REGION_ACK = new MasterWorkerProtocol.WorkerRegionAck();
    private final JobQueue jobQueue;
    
    public static Props props() {
        return Props.create(Master.class);
    }
    
    public Master() {
    	 this.workerRegionState = new WorkerRegionState();
    	 this.jobQueue = new JobQueue();
    }
   
	@Override
	public Receive createReceive() {
		System.out.println("Master actor received message");
		return receiveBuilder()	
			.match(MasterWorkerProtocol.RegisterWorkerRegion.class, this::handleRegisterWorkerRegion)
			.match(JobMessage.class, this::handleSensorData)
			.matchAny(this::handleAny)
	        .build();
	}
	
	private void handleAny(Object o) {
		System.out.println("Actor received unknown message");
	}
	
	private void handleRegisterWorkerRegion(MasterWorkerProtocol.RegisterWorkerRegion workerRegion) {
		System.out.println("In master class register worker method");
        ActorRef workerRegionActorRef = workerRegion.getRegionActorRef();
        if (workerRegionState.containsRegisteredRegion(workerRegionActorRef)) {
            System.out.println("Worker region already registered");
            sender().tell(WORKER_REGION_ACK, self());
        } else {
        	System.out.println("Started worker region registration");
        	registerWorkerRegion(new WorkerRegionEvent.Registered(workerRegionActorRef));
        }
    }
	
	private void registerWorkerRegion(WorkerRegionEvent.Registered registered) {
		System.out.println("In main registerWorkerRegion method!!");
        workerRegionState.updateState(registered);
        ActorRef workerRegion = registered.getWorkerRegion();
        System.out.println("Region registered! " + workerRegion.path());
        // Watch workerRegion in case it becomes unavailable (terminated message will be received)
        getContext().watch(workerRegion);
        sender().tell(WORKER_REGION_ACK, self());
        if (jobQueue.hasPendingSensorData()) {
            sendDataToWorkers();
        }
    }
	
	 private void handleSensorData(JobMessage msg) {
        if (jobQueue.contains(msg)) {
        	System.out.println("Job already in queue");
        } else {
            System.out.println("Master received job msg!");
            jobQueue.updateState(msg);
            System.out.println("@@@@@@@@@@@@@ " + msg.getPayload());
            sendDataToWorkers();
        }
    }
	 
	 private void sendDataToWorkers() {
        if (jobQueue.hasPendingSensorData()) {
            System.out.println("Size of pending data: " + jobQueue.getPendingSensorData());
            Optional<ActorRef> potentialWorkerRegion = workerRegionState.getAvailableWorkerRegion();
            if (potentialWorkerRegion.isPresent()) {
                ActorRef workerRegion = potentialWorkerRegion.get();
                System.out.println("#########: " + workerRegion.path());
                JobMessage nextMessage = jobQueue.nextSensorData();
                workerRegion.tell(nextMessage, self());
            } else {
                System.out.println("No worker regions to send work to.");
            }
        }
	 }	
}