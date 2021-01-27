package com.evanwlee.utils.threading.work;

import java.util.Scanner;

import com.evanwlee.utils.string.RandomString;
import com.evanwlee.utils.threading.VMThreadManager;

public class TestAsynchCommand implements IAsynchCommand {

	@Override
	public void execute() {
		System.out.println("**Work Done By Me="+new RandomString(5).nextString());

	}
	
	public static void main(String...strings ) throws Exception{
		//VMThreadManager.current().addToQueue(new TestAsynchCommand());
		
		
		try{
			Scanner in = new Scanner(System.in);
			
			//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	        System.out.println("\nEnter Number of work to add:");
	        int v = 0;
	        while((v = Integer.parseInt(in.nextLine())) != -1){
	        	if( v == 99){
	        		VMThreadManager.current().stop();
	        	}else{
			        try{
			            for(int i = 0; i < v;i++){
			    			VMThreadManager.current().addToQueue(new TestAsynchCommand());
			    		}
			            System.out.print("\n\nEnter Number of work to add:");
			        }catch(NumberFormatException nfe){
			            System.err.println("Invalid Format!");
			        }
	        	}
			}
	        try{in.close();}catch(Exception e){}
	        
		}catch(Exception e){
			System.out.println("Problem with input, Exiting now: "+e);
			
		}
		System.exit(0);
	}
}
