package csd2324.trab1.clients;

import csd2324.trab1.api.java.Result;
import csd2324.trab1.api.java.Result.ErrorCode;
import csd2324.trab1.utils.InsecureHostnameVerifier;
import csd2324.trab1.utils.Sleep;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import javax.net.ssl.HttpsURLConnection;
import java.util.function.Supplier;
import java.util.logging.Logger;

import static csd2324.trab1.api.java.Result.error;
import static csd2324.trab1.api.java.Result.ok;


public class RestClient {
	private static Logger Log = Logger.getLogger(RestClient.class.getName());

	protected static final int READ_TIMEOUT = 10000;
	protected static final int CONNECT_TIMEOUT = 10000;

	protected static final int MAX_RETRIES = 10;
	protected static final int RETRY_SLEEP = 1000;

	final Client client;
	final String serverURI;
	final ClientConfig config;

	RestClient(String serverURI) {
		this.serverURI = serverURI;
		this.config = new ClientConfig();
		config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
		config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

		this.client = ClientBuilder.newClient(config);
		HttpsURLConnection.setDefaultHostnameVerifier(new InsecureHostnameVerifier());

	}

	protected <T> Result<T> reTry(Supplier<Result<T>> func) {
		for (int i = 0; i < MAX_RETRIES; i++)
			try {
				return func.get();
			} catch (ProcessingException x) {
				Log.fine("Timeout: " + x.getMessage());
				Sleep.ms(RETRY_SLEEP);
			} catch (Exception x) {
				return error(ErrorCode.INTERNAL_ERROR);
			}
		System.err.println("TIMEOUT...");
		return error(ErrorCode.TIMEOUT);
	}

	protected <T> Result<T> toJavaResult(Response r) {
		try {
			var status = r.getStatusInfo().toEnum();
			if (status == Status.OK && r.hasEntity())
				return ok(r.readEntity(new GenericType<T>(){}));
			else
				if( status == Status.NO_CONTENT) return ok();

			return error(getErrorCodeFrom(status.getStatusCode()));
		} finally {
			r.close();
		}
	}

	protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
		try {
			var status = r.getStatusInfo().toEnum();
			if (status == Status.OK && r.hasEntity())
				return ok(r.readEntity(entityType));
			else 
				if( status == Status.NO_CONTENT) return ok();
			
			return error(getErrorCodeFrom(status.getStatusCode()));
		} finally {
			r.close();
		}
	}
	
	protected <T> Result<T> toJavaResult(Response r, GenericType<T> entityType) {
		try {
			var status = r.getStatusInfo().toEnum();
			if (status == Status.OK && r.hasEntity())
				return ok(r.readEntity(entityType));
			else 
				if( status == Status.NO_CONTENT) return ok();
			
			return error(getErrorCodeFrom(status.getStatusCode()));
		} finally {
			r.close();
		}
	}
	
	public static ErrorCode getErrorCodeFrom(int status) {
		return switch (status) {
		case 200, 209 -> ErrorCode.OK;
		case 409 -> ErrorCode.CONFLICT;
		case 403 -> ErrorCode.FORBIDDEN;
		case 404 -> ErrorCode.NOT_FOUND;
		case 400 -> ErrorCode.BAD_REQUEST;
		case 501 -> ErrorCode.NOT_IMPLEMENTED;
		default -> ErrorCode.INTERNAL_ERROR;
		};
	}

	@Override
	public String toString() {
		return serverURI.toString();
	}
}
