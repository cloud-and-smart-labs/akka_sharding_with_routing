package actor;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


import akka.actor.ActorRef;

public class WorkerRegionState {
	private Set<ActorRef> workerRegionSet = new HashSet<>();
	
	void updateState(WorkerRegionEvent event) {
        if (event instanceof WorkerRegionEvent.Registered) {
            ActorRef registeredRegion = ((WorkerRegionEvent.Registered) event).getWorkerRegion();
            workerRegionSet.add(registeredRegion);
        } else if (event instanceof WorkerRegionEvent.Unregistered) {
            ActorRef unregisteredRegion = ((WorkerRegionEvent.Unregistered) event).getWorkerRegion();
            workerRegionSet.remove(unregisteredRegion);
        } else {
            throw new IllegalArgumentException();
        }
    }
	
	public boolean containsRegisteredRegion(ActorRef workerRegion) {
        return workerRegionSet.contains(workerRegion);
    }

    public Optional<ActorRef> getAvailableWorkerRegion() {
        return workerRegionSet.stream().findAny();
    }

    public int getWorkerRegionsCount() {
        return workerRegionSet.size();
    }

    public Set<ActorRef> getWorkerRegions() {
        return workerRegionSet;
    }
}