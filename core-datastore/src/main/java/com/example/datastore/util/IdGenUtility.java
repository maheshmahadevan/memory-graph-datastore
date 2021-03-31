package com.example.datastore.util;

import java.util.concurrent.atomic.AtomicInteger;

public enum IdGenUtility {
	
	INSTANCE;
	
	private static AtomicInteger entityId = new AtomicInteger(100);
	
	public int getNextId() {
		return entityId.incrementAndGet();
	}
	

}
