package net.suteren.netatmo.client;

import org.apache.commons.collections4.MapUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import net.suteren.netatmo.auth.AuthClient;

/**
 * Extends the generic client by authorization information.
 * Using this client, <code>Authorization: Bearer $TOKEN</code> is added to the request.
 */
public class AbstractApiClient extends AbstractNetatmoClient {
	/**
	 * Creates a new AbstractApiClient with the given AuthClient.
	 *
	 * @param auth the AuthClient to use for authorization.
	 */
	public AbstractApiClient(AuthClient auth) {
		this.auth = auth;
	}

	@Override protected InputStream callNetatmo(String method, String path, Object content, String contentType,
		Map<String, String> params, Map<String, String> headers)
		throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		if (MapUtils.isEmpty(headers)) {
			headers = new LinkedHashMap<>();
		}
		headers.put("Authorization", "Bearer %s".formatted(auth.getToken()));
		return super.callNetatmo(method, "/api" + sanitizePath(path), content, contentType, params, headers);
	}

	private final AuthClient auth;
}
