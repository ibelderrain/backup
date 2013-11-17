package com.ufasta.mobile.core.request;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONObject;

import com.ufasta.mobile.core.action.ActionController;
import com.ufasta.mobile.core.action.ActionController.IAction;
import com.ufasta.mobile.core.logger.Logger;
import com.ufasta.mobile.core.request.MultiPartEntity.MultiPartProgressListener;
import com.ufasta.mobile.core.request.security.EasySSLSocketFactory;

public abstract class AbstractHttpRequest<T, E> extends AbstractRequest<T, E> {

	public static boolean USE_PROXY = false;
	public static String PROXY_SERVER = "192.168.1.101";
	public static final int CONNECTION_TIME_OUT = 60 * 1000;
	public static final int CONNECTION_TIME_OUT_MULTIPART = 600 * 1000;
	public static final int SOCKET_TIME_OUT = 30 * 1000;
	public static final int SOCKET_TIME_OUT_MULTIPART = 600 * 1000;
	public static final String HTTP_SLASH = "/";
	public static final String HTTP_QUESTION = "?";
	public static final String HTTP_EQUALS = "=";
	public static final String HTTP_AMPER = "&";
	public static final String HTTP_SEMICOLON = ";";
	public static final String HTTP_COLON = ",";
	public static final String HTTP_PIPE = "|";
	public static final String UTF_8 = "utf-8";

	public int intents = 0;
	private boolean readyToExecute = false;

	public int getIntents() {
		return intents;
	}

	/**
	 * PARAMs that can be passed to the command with particular meaning.
	 */
	// public static String PARAM_PROTOCOL = "param_protocol";
	public static String PARAM_DOMAIN = "param_domain";
	public static String PARAM_SERVICE_NAME = "param_service_name";
	public static String PARAM_SERVICE_DATA = "param_service_data";
	public static String PARAM_PROGRESS = "param_progress";

	/**
	 * Default reason
	 */
	public static String DEFAULT_REASON = "Not implemented";

	// Size of the buffer
	protected int BUFFER_SIZE = 4096;

	// Common buffer use for reading and content container
	protected byte[] buffer = new byte[BUFFER_SIZE];

	protected StringBuffer mData = new StringBuffer();
	private StringBuffer postParams = new StringBuffer();
	protected List<Part> parts = new ArrayList<Part>();

	private DefaultHttpClient httpClient;

