package com.ufasta.mobile.core.request;

public class RequestException extends Exception {

	private static final long serialVersionUID = -3640634824147046596L;

	public RequestException(String string, Throwable e) {
		super(string, e);
	}

	public RequestException(String string) {
		super(string);
	}

	public RequestException(Throwable e) {
		super(e);
	}

}
