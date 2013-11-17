package com.ufasta.mobile.core.request;

import android.annotation.SuppressLint;

import com.ufasta.mobile.core.action.ActionController;
import com.ufasta.mobile.core.logger.Logger;
import com.ufasta.mobile.core.request.AbstractRequest.RequestListener;

public class RequestExecutor {

	public static final String PARAM_ERROR = "paramError";
	public static final String PARAM_FAILED_REQUEST = "paramFailedRequest";
	public static final String PARAM_FAILED_LISTENER = "paramFailedListener";

	protected ActionController actionController;

	protected CacheRequest cacheRequest;

	public RequestExecutor(ActionController actionController,
			CacheRequest cacheRequest) {
		this.actionController = actionController;
		this.cacheRequest = cacheRequest;
	}

	public <T, E> T sync(final AbstractRequest<T, E> request) {
		return sync(request, null);
	}

	public <T, E> T sync(final AbstractRequest<T, E> request, Params parameters) {
		request.prepare();
		RequestListener<T, E> requestListener = buildRequestListener(request);
		request.setRequestListener(requestListener);
		T result = cacheRequest.exists(request);
		if (result != null) {
			requestListener.requestStarted(parameters);
			request.operateWithContent(result);
			requestListener.requestSucceed(result, parameters);
		} else {
			result = cacheRequest.get(request);
		}
		return result;
	}

	public <T, E> T force(final AbstractRequest<T, E> request) {
		return force(request, null);
	}

	public <T, E> T force(final AbstractRequest<T, E> request, Params parameters) {
		request.prepare();
		request.setRequestListener(buildRequestListener(request));
		return cacheRequest.force(request);
	}

	public <T, E> void async(final AbstractRequest<T, E> request) {
		async(request, null);
	}

	public <T, E> void async(final AbstractRequest<T, E> request,
			final Params parameters) {

		new Thread() {

			@Override
			public void run() {
				sync(request, parameters);
			}
		}.start();
	}

	public <T, E> void forceAsync(final AbstractRequest<T, E> request) {
		forceAsync(request, null);
	}

	public <T, E> void forceAsync(final AbstractRequest<T, E> request,
			final Params parameters) {

		new Thread() {

			@Override
			public void run() {
				force(request, parameters);
			}
		}.start();
	}

	public <T, E> void abort(final AbstractRequest<T, E> request) {
		request.abort();
	}

	public <T, E> void abortAsync(final AbstractRequest<T, E> request) {
		new Thread() {
			@Override
			public void run() {
				abort(request);
			}
		}.start();
	}

	@SuppressLint("DefaultLocale")
	protected <T, E> RequestListener<T, E> buildRequestListener(
			final AbstractRequest<T, E> request) {
		return new RequestListener<T, E>() {

			@Override
			public void requestStarted(Params extras) {
				actionController.setActionStatus(request.getAction(),
						ActionController.State.STATE_INPROGRESS, extras);

				Logger.info(String.format("ACTION: %s %s", request.getAction()
						.getName(), ActionController.State.STATE_INPROGRESS));

			}

			@Override
			public void requestSucceed(T t, Params extras) {

				actionController.setActionStatus(request.getAction(),
						ActionController.State.STATE_DONE, extras);

				Logger.info(String.format("ACTION: %s %s", request.getAction()
						.getName(), ActionController.State.STATE_DONE));

			}

			@Override
			public void requestFailed(E e, String reason, Params extras) {
				extras.put(PARAM_ERROR, e);
				actionController.setActionStatus(request.getAction(),
						ActionController.State.STATE_ERROR, extras);
				Logger.info(String.format("ACTION: %s %s", request.getAction()
						.getName(), ActionController.State.STATE_ERROR));

			}

			@Override
			public void requestExecutionException(String reason, Params extras) {
				actionController.setActionStatus(request.getAction(),
						ActionController.State.STATE_EXCEPTION, extras);

				Logger.info(String.format("ACTION: %s %s", request.getAction()
						.getName(), ActionController.State.STATE_EXCEPTION));

			}

			@Override
			public void requestProgress(Params extras) {
				actionController.setActionStatus(request.getAction(),
						ActionController.State.STATE_INPROGRESS, extras);

				Logger.info(String.format("ACTION: %s %s, uploaded=%d", request
						.getAction().getName(),
						ActionController.State.STATE_INPROGRESS, extras
								.getInt(AbstractHttpRequest.PARAM_PROGRESS)));

			}

		};
	}
}
