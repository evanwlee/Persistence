package com.evanwlee.utils.token;

import org.apache.log4j.Logger;



public class StatusMessageImpl implements IStatusMessage {
	private IReturnToken.Status status;
	private String text;
	
	public StatusMessageImpl(IReturnToken.Status status, String text, Logger log){
		this.status = status;
		this.text = text;
		
		switch(status){
			 case SUCCESS:log.info(text);
			 case WARNING:log.warn(text);
			 case ERROR:log.error(text);
			 default: ;
		}
	}
	
	public StatusMessageImpl(IReturnToken.Status status, String text){
		this.status = status;
		this.text = text;
	}

	public IReturnToken.Status getStatus() {
		return status;
	}

	public void setStatus(IReturnToken.Status status) {
		this.status = status;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	
}
