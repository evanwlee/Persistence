package com.evanwlee.utils.token;

public interface IStatusMessage {
	public IReturnToken.Status getStatus();

	public void setStatus(IReturnToken.Status status);

	public String getText();

	public void setText(String text);

}
