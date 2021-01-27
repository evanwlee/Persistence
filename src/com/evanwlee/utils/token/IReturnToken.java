package com.evanwlee.utils.token;

import java.util.List;

import org.apache.log4j.Logger;


public interface IReturnToken {

	
	public enum Status{
		ERROR(),
		WARNING(),
		SUCCESS();
	}
	
	
	public Status getStatus();
	public List<IStatusMessage> getAllMessages();
	public void addMessage(IStatusMessage msg);
	public Object getResult();
	public void addToken(IReturnToken token);
	public boolean isFailure();
	public void setFailure(boolean failed);
	

	public void logErrorMessages(Logger log);
	public void logWarningMessages(Logger log);
	public void logSuccessMessages(Logger log);
	public void setResult(Object obj);
	public boolean hasResult();

}
