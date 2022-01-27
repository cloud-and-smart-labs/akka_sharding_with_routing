package actor;

import java.time.Duration;
import akka.actor.typed.receptionist.Receptionist;
import java.util.concurrent.CompletionStage;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.pattern.Patterns;
import akka.routing.FromConfig;
import pi_swarm_approx.JobMessage;
import actor.WorkProcessor;

public class WorkerRegion extends AbstractActor{
	private String regionId;
	private final ActorRef workerRegion;
	private final ActorSelection master;
	
	public static Props props(String regionId) {
        return Props.create(WorkerRegion.class, regionId);
    }
	
	 public WorkerRegion(String regionId) {
        this.regionId = regionId;
        master = getContext().actorSelection("akka://MasterSystem@127.0.0.1:2550/user/master");
        ActorSystem system = getContext().getSystem();
        ClusterShardingSettings settings = ClusterShardingSettings.create(system);
        
        ActorRef workProcessorRouter = getContext().actorOf(
                FromConfig.getInstance()
                        .withSupervisorStrategy(supervisorStrategy())
                        .props(Props.create(WorkProcessor.class)),
                "workProcessorRouter");

        workerRegion = ClusterSharding.get(system)
                .start(
                        "workers",
                        PiWorker.props(workProcessorRouter),
                        settings,
                        new WorkRegionMessageExtractor(5));
        registerRegionToMaster();
    }
	 
	private void registerRegionToMaster() {
		Duration timeout = Duration.ofSeconds(5);
		System.out.println("Asking master to register" + master.path());
		
		System.out.println("########!!!!!: " + workerRegion.path());
		CompletionStage<Object> future = Patterns.ask(master, new MasterWorkerProtocol.RegisterWorkerRegion(this.regionId, self()), timeout);
		future.thenAccept(result -> {
            if (result instanceof MasterWorkerProtocol.WorkerRegionAck) {
                System.out.println("Received register ack form Master : " + result.getClass().getName());	
            } else {
            	System.out.println("Unknown message received from Master : " + result.getClass().getName());
            }
        }).exceptionally(e ->
                {
                	System.out.println("No registration Ack from Master. Exception : " + e.getCause().getMessage());
                	System.out.println("Retrying ...");
                    registerRegionToMaster();
                    return null;
                }
        );
	}

	@Override
	public Receive createReceive() {
		System.out.println("WorkerRegion received message");
		return receiveBuilder()
		    .match(JobMessage.class, this::handleWork)
		    .build();	
	}
	
	private void handleWork(JobMessage msg) {
        System.out.println("WorkerRegion >>> Worker Region {}" + regionId);
        workerRegion.tell(new JobMessage("Get pi"), self());
    }
}