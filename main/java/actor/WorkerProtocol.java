package actor;

import java.io.Serializable;
import pi_swarm_approx.JobMessage;

public interface WorkerProtocol {
	class SensorDataModelTask implements WorkerProtocol, Serializable {

        private final JobMessage msg;

        public SensorDataModelTask(JobMessage msg) {
            this.msg = msg;
        }

        public JobMessage getSensorData() {
            return msg;
        }   
    }
	
	class WorkProcessed implements WorkerProtocol, Serializable {
        private double pi_val;

        WorkProcessed(double pi_val) {
            this.pi_val = pi_val;
        }

        double getResult() {
            return pi_val;
        }
    }

}