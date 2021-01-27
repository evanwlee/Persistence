package com.evanwlee.utils.string;


import org.junit.Test;

import static org.junit.Assert.*;


public class RandomStringTest {
	
	@Test
    public void twoStringsNotEqual() {
		RandomString rand = new RandomString(5);
		
		String one = rand.nextString();
		String two = rand.nextString();
		
		assertFalse(one +" !=" +two, one.equals(two));
	}
}
