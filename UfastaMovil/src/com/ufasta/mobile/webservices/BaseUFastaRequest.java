package com.ufasta.mobile.webservices;

import org.apache.http.Header;

import com.ufasta.mobile.Constants;
import com.ufasta.mobile.core.action.ActionController.IAction;
import com.ufasta.mobile.core.request.AbstractHttpRequest;
import com.ufasta.mobile.core.request.Params;
import com.ufasta.mobile.core.request.RequestException;
import com.ufasta.mobile.model.Error;

public abstract class BaseUFastaRequest<T> extends AbstractHttpRequest<T, Error> {

	public BaseUFastaRequest(IAction action) {
		super(action);
	}

	@Override
	protected Error processErrorContents(int statusCode, Header[] responseHeaders, String body, Params extras)
			throws RequestException {
		return null;
	}

	@Override
	protected String getDomain() {
		return Constants.API_DOMAIN;
	}

}
