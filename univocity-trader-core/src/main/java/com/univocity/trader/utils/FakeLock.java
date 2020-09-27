package com.univocity.trader.utils;

import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class FakeLock implements Lock {
	@Override
	public void lock() {

	}

	@Override
	public void lockInterruptibly() {

	}

	@Override
	public boolean tryLock() {
		return true;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) {
		return true;
	}

	@Override
	public void unlock() {

	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}
}
