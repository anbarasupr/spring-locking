package com.example.pessimisticlock.domain;


public class LockResult<T> {
    private final boolean lockAcquired;
    public final T resultIfLockAcquired;
    public final Exception exception;

    private LockResult(boolean lockAcquired, T resultIfLockAcquired, final Exception exception) {
        this.lockAcquired = lockAcquired;
        this.resultIfLockAcquired = resultIfLockAcquired;
        this.exception = exception;
    }

    public static <T> LockResult<T> build(final T result) {
        return new LockResult<>(true, result, null);
    }

    public static <T> LockResult<T> buildWithException(final Exception e) {
        return new LockResult<>(true, null, e);
    }

    public static <T> LockResult<T> lockNotAcquired() {
        return new LockResult<>(false, null, null);
    }

    public boolean isLockAcquired() {
        return lockAcquired;
    }

    public T getResultIfLockAcquired() {
        return resultIfLockAcquired;
    }

    public boolean hasException() {
        return exception != null;
    }

	@Override
	public String toString() {
		return "LockExecutionResult [lockAcquired=" + lockAcquired + ", resultIfLockAcquired=" + resultIfLockAcquired
				+ ", exception=" + exception + "]";
	}
    
    
}
