package com.ufasta.mobile.core.request;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;

public class MultiPartEntity implements HttpEntity {

	private MultipartRequestEntity wrapped;

	private MultiPartProgressListener progressListener = null;

	public MultiPartEntity(Part[] parts, HttpMethodParams params,
			MultiPartProgressListener progressListener) {
		wrapped = new MultipartRequestEntity(parts, params);
		this.progressListener = progressListener;
	}

	@Override
	public void consumeContent() throws IOException {
		return;
	}

	@Override
	public InputStream getContent() throws IOException, IllegalStateException {
		return null;
	}

	@Override
	public Header getContentEncoding() {
		return null;
	}

	@Override
	public boolean isChunked() {
		return true;
	}

	@Override
	public boolean isStreaming() {
		return wrapped.isRepeatable();
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		wrapped.writeRequest(new CoutingOutputStream(outstream));
	}

	@Override
	public long getContentLength() {
		return wrapped.getContentLength();
	}

	@Override
	public Header getContentType() {
		return new Header() {

			@Override
			public String getValue() {
				return wrapped.getContentType();
			}

			@Override
			public String getName() {
				return "Content-Type";
			}

			@Override
			public HeaderElement[] getElements() throws ParseException {
				return null;
			}
		};
	}

	@Override
	public boolean isRepeatable() {
		return wrapped.isRepeatable();
	}

	public void setProgressListener(MultiPartProgressListener progressListener) {
		this.progressListener = progressListener;
	}

	public class CoutingOutputStream extends FilterOutputStream {

		private int sum = 0;

		CoutingOutputStream(final OutputStream out) {
			super(out);
		}

		@Override
		public void write(int b) throws IOException {
			sum++;
			out.write(b);
			if (progressListener != null) {
				progressListener.uploaded(Math.round(sum * 100
						/ getContentLength()));
			}
		}

		@Override
		public void write(byte[] b) throws IOException {
			sum += b.length;
			out.write(b);
			if (progressListener != null) {
				progressListener.uploaded(Math.round(sum * 100
						/ getContentLength()));
			}
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			sum += len;
			out.write(b, off, len);
			if (progressListener != null) {
				progressListener.uploaded(Math.round(sum * 100
						/ getContentLength()));
			}
		}

	}

	public interface MultiPartProgressListener {

		public void uploaded(int percent);

	}

}
