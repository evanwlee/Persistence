package com.evanwlee.utils.token;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;


public class ReturnTokenImpl implements IReturnToken{
	
	private IReturnToken.Status status = IReturnToken.Status.SUCCESS;
	
	Object result = null;
	boolean failed = false;
	
	List<IStatusMessage> errorMessage = new ArrayList<IStatusMessage>();
	List<IStatusMessage> warningMessage = new ArrayList<IStatusMessage>();
	List<IStatusMessage> infoMessage = new ArrayList<IStatusMessage>();

	public List<IStatusMessage> getAllMessages() {
		List<IStatusMessage> all = new ArrayList<IStatusMessage>();
		all.addAll(errorMessage);
		all.addAll(warningMessage);
		all.addAll(infoMessage);
		return all;
	}

	public Status getStatus() {
		return status;
	}
	
	//Only let status get worse, not better
	private void setStatus(IReturnToken.Status newStatus){
		if( newStatus == IReturnToken.Status.ERROR){
			status = newStatus;
			setFailure(true);
			return;
		}
		
		if( newStatus == IReturnToken.Status.WARNING){	
			status = newStatus;
			return;
		}

		status = newStatus;
	}

	public void addMessage(IStatusMessage msg) {
		switch (msg.getStatus()) {
            case WARNING:  	warningMessage.add(msg); setStatus(msg.getStatus()); break;
            case ERROR:  	errorMessage.add(msg); setStatus(msg.getStatus());break;
            //case STACK:  	errorMessage.add(msg); setStatus(msg.getStatus());break;
            case SUCCESS:  infoMessage.add(msg); setStatus(msg.getStatus());break;
        }
	}
	
	public List<IStatusMessage> getMessages(IReturnToken.Status type) {
		switch (type) {
            case WARNING:  	return warningMessage;
            case ERROR:  	return errorMessage;
            //case STACK:  	return errorMessage;
            case SUCCESS:  return infoMessage;
            default: return getAllMessages();
        }
	}
	
	public void logErrorMessages(Logger log) {
		logMessagesOfType(IReturnToken.Status.ERROR,log);
	}
	
	public void logWarningMessages(Logger log) {
		logMessagesOfType(IReturnToken.Status.WARNING,log);
	}
	
	public void logSuccessMessages(Logger log) {
		logMessagesOfType(IReturnToken.Status.SUCCESS,log);
	}
	
	private void logMessagesOfType(IReturnToken.Status type, Logger log){
		List<IStatusMessage> message = new ArrayList<IStatusMessage>();
		switch (type) {
	        case WARNING:  	message.addAll(warningMessage);break;
	        case ERROR:  	message.addAll(errorMessage);break;
	        case SUCCESS:  message.addAll(infoMessage);break;
		}
		
		for (Iterator<?> iterator = message.iterator(); iterator.hasNext();) {
			IStatusMessage msg = (IStatusMessage) iterator.next();
			switch (msg.getStatus()) {
		        case WARNING:  	log.info(msg.getText());break;
		        case ERROR:  	log.error(msg.getText());break;
		        case SUCCESS:  	log.debug(msg.getText());break;
			}
		}
	}
	
	public void addToken(IReturnToken token) {
	}

	public void setResult(Object result) {
		this.result = result;
	}
	public Object getResult() {
		return result;
	}

	public boolean isFailure() {

		return failed;
	}

	public void setFailure(boolean failed) {
		this.failed = failed;
		
	}
	public boolean hasResult() {
		if( result != null){
			return true;
		}
		return false;
	}
	


}
