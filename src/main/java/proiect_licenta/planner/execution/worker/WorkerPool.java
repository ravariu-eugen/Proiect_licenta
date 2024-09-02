package proiect_licenta.planner.execution.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.WorkerManager;
import proiect_licenta.planner.execution.analysis.InstanceConfiguration;
import proiect_licenta.planner.execution.analysis.MarketAnalyzer;
import proiect_licenta.planner.execution.ec2_instance.EC2InstanceManager;
import proiect_licenta.planner.execution.worker.load_balancing.LoadBalancer;
import proiect_licenta.planner.execution.worker.load_balancing.RandomWorker;
import proiect_licenta.planner.jobs.ComputeJob;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.stream.Collectors.toList;

public class WorkerPool {
	private static final Logger logger = LogManager.getLogger();

	private static final int maxWorkers = 3;
	private static final int maxVCPUs = 4;
	private static final int numTop = 5;

	private final List<WorkerManager> workerManagers = new ArrayList<>();
	private final MarketAnalyzer marketAnalyzer;
	private final LoadBalancer loadBalancer = new RandomWorker();
	Queue<WorkerRequest> workerRequests = new LinkedBlockingQueue<>();


	public WorkerPool(List<Region> regions, int minVCPUs) {
		logger.info("Creating worker pool");
		marketAnalyzer = new MarketAnalyzer(regions, MarketAnalyzer.getGeneralPurposeInstances(16));
		List<InstanceConfiguration> configurations = marketAnalyzer.getTopN(numTop);

		logger.info("Configurations: {}", configurations);


		for (Region region : regions) {
			List<InstanceConfiguration> regionConfigurations = configurations.stream().filter(ic -> ic.region().equals(region)).toList();
			if (regionConfigurations.isEmpty()) {
				continue;
			}
			var instanceManager = new EC2InstanceManager(region, "e" + region.id(), regionConfigurations);
			workerManagers.add(instanceManager);
		}
		int vcpusPerManager = (int) Math.ceil((double) minVCPUs / workerManagers.size());


		workerManagers.forEach(manager -> manager.createWorkers(vcpusPerManager));

		logger.info("Created {} workers", allWorkers().size());
	}

	private void fulfillRemainingRequests() {
		while (!workerRequests.isEmpty()) {
			WorkerRequest request = workerRequests.peek();
			var future = request.future;
			var worker = pickWorker(request.job);

			if (worker == null) {
				return;
			}
			future.complete(worker);
			workerRequests.poll();
		}
	}

	private List<Worker> allWorkers() {
		return workerManagers.stream().flatMap(manager -> manager.getWorkers().stream()).collect(toList());
	}

	private Worker pickWorker(ComputeJob job) {
		return loadBalancer.pickWorker(allWorkers(), job);
	}

	public CompletableFuture<Worker> requestWorker(ComputeJob job) {
		Worker w = pickWorker(job);
		if (w != null) {
			return CompletableFuture.completedFuture(w);
		}

		var future = new CompletableFuture<Worker>();
		workerRequests.add(new WorkerRequest(future, job));
		return future;
	}

	public void close() {
		try {
			workerManagers.parallelStream().forEach(WorkerManager::close);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private record WorkerRequest(CompletableFuture<Worker> future, ComputeJob job) {
	}


}
