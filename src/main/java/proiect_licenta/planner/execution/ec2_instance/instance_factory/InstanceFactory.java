package proiect_licenta.planner.execution.ec2_instance.instance_factory;

import java.util.List;

public interface InstanceFactory {

	List<InstanceWrapper> createInstances(int count);

	void clean();
}
