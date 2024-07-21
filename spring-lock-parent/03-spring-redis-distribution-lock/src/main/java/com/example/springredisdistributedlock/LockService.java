package com.example.springredisdistributedlock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.qos.logback.core.util.Duration;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class LockService {

	private final RedisDistributedLock lock;

	@Autowired
	public LockService(RedisDistributedLock lock) {
		this.lock = lock;
	}

	public String performWithLock(String lockKey, int timeout, int taskTime) throws InterruptedException {

		System.out.println("lockKey: " + lockKey + ", timeout: " + timeout + ", taskTime: " + taskTime);
		if (lock.acquireLock(lockKey, timeout, TimeUnit.SECONDS)) {
			log.info("Lock acquired. Operation started.");

			sleepSeconds(taskTime);

			log.info("Operation completed.");
			log.info("Releasing Lock.");
 			lock.releaseLock(lockKey);
			return "Operation completed";
		} else {
			log.error("Failed to acquire lock. Resource is busy.");
			return "Failed to acquire lock. Resource is busy";
		}
	}

	public static void sleepSeconds(int seconds) {
		System.out.println("Sleeping for " + seconds + " Seconds, Thread: " + Thread.currentThread().getName());
		sleepMillis(seconds * 1000);
	}

	public static void sleepMillis(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
