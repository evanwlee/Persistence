package com.evanwlee.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.evanwlee.data.domain.evanwlee.ShippingAddress;
import com.evanwlee.utils.logging.LoggerFactory;
import com.evanwlee.utils.token.IReturnToken;
import com.evanwlee.utils.token.ReturnTokenImpl;

public class ExternalServiceManager {
	private Logger log = LoggerFactory.getLogger(ExternalServiceManager.class.getName());
	private volatile static ExternalServiceManager currentManager = null;

	ExternalServiceManager() {
	}
	
	/**
	 * Returns the current domain factory manager for the application.
	 */
	public static ExternalServiceManager current() {
		if (currentManager == null ) {
			synchronized (ExternalServiceManager.class) {
				if (currentManager == null) {
					currentManager = new ExternalServiceManager();
				}
			}
		}
		return currentManager;
	}
	
	private boolean isEmpyt(String val){
		if( val == null || "".equalsIgnoreCase(val.trim())){
			return true;
		}
		return false;
	}
	
	public IReturnToken getWashingtonTaxInfo(ShippingAddress address){
		String urlString = new String("");
		IReturnToken result = new ReturnTokenImpl();
		if( address == null || this.isEmpyt(address.getZipcode()) || this.isEmpyt(address.getAddressLineOne())){
			result.setFailure(true);
			log.error("Missing key attributes to determine tax info");
			return result;
		}
		try {
			String street =  URLEncoder.encode(address.getAddressLineOne(), "UTF-8");
			String city =  URLEncoder.encode(address.getCity(), "UTF-8");
			String post =  URLEncoder.encode(address.getZipcode(), "UTF-8");
			urlString = "http://dor.wa.gov/AddressRates.aspx?output=text&addr="+street+"&city="+city+"&zip="+post;
			log.debug("WA tax data collected from :"  +urlString);
		    URL myURL = new URL(urlString);
		    URLConnection myURLConnection = myURL.openConnection();
		    myURLConnection.connect();
		    
		    BufferedReader in = new BufferedReader(new InputStreamReader(myURLConnection.getInputStream()));
			String resultString = new String("");
			String xmlString;
			while ((xmlString = in.readLine()) != null){
				resultString+= xmlString;
			}
			in.close();
			
			String taxId = new String("");
			if( resultString.indexOf(" ") > -1){
				String[] values = resultString.split(" ");
				if(values.length > 0){
					if(values[0].indexOf("=") > -1){
						values = values[0].split("=");
						if( values.length == 2){
							taxId = values[1];
						}
					}
				}
			}
			
			result.setResult(taxId);
		} catch (MalformedURLException e) { 
		    // new URL() failed
		    // ...
			result.setFailure(true);
			log.error("Bad url passed to service:"+urlString);
		} catch (IOException e) {   
		    // openConnection() failed
		    // ...
			result.setFailure(true);
			log.error("Washington Tax Service seems down");
		}
		return result;
	}
	
	public static void main(String[] args){
		ShippingAddress sa = new ShippingAddress();
		sa.setAddressLineOne("15617 61st LN NE");
		sa.setCity("kenmore");
		sa.setZipcode("98028");
		IReturnToken result = ExternalServiceManager.current().getWashingtonTaxInfo(sa);
		System.out.println(result.getResult());
	}

}
