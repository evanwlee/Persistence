package com.evanwlee.utils.email;

import java.io.OutputStream;

import com.evanwlee.utils.string.StringUtils;

public class Recipient {
	private String emailAddr = "";
	private String firstName = "";
	private String lastName = "";

	public String getEmailAddr() {
		return emailAddr;
	}

	public void setEmailAddr(String emailAddr) {
		this.emailAddr = emailAddr;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	
	public String toString(){
		return StringUtils.renderBeanAsString(this);
	}
	
	public void toStream(OutputStream out){
		StringUtils.writeBean(this, out);
	}
	

}
