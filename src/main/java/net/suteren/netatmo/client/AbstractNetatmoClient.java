package net.suteren.netatmo.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

/**
 * boilerplate for specific Netatmo API calls.
 * Provides common functions to call the api and to all related stuff.
 */
@Setter @Getter @Slf4j
public abstract class AbstractNetatmoClient {

	/**
	 * Mimetype of <code>x-www-form-urlencoded</code>.
	 */
	public static final String URLENCODED_CHARSET_UTF_8 = "application/x-www-form-urlencoded;charset=UTF-8";
	protected static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/**
	 * Base URL of the NEtatmo API server.
	 */
	public static final String NETATMO_BASE_URL = "https://api.netatmo.com";

	/**
	 * GET request type.
	 */
	public static final String HTTP_METHOD_GET = "GET";
	/**
	 * POST request type.
	 */
	public static final String HTTP_METHOD_POST = "POST";
	private HttpURLConnection connection;

	/**
	 * Send a POST request to the Netatmo API server.
	 *
	 * @param path URL path.
	 * @param params query parameters.
	 * @param content body of the POST request.
	 * @param contentType mimetype as the content.
	 * @return content body of the response.
	 * @throws IOException
	 * @throws ConnectionException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public InputStream post(String path, Map<String, String> params, Object content,
		String contentType) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return callNetatmo(HTTP_METHOD_POST, path, content, contentType, params, null);
	}

	/**
	 * Send a GET request to the Netatmo API server.
	 *
	 * @param path URL path.
	 * @param params query parameters.
	 * @return content body of the response.
	 * @throws IOException
	 * @throws ConnectionException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public InputStream get(String path, Map<String, String> params) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return callNetatmo(HTTP_METHOD_GET, path, null, null, params, null);
	}

	/**
	 * Send a GET request to the Netatmo API server.
	 *
	 * @param path URL path.
	 * @return content body of the response.
	 * @throws IOException
	 * @throws ConnectionException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	public InputStream get(String path) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		return callNetatmo(HTTP_METHOD_GET, path, null, null, null, null);
	}

	/**
	 * Common code for all request types.
	 *
	 * @param method type of the request.
	 * @param path URL path.
	 * @param content body of the POST request.
	 * @param contentType mimetype as the content.
	 * @param params query parameters.
	 * @param headers request headers.
	 * @return content body of the response.
	 * @throws IOException
	 * @throws ConnectionException
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 */
	protected InputStream callNetatmo(String method, String path, Object content, String contentType,
		Map<String, String> params, Map<String, String> headers)
		throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		connection = ((HttpURLConnection) (new URL(constructUrl(path, params)).openConnection()));
		connection.setRequestMethod(method);
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", contentType);

		Optional.ofNullable(headers)
			.map(Map::entrySet)
			.stream().flatMap(Collection::stream)
			.forEach(e -> connection.setRequestProperty(e.getKey(), e.getValue()));

		log.debug("URL: {}", getConnection().getURL());
		log.debug("Content: {}", content);
		log.debug("Headers: {}", getConnection().getRequestProperties());

		if (content != null) {
			if (content instanceof Reader reader) {
				IOUtils.copy(reader, connection.getOutputStream());
			} else if (content instanceof InputStream inputStream) {
				IOUtils.copy(inputStream, connection.getOutputStream());
			} else if (content instanceof String string) {
				IOUtils.copy(IOUtils.toInputStream(string, StandardCharsets.UTF_8), connection.getOutputStream());
			} else {
				throw new IllegalStateException("Only InputStream, Reader or String are supported.");
			}
		}

		if (connection.getResponseCode() == 200) {
			return (InputStream) connection.getContent();
		} else {
			NetatmoError netatmoError;
			String errorMessage = IOUtils.toString(connection.getErrorStream());
			try {
				netatmoError = OBJECT_MAPPER.readValue(errorMessage, NetatmoError.class);
			} catch (Exception e) {
				netatmoError = new NetatmoError(new NetatmoError.NetatmoErrorInfo(-1L, errorMessage));
			}
			throw new ConnectionException(connection, netatmoError.error());
		}
	}

	/**
	 * Construct URL encoded string of query parameters passed as a map.
	 *
	 * @param params as a map.
	 * @return parameter part of the query URL.
	 */
	public static String queryParams(Map<String, String> params) {
		return params.entrySet().stream()
			.map(e -> "%s=%s".formatted(urlEncode(e.getKey()), urlEncode(e.getValue())))
			.collect(Collectors.joining("&"));
	}

	/**
	 * Construct ful URL of the request to the Netatmo API server.
	 *
	 * @param path URL path.
	 * @param params query parameters.
	 * @return URL of the request for the Netatmo API server.
	 */
	public static String constructUrl(String path, Map<String, String> params) {
		return NETATMO_BASE_URL + sanitizePath(path) + Optional.ofNullable(params)
			.map(AbstractNetatmoClient::queryParams)
			.map("?%s"::formatted)
			.orElse("");
	}

	/**
	 * Append leading slash to the URL if not present.
	 *
	 * @param url to sanitize.
	 * @return sanitized URL.
	 */
	public static String sanitizePath(String url) {
		return url.startsWith("/") ? url : ("/" + url);
	}

	/**
	 * Shortcut for {@link URLEncoder#encode(String, String)}
	 *
	 * @param value to encode.
	 * @param charset to use for encoding.
	 * @return encoded value.
	 */
	protected static String urlEncode(String value, Charset charset) {
		return URLEncoder.encode(value, charset);
	}

	/**
	 * Shortcut for {@link URLEncoder#encode(String, String)}
	 *
	 * @param value to encode.
	 * @return encoded value.
	 */
	@SneakyThrows
	protected static String urlEncode(String value) {
		return urlEncode(value, StandardCharsets.UTF_8);
	}
}
