package com.opcoach.e4.modelmerger.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestMergeDuration extends TestMerge {

	
	@Test
	public void testMergeDuration() {
		
		long start = System.currentTimeMillis();
		merger.mergeModels(master, model);
		long end = System.currentTimeMillis();
		
		System.out.println("Merge duration is : " + (end-start) + " ms");
		assertTrue("Merge must be done in less than 10 ms ", ((end-start) <= 10));
	}


}
