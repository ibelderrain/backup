package com.ufasta.mobile.core.request;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.ufasta.mobile.core.logger.Logger;

public class CacheRequest {

	private Map<String, CacheElement> cache;

	public class CacheElement {

		private Object object;
		private long time;

		CacheElement(Object object) {
			this.object = object;
			this.time = System.currentTimeMillis();
		}

		public synchronized void reset() {
			time = System.currentTimeMillis();
		}

		public synchronized Object get() {
			time = System.currentTimeMillis();
			return object;
		}

		public synchronized long getTime() {
			return time;
		}
	}

	public interface PolicyReplacement {
		public boolean replace(long time, Object element);
	}

	public CacheRequest() {
		this(new PolicyReplacement() {

			@Override
			public boolean replace(long time, Object element) {
				return false; // default behavior, cache elements never are
								// replaced
			}
		});
	}

	private PolicyReplacement policyReplacement;

	public CacheRequest(PolicyReplacement policyReplacement) {
		this.policyReplacement = policyReplacement;
		initCache();
	}

	private void initCache() {
		cache = Collections.synchronizedMap(new LinkedHashMap<String, CacheElement>());
	}

	@SuppressWarnings("unchecked")
	public <T> T exists(AbstractRequest<T, ?> request) {
		CacheElement ce = (request.getCacheKey() != null) ? cache.get(request.getCacheKey()) : null;
		return (ce != null) ? (T) ce.get() : null;
	}

	/**
	 * <p>
	 * Retrieve the object response for the current request from the cache. If
	 * the object is not found in the cache, a server call will be made to
	 * hopefully retrieve it.
	 * </p>
	 * <p>
	 * If the sever responds with null, the response won't be cached and a null
	 * will be returned to the caller.
	 * </p>
	 * 
	 * @param clazz
	 *            Request response class
	 * @param request
	 *            Request instance to execute
	 * @return response value
	 */
	public <T> T get(AbstractRequest<T, ?> request) {

		String path = request.getCacheKey();
		CacheElement ce = (path != null) ? cache.get(path) : null;

		if (ce != null) {
			if (!policyReplacement.replace(ce.getTime(), ce.get())) {
				@SuppressWarnings("unchecked")
				T t = (T) ce.get();
				Logger.info(String.format("[cached] Path %s in cache with value %s", path, t.toString()));
				return t;
			}

			Logger.info(String.format(
					"[expired] Path %s in cache but due policy replacement, we must update current element %s", path));
		}

		// Need to request.
		T t = request.execute();

		if (t == null) {
			Logger.info(String.format("[invalid] null response we can't cache this."));
			return null; // We do not allow null values
		}

		if (path != null) {
			Logger.info(String.format("[new] Path %s added to cache with value %s", path, t.toString()));
			cache.put(path, new CacheElement(t));
		}

		return t;
	}

	/**
	 * <p>
	 * Force the request against server, result will be cached if not null,
	 * previous key/value entry will be overwritten.
	 * </p>
	 * 
	 * @param clazz
	 *            response class
	 * @param request
	 *            instance to execute
	 * @return response value
	 */
	public <T> T force(AbstractRequest<T, ?> request) {

		// Need to request.
		T t = request.execute();

		if (t == null) {
			return null; // We do not allow null values
		}

		String path = request.getCacheKey();
		// Adding response to Cache
		if (path != null) {
			Logger.info(String.format("[force] Path %s entry overritten in cache with value %s", path, t.toString()));
			cache.put(path, new CacheElement(t));
		}

		return t;
	}

	/**
	 * <p>
	 * Clears (or inits) the App's Cache.
	 * </p>
	 */
	public void clear() {
		if (cache != null) {
			cache.clear();
		} else {
			initCache();
		}
	}

}
