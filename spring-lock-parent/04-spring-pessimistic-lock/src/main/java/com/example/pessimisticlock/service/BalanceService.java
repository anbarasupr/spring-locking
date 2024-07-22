package com.example.pessimisticlock.service;

import com.example.pessimisticlock.domain.Balance;
import com.example.pessimisticlock.domain.LockResult;
import com.example.pessimisticlock.repository.BalanceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class BalanceService {
	private static final Logger LOG = LoggerFactory.getLogger(BalanceService.class);

	private final BalanceRepository balanceRepository;

	public BalanceService(BalanceRepository balanceRepository) {
		this.balanceRepository = balanceRepository;
	}

	@Transactional
	public Balance incrementBalance() {
		Balance balance = balanceRepository.findBalanceByOwner("user-1")
				.orElseThrow(() -> new EntityNotFoundException("Balance not found"));

		log.info("balance incrementing..");

		balance.setBalance(balance.getBalance() + 1);
		return balanceRepository.save(balance);
	}

	@Transactional
	public Balance getBalance() {
		Balance balance = balanceRepository.findBalanceByOwner("user-1")
				.orElseThrow(() -> new EntityNotFoundException("Balance not found"));
		return balance;
	}

	@Transactional
	public Balance incrementBalance(int amount) {
		Balance balance = balanceRepository.findBalanceByOwner("user-1")
				.orElseThrow(() -> new EntityNotFoundException("Balance not found"));

		log.info("balance incrementing {} ...", amount);

		balance.setBalance(balance.getBalance() + amount);
		return balanceRepository.save(balance);
	}

	private <T> LockResult<T> incrementBalance(int amount, final Callable<T> task) {
		try {
			Balance balance = balanceRepository.findBalanceByOwner("user-1")
					.orElseThrow(() -> new EntityNotFoundException("Balance not found"));
			T taskResult = task.call(); // sleep
			log.info("balance incrementing {} ...", amount);
			balance.setBalance(balance.getBalance() + amount);
			balanceRepository.save(balance);
			return LockResult.build(taskResult);
		} catch (Exception e) {
			return LockResult.buildWithException(e);
		}
	}

	public void incrementBalanceAsync(Integer count) {
		List<CompletableFuture<LockResult<String>>> futuresList = new ArrayList<>();
		for (int i = 1; i <= count; i++) {
			int amount = i * 10;
			String taskId = "Task-" + i + "-" + amount;
			int executionTime = 5;// ThreadLocalRandom.current().nextInt(4, 5);
			CompletableFuture<LockResult<String>> c = CompletableFuture
					.supplyAsync(() -> runTask(taskId, executionTime, amount));
			futuresList.add(c);
		}
		CompletableFuture<List<LockResult<String>>> finalList = allOf(futuresList);
		finalList.whenComplete((result, ex) -> {
			System.out.println("result: " + result);
			System.out.println("ex: " + ex);
		});
	}

	private LockResult<String> runTask(final String taskNumber, final long sleep, int amount) {
		LOG.info("Running task : '{}'", taskNumber);
		LockResult<String> result = this.incrementBalance(amount, () -> {
			LOG.info("Task {} Processing............. for '{}' sec", taskNumber, sleep);
			Thread.sleep(sleep * 1000);
			LOG.info("Task {} Processing Completed............. for '{}' sec", taskNumber, sleep);
			return taskNumber;
		});
		return result;
	}

	public <T> CompletableFuture<List<T>> allOf(List<CompletableFuture<T>> futuresList) {
		CompletableFuture<Void> allFuturesResult = CompletableFuture
				.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()]));
		return allFuturesResult
				.thenApply(v -> futuresList.stream().map(future -> future.join()).collect(Collectors.<T>toList()));
	}
}
