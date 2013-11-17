package com.ufasta.mobile.core.request;

import com.ufasta.mobile.core.action.ActionController.IAction;
import com.ufasta.mobile.core.action.Actions;

public abstract class AbstractRequest<T, E> {

	protected IAction action = Actions.NO_ACTION;

	protected RequestListener<T, E> requestListener;

	public AbstractRequest(IAction action) {
		this.action = action == null ? Actions.NO_ACTION : action;
	}

	public IAction getAction() {
		return action;
	}

	public abstract String getCacheKey();

	public abstract String getPath();

	public abstract void prepare();

	public abstract T execute();

	public abstract void abort();

	protected abstract T processContents(String body, Params extras) throws RequestException;

	protected abstract void operateWithContent(T content);

	public abstract T execute(Params params);

	public void setRequestListener(RequestListener<T, E> requestListener) {
		this.requestListener = requestListener;
	}

	public interface RequestListener<T, E> {

		public void requestStarted(Params extras);

		public void requestProgress(Params extras);

		public void requestSucceed(T t, Params extras);

		public void requestFailed(E e, String reason, Params extras);

		public void requestExecutionException(String reason, Params extras);
	}

}
