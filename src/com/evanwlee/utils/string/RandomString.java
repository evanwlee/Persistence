package com.evanwlee.utils.string;

import java.util.Calendar;
import java.util.Random;

public class RandomString {

	  private static final char[] symbols;

	  static {
	    StringBuilder tmp = new StringBuilder();
	    for (char ch = '1'; ch <= '9'; ++ch)
	      tmp.append(ch);
	    //for (char ch = 'a'; ch <= 'z'; ++ch)
	     // tmp.append(ch);
	    symbols = tmp.toString().toCharArray();
	  }   

	  private final Random random = new Random();

	  private final char[] buf;

	  public RandomString(int length) {
	    if (length < 1)
	      throw new IllegalArgumentException("length < 1: " + length);
	    buf = new char[length];
	  }

	  public String nextString() {
	    for (int idx = 0; idx < buf.length; ++idx){
	      buf[idx] = symbols[random.nextInt(symbols.length)];
	    }
	    Calendar calendar = Calendar.getInstance();
	    int year = Calendar.getInstance().get(Calendar.YEAR);
	    int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
	    return new String(buf)+dayOfYear+year;
	  }
	  
	  
	  public static void main(String[] args){
		  System.out.println(new RandomString(5).nextString());
	  }
	}