package com.example.pessimisticlock.controller;

import com.example.pessimisticlock.domain.Balance;
import com.example.pessimisticlock.service.BalanceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/balances")
public class BalanceController {

	private final BalanceService balanceService;

	public BalanceController(BalanceService balanceService) {
		this.balanceService = balanceService;
	}

	@GetMapping("inc")
	public Balance incrementBalance() {
		return balanceService.incrementBalance();
		// return "balance incremented.";
	}

	@GetMapping("inc/amount/{amount}")
	public Balance incrementBalance(@PathVariable Integer amount) {
		return balanceService.incrementBalance(amount);
		// return "balance incremented.";
	}

	@GetMapping
	public Balance getBalance() {
		return balanceService.getBalance();
	}

	@GetMapping("inc/async/count/{count}")
	public String incrementBalanceAsync(@PathVariable Integer count) {
		balanceService.incrementBalanceAsync(count);
		return "balance increment process initiated.";
	}
}
