package actor;

import java.io.Serializable;

import akka.actor.ActorRef;

public interface WorkerRegionEvent {
    class Registered implements WorkerRegionEvent, Serializable {
        private ActorRef workerRegion;

        public Registered(ActorRef workerRegion){
            this.workerRegion = workerRegion;
        }

        public ActorRef getWorkerRegion() {
            return workerRegion;
        }
    }
    class Unregistered implements WorkerRegionEvent, Serializable {
        private ActorRef workerRegion;

        public Unregistered(ActorRef workerRegion){
            this.workerRegion = workerRegion;
        }

        public ActorRef getWorkerRegion() {
            return workerRegion;
        }
    }
}