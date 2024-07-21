package com.redis.service.locker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class DistributedLocker {
    private static final Logger LOG = LoggerFactory.getLogger(DistributedLocker.class);
    private static final long DEFAULT_RETRY_TIME = 300L;
    private final ValueOperations<String, String> valueOps;

    public DistributedLocker(final RedisTemplate<String, String> redisTemplate) {
        this.valueOps = redisTemplate.opsForValue();
    }

    public <T> LockExecutionResult<T> lock(final String key,
                                           final int howLongShouldLockBeAcquiredSeconds,
                                           final int lockTimeoutSeconds,
                                           final Callable<T> task) {
        try {
            return tryToGetLock(() -> {
                final Boolean lockAcquired = valueOps.setIfAbsent(key, key, lockTimeoutSeconds, TimeUnit.SECONDS);
                if (lockAcquired == Boolean.FALSE) {
                    //LOG.error("Failed to acquire lock for key '{}', Thread: {}", key, Thread.currentThread().getName());
                    return null;
                }

                LOG.info("Successfully acquired lock for key '{}, Thread: {}'", key, Thread.currentThread().getName());

                try {
                    T taskResult = task.call();
                    return LockExecutionResult.buildLockAcquiredResult(taskResult);
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                    // LOG.info("Failure to acquire Lock for key '{}, Thread: {}'", key, Thread.currentThread().getName());
                    return LockExecutionResult.buildLockAcquiredWithException(e);
                } finally {
                    releaseLock(key);
                    LOG.info("Successfully Released Lock for key '{}, Thread: {}'", key, Thread.currentThread().getName());
                }
            }, key, howLongShouldLockBeAcquiredSeconds);
        } catch (final Exception e) {
            LOG.error(e.getMessage(), e);
            return LockExecutionResult.buildLockAcquiredWithException(e);
        }
    }

    private void releaseLock(final String key) {
       valueOps.getOperations().delete(key);
    }

    private static <T> T tryToGetLock(final Supplier<T> task,
                                      final String lockKey,
                                      final int howLongShouldLockBeAcquiredSeconds) throws Exception {
        final long tryToGetLockTimeout = TimeUnit.SECONDS.toMillis(howLongShouldLockBeAcquiredSeconds);

        final long startTimestamp = System.currentTimeMillis();
        while (true) {
            //LOG.info("Trying to get the lock with key '{}, Thread: {}'", lockKey, Thread.currentThread().getName());

            final T response = task.get();
            // LOG.info("Getting the Result for key '{}, Thread: {}'", lockKey, Thread.currentThread().getName());

            if (response != null) {
                return response;
            }
            sleep(DEFAULT_RETRY_TIME);
            
            if (System.currentTimeMillis() - startTimestamp > tryToGetLockTimeout) {
                LOG.info("Retrying Timout exceeded to get the lock with key '{}, Thread: {}'", lockKey, Thread.currentThread().getName());
                throw new Exception("Failed to acquire lock in " + tryToGetLockTimeout + " milliseconds");
            }
           // LOG.info("Retrying again to get the lock with key '{}, Thread: {}'", lockKey, Thread.currentThread().getName());

        }
    }

    private static void sleep(final long sleepTimeMillis) {
        try {
            Thread.sleep(sleepTimeMillis);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error(e.getMessage(), e);
        }
    }
}
