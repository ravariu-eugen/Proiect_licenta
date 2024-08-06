package proiect_licenta.planner.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import proiect_licenta.planner.execution.analysis.InstanceConfiguration;
import proiect_licenta.planner.execution.analysis.MarketAnalyzer;
import proiect_licenta.planner.execution.ec2_instance.EC2InstanceManager;
import proiect_licenta.planner.execution.worker.Worker;
import proiect_licenta.planner.execution.worker_request.AllocationStrategy;
import proiect_licenta.planner.execution.worker_request.ExistingSetUp;
import proiect_licenta.planner.helper.AmiMap;
import proiect_licenta.planner.jobs.ProcessingJob;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

public class WorkerPool {
	private static final Logger logger = LogManager.getLogger();
	private final List<WorkerManager> workerManagers = new ArrayList<>();
	private final MarketAnalyzer marketAnalyzerGP = new MarketAnalyzer(AmiMap.getRegions(),
			MarketAnalyzer.getGeneralPurposeInstances(16));
	private final MarketAnalyzer marketAnalyzerCO = new MarketAnalyzer(AmiMap.getRegions(),
			MarketAnalyzer.getComputeOptimizedInstances(16));
	private final MarketAnalyzer marketAnalyzerMO = new MarketAnalyzer(AmiMap.getRegions(),
			MarketAnalyzer.getMemoryOptimizedInstances(16));
	private final AllocationStrategy allocationStrategy = new ExistingSetUp();


	public WorkerPool() {
		List<InstanceConfiguration> configurations = marketAnalyzerGP.getTopN(3);
		for (int i = 0; i < configurations.size(); i++) {
			logger.info("Config {}:", i + 1);
			logger.info(configurations.get(i).toString());
			workerManagers.add(new EC2InstanceManager("e" + (i + 1), configurations.get(i)));
		}
		int num_workers = 1;

		workerManagers.forEach(manager -> manager.createWorkers(num_workers));
		allWorkers().forEach(worker -> logger.info("Created worker {}", worker.toString()));
		logger.info("Created {} workers", allWorkers().size());
	}

	private List<Worker> allWorkers() {
		return workerManagers.stream().flatMap(manager -> manager.getWorkers().stream()).collect(toList());
	}

	public CompletableFuture<Worker> requestWorker(ProcessingJob job) {
		// TODO: implement
		ExecutorService executor = Executors.newSingleThreadExecutor();

		return CompletableFuture.supplyAsync(() -> {
			while (true) {
				Worker result = allocationStrategy.pickWorker(allWorkers(), job);
				if (result != null) {
					return result;
				}

				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}, executor);

	}

	public void close() {
		try {
			workerManagers.parallelStream().forEach(WorkerManager::close);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}






}
