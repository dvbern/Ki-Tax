package ch.dvbern.ebegu.ws.oicd;

import ch.dvbern.ebegu.errors.OIDCServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

public class OIDCClient {

	private static final String GRANT_TYPE = "client_credentials";
	private static final String ISSUE_TOKEN_METHOD_NAME = "issueToken";
	private String clientId;
	private String endpoint;
	private String secret;


	public OIDCClient cientId(String clientId) {
		this.clientId = clientId;
		return this;
	}

	public OIDCClient endpoint(String endpoint) {
		this.endpoint = endpoint;
		return this;
	}

	public OIDCClient secret(String secret) {
		this.secret = secret;
		return this;
	}

	public OIDCToken issueToken() throws OIDCServiceException {
		validateInput();
		return requestToken();
	}

	private void validateInput() throws OIDCServiceException {
		if (StringUtils.isBlank(this.clientId)) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Es wurde keine Client-ID für das Abfragen des Auth-Tokens definiert");
		}

		if (StringUtils.isBlank(this.secret)) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Es wurde kein Client-Secret für das Abfragen des Auth-Tokens definiert");
		}

		if (StringUtils.isBlank(this.endpoint)) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Es wurde keine Endpoint für das Abfragen des Auth-Tokens definiert");
		}
	}

	private OIDCToken requestToken() throws OIDCServiceException {
		HttpPost httpPost = buildHttpPostRequest();
		LocalDateTime requestTime = LocalDateTime.now();

		try (CloseableHttpResponse response = executePostRequest(httpPost)) {
			String responseBody = handleResponse(response);
			return mapRepsonseToToken(responseBody, requestTime);
		} catch (IOException e) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Error during Auth-Tokens Request", e);
		}
	}

	private HttpPost buildHttpPostRequest() throws OIDCServiceException {
		try {
			HttpPost httpPost = new HttpPost(endpoint);
			httpPost.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
			httpPost.setEntity(new StringEntity(buildRequestBody()));
			return httpPost;
		} catch (UnsupportedEncodingException e) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Error Build of Request", e);
		}
	}

	private String buildRequestBody() {
		// Build the request body
		return "grant_type=" + GRANT_TYPE +
			"&client_id=" + this.clientId +
			"&client_secret=" + this.secret;
	}

	private CloseableHttpResponse executePostRequest(HttpPost httpPost) throws OIDCServiceException {
		try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
			return httpClient.execute(httpPost);
		} catch (IOException e) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Error during execution of Auth-Tokens Request", e);
		}
	}

	private String handleResponse(CloseableHttpResponse response) throws OIDCServiceException {
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)  {
			throw new OIDCServiceException(
				ISSUE_TOKEN_METHOD_NAME,
				"Auth-Token Response not OK " +
				response.getStatusLine().getStatusCode() +
				response.getStatusLine().getReasonPhrase());
		}

		try {
			return EntityUtils.toString(response.getEntity());
		} catch (IOException e) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Token Response Parsing Error", e);
		}
	}

	private OIDCToken mapRepsonseToToken(String responseBody, LocalDateTime requestTime) throws OIDCServiceException {
		try {
			OIDCToken token = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.readValue(responseBody, OIDCToken.class);
			token.setRequestTime(requestTime);
			return token;
		} catch (JsonProcessingException e) {
			throw new OIDCServiceException(ISSUE_TOKEN_METHOD_NAME, "Token Response could not me mapped into OIDCToken ", e);
		}
	}
}