	public AbstractHttpRequest(IAction action) {
		super(action);

		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));

		HttpParams params = new BasicHttpParams();
		params.setParameter(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, 30);
		params.setParameter(ConnManagerPNames.MAX_CONNECTIONS_PER_ROUTE, new ConnPerRouteBean(30));
		params.setParameter(HttpProtocolParams.USE_EXPECT_CONTINUE, false);

		HttpConnectionParams.setConnectionTimeout(params,
				(getContentType() == ContentType.MULTIPART) ? CONNECTION_TIME_OUT_MULTIPART : CONNECTION_TIME_OUT);
		HttpConnectionParams.setSoTimeout(params,
				(getContentType() == ContentType.MULTIPART) ? SOCKET_TIME_OUT_MULTIPART : SOCKET_TIME_OUT);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);

		ClientConnectionManager cm = new SingleClientConnManager(params, schemeRegistry);

		/*
		 * Default HTTP client
		 */
		httpClient = new DefaultHttpClient(cm, params);
	}

	/**
	 * Accesor method to set HTTP client, useful for TDD
	 * 
	 * @param httpClient
	 */
	public void setHttpClient(DefaultHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Cookie container for maintain cookies between server calls
	 */
	protected static CookieStore cookieStore = new BasicCookieStore();

	public synchronized static CookieStore getCookieStore() {
		return cookieStore;
	}

	public synchronized static void setCookieStore(CookieStore cookieStore) {
		AbstractHttpRequest.cookieStore = cookieStore;
	}

	public static String authorizationHeader = null;
	public static String authorizationHeaderName = null;
	public static long lastAuthorizationTime = 0;

	public synchronized static void setCustomAuthorizationHeader(String name, String value) {
		lastAuthorizationTime = Calendar.getInstance().getTimeInMillis();
		authorizationHeader = value;
		authorizationHeaderName = name;
		if (value != null && value.length() > 0) {
			lastAuthorizationTime = Calendar.getInstance().getTimeInMillis();
		}
	}

	public synchronized static void clearAuthorization() {
		authorizationHeader = null;
		authorizationHeaderName = null;
		lastAuthorizationTime = 0;
	}

	public static long getLastAuthorizationTime() {
		return lastAuthorizationTime;
	}

	/**
	 * Clear the cookie container
	 */
	public static void clearCookies() {
		cookieStore.clear();
	}

	public enum HttpMethod {
		GET(0), POST(1), PUT(2), DELETE(3), POST_MULTIPART(4);

		private int type;

		HttpMethod(int type) {
			this.type = type;
		}

		public int getType() {
			return type;
		}
	}

	public enum ContentType {
		JSON("application/json"), FORM("application/x-www-form-urlencoded"), MULTIPART("multipart/form-data; boundary=");

		private String type;

		private ContentType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

	}

	/**
	 * Method to be executed after the parsing finished regardless there was an
	 * error or not.
	 */
	protected void requestCompleted(Params extras) {
		// Override if needed
	}

	protected abstract E processErrorContents(int statusCode, Header[] responseHeaders, String body, Params extras)
			throws RequestException;

	/**
	 * Let the implementer configure information needed for the command.<br>
	 * <br>
	 * This method shall be used to configure the parameters of the request. All
	 * this information shall be added to mData StringBuffer.
	 */
	protected void setup(Params params) {
		params.putString(PARAM_DOMAIN, getDomain());
		params.putString(PARAM_SERVICE_NAME, getPath());
	}

	protected String getStatus(JSONObject response, Params extras) {
		// JSONObject status =
		// response.optJSONObject(MgoConstants.JsonFields.STATUS);
		//
		// String code = status.optString(MgoConstants.JsonFields.ERROR_CODE);
		// String message =
		// status.optString(MgoConstants.JsonFields.ERROR_MESSAGE);
		// extras.putString(ActionController.KEY_ERROR_DESCRIPTION, message);
		// extras.putString(ActionController.KEY_ERROR_NUMBER, code);
		return "";
	}

	/**
	 * @return An String with the information common to all user commands
	 */
	protected static String getCommonData() {
		StringBuilder builder = new StringBuilder();

		// builder.append(ExtraKeys.OUTPUT);
		// builder.append(HTTP_EQUALS);
		// builder.append(API_OUTPUT);
		// builder.append(HTTP_AMPER);
		//
		// builder.append(ExtraKeys.SOURCE);
		// builder.append(HTTP_EQUALS);
		// builder.append(API_SOURCE);
		// builder.append(HTTP_AMPER);

		return builder.toString();
	}

	/**
	 * Add a key=value parameter to URL with trailing & option, and also allows
	 * to encode the value
	 * 
	 * @param key
	 *            string value of the key name
	 * @param value
	 *            value associated with the key
	 * @param amper
	 *            add or not trailing &
	 * 
	 * @param encode
	 *            url encode applied to value argument
	 */
	protected void addParameter(String key, String value, boolean amper, boolean encode) {
		String encoded = value;

		if (encode) {
			try {
				encoded = URLEncoder.encode(value, UTF_8);
			} catch (UnsupportedEncodingException e) {
				Logger.error(String.format("Can't encode parameter: key %s value: %s, this paraneter will be omited",
						key, value));
				return;
			}
		}

		mData.append(key);
		mData.append(HTTP_EQUALS);
		mData.append(encoded);
		if (amper) {
			mData.append(HTTP_AMPER);
		}

	}

	protected void addPostParameter(String key, String value) {
		addPostParameter(key, value, true, true);
	}

	protected void addPostParameter(String key, String value, boolean amper, boolean encode) {
		String encoded = value;

		if (encode) {
			try {
				encoded = URLEncoder.encode(value, UTF_8);
			} catch (UnsupportedEncodingException e) {
				Logger.error(String.format("Can't encode parameter: key %s value: %s, this paraneter will be omited",
						key, value));
				return;
			}
		}

		postParams.append(key);
		postParams.append(HTTP_EQUALS);
		postParams.append(encoded);
		if (amper) {
			postParams.append(HTTP_AMPER);
		}

	}

	protected void appendPostParam(String value) {
		String encoded = value;

		// try {
		// encoded = URLEncoder.encode(value, UTF_8);
		// } catch (UnsupportedEncodingException e) {
		// Logger.error(String.format("Can't encode parameter: key %s value: %s, this paraneter will be omited",
		// value));
		// return;
		// }

		postParams.append(encoded);
	}

	/**
	 * Add a key=value parameter to URL with & append at the end
	 * 
	 * @param key
	 *            string value of the key name
	 * @param value
	 *            value associated with the key
	 */
	protected void addParameter(String key, String value) {
		addParameter(key, value, true, true);
	}

	/**
	 * Add a key=value parameter to URL with trailing & optional, by default
	 * parameters are URL encoded
	 * 
	 * @param key
	 *            string value of the key name
	 * @param value
	 *            value associated with the key
	 * @param amper
	 *            add or not trailing &
	 */
	protected void addParameter(String key, String value, boolean amper) {
		addParameter(key, value, amper, true);
	}

	protected void addPart(Part part) {
		this.parts.add(part);
	}

	protected abstract String getDomain();

	protected abstract HttpMethod getMethod();

	protected abstract ContentType getContentType();

	protected boolean useBasicAuth() {
		return false;
	}

	protected String getBasicAuthToken() {
		return null;
	}

	@Override
	public void prepare() {

		mData.delete(0, mData.length());
		postParams.delete(0, postParams.length());

		globalParams = new Params();

		// Configure the command with relevant info for the command.
		setup(globalParams);

		// Get the base info from the extras bundle
		// NOTE: externalParams cannot be null because the caller has
		// the right checks
		String domain = globalParams.getString(PARAM_DOMAIN);
		String serviceName = globalParams.getString(PARAM_SERVICE_NAME);
		String data = globalParams.getString(PARAM_SERVICE_DATA);

		if (data == null || data.length() == 0) {
			data = mData.toString();
		}

		// This assignment is mostly used by mock and units test
		url = buildUrl(domain, serviceName, data);
		readyToExecute = true;
	}

	@Override
	public T execute() {
		return execute(null);
	}

	/**
	 * Request the Profile according the spec defined in gdoc document named
	 * "MobSquare Server API Specs"
	 */
	@Override
	public T execute(Params params) {

		if (!readyToExecute) {
			throw new IllegalStateException("You should call AbstractRequest.prepare() first");
		}

		if (params == null) {
			params = new Params();
		}
		try {
			params.putAll(globalParams);
			requestListener.requestStarted(params);
			intents++;
			HttpResponse response = executeHttpRequest(url, params);
			return handleResponse(response, params);
		} catch (Exception e) {
			e.printStackTrace();
			Params error = new Params();
			error.putInt("Status-Code", params.getInt("Status-Code"));
			error.putString(ActionController.KEY_ERROR_DESCRIPTION, e.getMessage());
			error.putString(ActionController.KEY_ERROR_TITLE, "Error");

			requestListener.requestExecutionException(e.toString(), error);
		}

		return null;
	}

	/**
	 * Process the response and the errors in a generic way
	 * 
	 * @param response
	 * @param mAction
	 *            The mAction in execution. This value is a constant inside
	 *            ActionController
	 * @param extras
	 */
	private T handleResponse(HttpResponse response, Params extras) throws RequestException {
		int statusCode = response.getStatusLine().getStatusCode();
		Logger.info("StatusCode=" + statusCode);

		StringBuilder contents = new StringBuilder();
		try {
			readContents(contents, response.getEntity().getContent());
		} catch (IllegalStateException e) {
			throw new RequestException("Can't get context response", e);
		} catch (IOException e) {
			throw new RequestException("IO exception", e);
		}

		T t = null;
		switch (statusCode) {
		case 200:
			// Process the response

			Logger.info(contents.toString());

			// Parse the information received
			t = processContents(contents.toString(), extras);

			operateWithContent(t);

			requestCompleted(extras);
			requestListener.requestSucceed(t, extras);

			break;

		case 400:
			/* Falls to 500 */
		case 401:
			/* Falls to 500 */
		case 403:
			/* Falls to 500 */
		case 404:
			/* Falls to 500 */
		case 406:
			/* Falls to 500 */
		case 500:
			extras.putInt("Status-Code", statusCode);

			E e = processErrorContents(statusCode, response.getAllHeaders(), contents.toString(), extras);

			requestCompleted(extras);

			requestListener.requestFailed(e, "StatusCode=" + statusCode, extras);

			break;
		default:
			Logger.info(String.format("Status code not handled", statusCode));
			break;
		}

		return t;
	}

	@Override
	public void abort() {
		if (httpUriRequest != null) {
			try {
				httpUriRequest.abort();
			} catch (UnsupportedOperationException e) {
				e.printStackTrace();
			}
		}
	}

	private String buildUrl(String domain, String service, String data) {

		StringBuilder req = new StringBuilder();

		if (domain != null) {
			req.append(domain);
		}

		if (service != null) {
			req.append(service);
		}

		if (data != null && data.length() > 0) {
			req.append("?");
			req.append(data);
		}

		return req.toString();
	}

	private String url = null;
	private Params globalParams;
	private HttpUriRequest httpUriRequest;

	/**
	 * Retrieve the URL construction string, this data is available after call
	 * to {@link AbstractHttpRequest#execute()} method. Mainly used for mocking
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Execute an http request (GET / POST) to the service specified in command,
	 * using the "data" parameters
	 * 
	 * @param type
	 *            - Either HTTP_TYPE_GET or HTTP_TYPE_POST. (0 or 1
	 *            respectively)
	 * @param protocol
	 *            - either http or https (don't append "://").
	 * @param domain
	 *            - The URL without http:// or https:// of the API. Must
	 *            <b>not</b> contain trailing slash (/).
	 * @param service
	 *            - The service(s) part of the url (what comes after the
	 *            domain). <b>Must start</b> with a slash "/" because it is
	 *            appended to the domain but <b>must not</b> end with "/". E.g.:
	 *            /some/api/call
	 * @param data
	 *            - Anything that comes after the "service". Don't start with a
	 *            "?" (used in HTTP_GET) because it gets automatically added.
	 * 
	 * @return HttpResponse
	 * 
	 * @throws IOException
	 *             Exception thrown when a something goes wrong with the
	 *             communication.
	 */
	protected HttpResponse executeHttpRequest(String url, final Params params) throws IOException {

		// FIXME
		// FOR CERTIFICATES ERROR

		httpUriRequest = null;

		switch (getMethod()) {
		case DELETE:
			Logger.info("DELETE REQUEST=>" + url);
			HttpDelete deleteRequest = new HttpDelete(url);
			httpUriRequest = deleteRequest;
			break;

		case PUT:
			Logger.info("PUT REQUEST=>" + url);
			HttpPut putRequest = new HttpPut(url);
			putRequest.setHeader("Content-Type", getContentType().getType());
			putRequest.setHeader("Accept", "application/json");
			putRequest.setHeader("Accepts-Encoding", "gzip");
			if (authorizationHeader != null) {
				putRequest.setHeader(authorizationHeaderName, authorizationHeader);
			}

			if (useBasicAuth()) {
				putRequest.setHeader("Authorization", "Basic " + getBasicAuthToken());
			}

			if (postParams != null && postParams.length() > 0) {
				HttpEntity entity = new StringEntity(postParams.toString());
				Logger.info("PUT REQUEST=>" + url);
				Logger.info("PUT PARAMS=>" + postParams.toString());
				putRequest.setEntity(entity);
			}

			httpUriRequest = putRequest;
			break;

		case POST:
			HttpPost postRequest = new HttpPost(url);
			postRequest.setHeader("Content-Type", getContentType().getType());
			postRequest.setHeader("Accept", "application/json");
			postRequest.setHeader("Accepts-Encoding", "gzip");
			if (authorizationHeader != null) {
				postRequest.setHeader(authorizationHeaderName, authorizationHeader);
			}

			if (useBasicAuth()) {
				postRequest.setHeader("Authorization", "Basic " + getBasicAuthToken());
			}

			if (postParams != null && postParams.length() > 0) {
				HttpEntity entity = new StringEntity(postParams.toString());
				Logger.info("POST REQUEST=>" + url);
				Logger.info("POST PARAMS=>" + postParams.toString());
				postRequest.setEntity(entity);
			}

			httpUriRequest = postRequest;
			break;

		case POST_MULTIPART:
			HttpPost postMultiRequest = new HttpPost(url);
			postMultiRequest.setHeader("Accept", "application/json");
			postMultiRequest.setHeader("Accepts-Encoding", "gzip");
			if (authorizationHeader != null) {
				postMultiRequest.setHeader(authorizationHeaderName, authorizationHeader);
			}

			if (useBasicAuth()) {
				postMultiRequest.setHeader("Authorization", "Basic " + getBasicAuthToken());
			}

			if (parts != null && parts.size() > 0) {
				HttpMethodParams httpParams = new HttpMethodParams();
				Part allParts[] = new Part[parts.size()];
				allParts = parts.toArray(allParts);
				Logger.info("POST MULTI REQUEST=>" + url);
				postMultiRequest.setEntity(new MultiPartEntity(allParts, httpParams, new MultiPartProgressListener() {

					@Override
					public void uploaded(int percent) {
						params.putInt(PARAM_PROGRESS, percent);
						requestListener.requestProgress(params);
					}
				}));
			}

			httpUriRequest = postMultiRequest;
			break;

		case GET:
			Logger.info("GET REQUEST=>" + url);
			httpUriRequest = new HttpGet(url);
			httpUriRequest.setHeader("Accept", "application/json");
			if (authorizationHeader != null) {
				httpUriRequest.setHeader(authorizationHeaderName, authorizationHeader);
			}

			if (useBasicAuth()) {
				httpUriRequest.setHeader("Authorization", "Basic " + getBasicAuthToken());
			}

			break;
		}

		if (httpUriRequest == null)
			return null;

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();
		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		if (USE_PROXY) {
			HttpHost proxy = new HttpHost(PROXY_SERVER, 8080);
			httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
		}

		// if (useBasicAuth()) {
		// CredentialsProvider credProvider = new BasicCredentialsProvider();
		// credProvider.setCredentials(new AuthScope(AuthScope.ANY_HOST,
		// AuthScope.ANY_PORT),
		// new UsernamePasswordCredentials(getBasicAuthUsername(),
		// getBasicAuthPassword()));
		// httpClient.setCredentialsProvider(credProvider);
		//
		// }

		HttpResponse response = httpClient.execute(httpUriRequest, localContext);

		if (cookieStore != null && cookieStore.getCookies() != null) {
			for (int i = 0; i < cookieStore.getCookies().size(); i++) {
				Logger.error("Cookie: " + cookieStore.getCookies().get(i).getName() + "="
						+ cookieStore.getCookies().get(i).getValue());
			}
		}

		return response;
	}

	@Override
	public String getCacheKey() {
		return (getMethod() == HttpMethod.GET) ? getMethod().toString().concat(url) : null;
	}

	/**
	 * Read the content from the input stream specified and store it in the
	 * StringBuffer specified.
	 * 
	 * @param content
	 *            An instance for the StringBuilder that will hold all the
	 *            incoming data. If this is used from a BaseCommand, the
	 *            "contents" member shall be passed.
	 * @param is
	 *            The input stream from the http response.
	 */
	private void readContents(StringBuilder content, InputStream is) throws IOException {
		int readBytes = 0;
		while ((readBytes = is.read(buffer, 0, BUFFER_SIZE)) > 0) {
			content.append(new String(buffer, 0, readBytes));
		}
	}

}
