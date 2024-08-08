package proiect_licenta.planner.execution.worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.WorkerManager;
import proiect_licenta.planner.execution.analysis.InstanceConfiguration;
import proiect_licenta.planner.execution.analysis.MarketAnalyzer;
import proiect_licenta.planner.execution.ec2_instance.EC2InstanceManager;
import proiect_licenta.planner.execution.worker.load_balancing.LoadBalancer;
import proiect_licenta.planner.execution.worker.load_balancing.RandomWorker;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.jobs.ProcessingJob;
import software.amazon.awssdk.regions.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;

import static java.util.stream.Collectors.toList;

public class WorkerPool {
	private static final Logger logger = LogManager.getLogger();
	private final List<WorkerManager> workerManagers = new ArrayList<>();
	private final int maxWorkers = 3;
	private final int maxVCPUs = 4;
	private final int numTop = 5;
	private final MarketAnalyzer marketAnalyzerGP = new MarketAnalyzer(AmiMap.getRegions().subList(0, 1),
			MarketAnalyzer.getGeneralPurposeInstances(maxVCPUs));
	private final MarketAnalyzer marketAnalyzerCO = new MarketAnalyzer(AmiMap.getRegions(),
			MarketAnalyzer.getComputeOptimizedInstances(maxVCPUs));
	private final MarketAnalyzer marketAnalyzerMO = new MarketAnalyzer(AmiMap.getRegions(),
			MarketAnalyzer.getMemoryOptimizedInstances(maxVCPUs));
	private final LoadBalancer loadBalancer = new RandomWorker();
	Queue<WorkerRequest> workerRequests = new LinkedBlockingQueue<>();


	public WorkerPool(List<Region> regions, int minVCPUs) {
		List<InstanceConfiguration> configurations = marketAnalyzerGP.getTopN(numTop);


		for (Region region : regions) {
			List<InstanceConfiguration> regionConfigurations = configurations.stream().filter(ic -> ic.region().equals(region)).toList();
			if (regionConfigurations.isEmpty()) {
				continue;
			}
			workerManagers.add(new EC2InstanceManager(region, "e" + region.id(), regionConfigurations));
		}

		workerManagers.forEach(manager -> manager.createWorkers(1));

		logger.info("Created {} workers", allWorkers().size());
	}

	private void fullfillRemainingRequests() {
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

	private Worker pickWorker(ProcessingJob job) {
		return loadBalancer.pickWorker(allWorkers(), job);
	}

	public CompletableFuture<Worker> requestWorker(ProcessingJob job) {
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

	private record WorkerRequest(CompletableFuture<Worker> future, ProcessingJob job) {
	}


}
