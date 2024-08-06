package proiect_licenta.planner.execution.instance_factory;

import java.util.List;

public interface InstanceFactory {

	List<InstanceWrapper> createInstances(int count);

	void clean();
}
