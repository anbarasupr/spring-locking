package com.example.springredisdistributedlock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LockController {

    @Autowired
    private LockService lockService;

    @GetMapping("/perform/lock-key/{lockKey}/lock-timout/{timeout}/execution-time/{taskTime}")
    public String performOperation(@PathVariable String lockKey, @PathVariable Integer timeout, @PathVariable Integer taskTime) throws InterruptedException {
    	return lockService.performWithLock(lockKey, timeout, taskTime);
     }
}

