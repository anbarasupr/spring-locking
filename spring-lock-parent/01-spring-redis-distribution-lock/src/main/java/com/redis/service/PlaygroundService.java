package com.redis.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.redis.service.locker.DistributedLocker;
import com.redis.service.locker.LockExecutionResult;

import jakarta.annotation.PostConstruct;

@Service
public class PlaygroundService {
	private static final Logger LOG = LoggerFactory.getLogger(PlaygroundService.class);

	private final DistributedLocker locker;

	@Autowired
	public PlaygroundService(DistributedLocker locker) {
		this.locker = locker;
	}

	@PostConstruct
	private void setup() {
//        CompletableFuture.runAsync(() -> runTask("Task1", 3000));
//        CompletableFuture.runAsync(() -> runTask("2", 1000));
//        CompletableFuture.runAsync(() -> runTask("3", 100));

		List<CompletableFuture<LockExecutionResult<String>>> futuresList = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			String taskId = "Task-" + i;
			int executionTime = 9;//ThreadLocalRandom.current().nextInt(4, 5);
			CompletableFuture<LockExecutionResult<String>> c = CompletableFuture.supplyAsync(() -> runTask(taskId, executionTime));
			futuresList.add(c);
		}
		
		CompletableFuture<List<LockExecutionResult<String>>> finalList = allOf(futuresList);
		finalList.whenComplete((result, ex) -> {
			System.out.println("result: "+result);
			System.out.println("ex: "+ex);
		});
		//.exceptionally(ex->	 System.out.println("ex: "+ex));

//		CompletableFuture<Void> allFuturesResult = CompletableFuture
//				.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
//		CompletableFuture<List<Void>> list = allFuturesResult
//				.thenApply(v -> futuresList.stream().map(future -> future.join()).collect(Collectors.toList()));
//		CompletableFuture.runAsync(() -> runTask("Task1", 3000));
//        CompletableFuture.runAsync(() -> runTask("Task2", 1000));
//        CompletableFuture.runAsync(() -> runTask("Task3", 100));
//		CompletableFuture.runAsync(() -> runTask("Task4", 3000));
//        CompletableFuture.runAsync(() -> runTask("Task5", 1000));
//        CompletableFuture.runAsync(() -> runTask("Task6", 100));
//		CompletableFuture.runAsync(() -> runTask("Task7", 3000));
//        CompletableFuture.runAsync(() -> runTask("Task8", 1000));
//        CompletableFuture.runAsync(() -> runTask("Task9", 100));
//		CompletableFuture.runAsync(() -> runTask("Task10", 3000));
//        CompletableFuture.runAsync(() -> runTask("Task11", 1000));
//        CompletableFuture.runAsync(() -> runTask("Task12", 100));
	}

	public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
		CompletableFuture<Void> allFuturesResult = CompletableFuture
				.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
		return allFuturesResult
				.thenApply(v -> futuresList.stream().map(future -> future.join()).collect(Collectors.<T>toList()));
	}

	private LockExecutionResult<String> runTask(final String taskNumber, final long sleep) {
		LOG.info("Running task : '{}'", taskNumber);

		// 5 - Retry Timeout
		// 6 - Lock key Expiry /Timeout
		// sleep - Task execution time
		LockExecutionResult<String> result = locker.lock("some-key", 60, 10, () -> {
			LOG.info("Task {} Processing............. for '{}' sec", taskNumber, sleep);
			Thread.sleep(sleep * 1000);
			LOG.info("Task {} Processing Completed............. for '{}' ms", taskNumber, sleep);
			return taskNumber;
		});
		return result;
		//LOG.info("Task result : '{}' -> exception : '{}'", result.getResultIfLockAcquired(), result.hasException());
	}
}
