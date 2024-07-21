package com.redis.service;

import java.util.concurrent.CompletableFuture;

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

		CompletableFuture.runAsync(() -> runTask("Task1", 3000));
//        CompletableFuture.runAsync(() -> runTask("Task2", 1000));
//        CompletableFuture.runAsync(() -> runTask("3", 100));
	}

	private void runTask(final String taskNumber, final long sleep) {
		LOG.info("Running task : '{}'", taskNumber);

		LockExecutionResult<String> result = locker.lock("some-key", 5, 6, () -> {
			LOG.info("Task {} Processing for '{}' ms", taskNumber, sleep);
			Thread.sleep(sleep);
			LOG.info("Task {} Processing Completed... for '{}' ms", taskNumber, sleep);
			return taskNumber;
		});

		LOG.info("Task result : '{}' -> exception : '{}'", result.getResultIfLockAcquired(), result.hasException());
	}
}
