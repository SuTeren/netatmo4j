package net.suteren.netatmo.auth;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;
import lombok.Setter;
import net.suteren.netatmo.client.AbstractNetatmoClient;
import net.suteren.netatmo.client.ConnectionException;

import static java.nio.file.attribute.PosixFilePermission.OWNER_READ;
import static java.nio.file.attribute.PosixFilePermission.OWNER_WRITE;

public final class AuthClient extends AbstractNetatmoClient {

	private final String clientId;
	private final Collection<Scope> scope;
	private final String state;
	@Setter @Getter private String accessToken;
	@Setter @Getter private String refreshToken;
	@Setter @Getter private Instant validUntil;
	private final String clientSecret;
	private final OAuth2 oauth;
	private final File authFile;

	/**
	 * Retrieves OAUTH2 token.
	 * It is required by {@link net.suteren.netatmo.client.AbstractApiClient} to provide valid `Authentication` token.
	 * See <a href="https://dev.netatmo.com/apidocumentation/oauth">Netatmo Authentication</a> for more details.
	 *
	 * @param clientId `client_id` used to retrieve the authentication token.
	 * @param clientSecret `client_secret` used to retrieve the authentication token.
	 * @param scope scopes to authorize to.
	 * If no scope is provided during the token request, the default is {@link Scope#read_station}
	 * @param state to prevent Cross-site Request Forgery.
	 * @param authFile local file to store tokens to. This file should be protected from unauthorized reading.
	 * @throws IOException in case of connection problems of problems accessing the `authFile`.
	 */
	public AuthClient(String clientId, String clientSecret, Collection<Scope> scope, String state, File authFile) throws IOException {
		this.authFile = authFile;
		if (this.authFile.exists()) {
			fixFilePermissions(this.authFile);
			JsonNode tokens = OBJECT_MAPPER.readTree(this.authFile);
			accessToken = tokens.at("/access_token").textValue();
			refreshToken = tokens.at("/refresh_token").textValue();
			validUntil = Instant.ofEpochSecond(tokens.at("/valid_until").longValue());
			if (StringUtils.isBlank(clientId)) {
				clientId = tokens.at("/client_id").textValue();
			}
			if (StringUtils.isBlank(clientSecret)) {
				clientSecret = tokens.at("/client_secret").textValue();
			}
		}

		this.scope = scope;
		this.state = state;
		oauth = new OAuth2();
		this.clientSecret = clientSecret;
		this.clientId = clientId;

	}

	/**
	 * Entrance URL to log in and approve the client.
	 *
	 * @param redirectUri url of this application to pass the auth code to.
	 * @return URL to open in the browser.
	 */
	public String authorizeUrl(String redirectUri) {
		LinkedHashMap<String, String> map = new LinkedHashMap<String, String>(4);
		map.put("client_id", clientId);
		map.put("redirect_uri", redirectUri);
		map.put("scope", renderScope(scope));
		map.put("state", state);
		return constructUrl("oauth2/authorize", map);
	}

	/**
	 * Retrieves the authorization token from cache, and try to refresh it in case it is going to expire.
	 * If there is no authorization token in the cache, start OAuth2 process to log in and retrieve the token.
	 *
	 * @return authorization token
	 * @throws URISyntaxException if {@link #authorizeUrl(String)} return wrong URL.
	 * @throws IOException in the case of local callback server issues.
	 * @throws ConnectionException in case of error from Netatmo API server during retrieving the token.
	 */
	public String getToken() throws URISyntaxException, IOException, InterruptedException, ConnectionException {
		if (accessToken == null) {
			oauth.authorize(this::authorizeUrl);
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>(6);
			parameters.put("grant_type", "authorization_code");
			parameters.put("client_id", clientId);
			parameters.put("client_secret", clientSecret);
			parameters.put("code", oauth.getCode());
			parameters.put("redirect_uri", oauth.getRedirectUri());
			parameters.put("scope", renderScope(scope));
			token(parameters);
		} else if (validUntil.isBefore(Instant.now().minus(1, ChronoUnit.MINUTES))) {
			LinkedHashMap<String, String> parameters = new LinkedHashMap<String, String>(4);
			parameters.put("grant_type", "refresh_token");
			parameters.put("client_id", clientId);
			parameters.put("client_secret", clientSecret);
			parameters.put("refresh_token", refreshToken);
			token(parameters);
		}

		return accessToken;
	}

	private void token(LinkedHashMap<String, String> parameters) throws IOException, ConnectionException, URISyntaxException, InterruptedException {
		JsonNode response =
			OBJECT_MAPPER.readTree(post("oauth2/token", null, queryParams(parameters), URLENCODED_CHARSET_UTF_8));
		accessToken = response.at("/access_token").textValue();
		refreshToken = response.at("/refresh_token").textValue();
		validUntil = Instant.now().plusSeconds(response.at("/expires_in").longValue());
		LinkedHashMap<String, Serializable> result = new LinkedHashMap<String, Serializable>(5);
		result.put("access_token", getAccessToken());
		result.put("refresh_token", getRefreshToken());
		result.put("valid_until", getValidUntil().getEpochSecond());
		result.put("client_id", clientId);
		result.put("client_secret", clientSecret);
		try (FileWriter w = new FileWriter(authFile)) {
			OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValue(w, result);
		}
		fixFilePermissions(authFile);
	}

	private static String renderScope(Collection<Scope> scope) {
		return scope.stream()
			.map(Object::toString)
			.collect(Collectors.joining(" "));
	}

	private static void fixFilePermissions(File authFile) throws IOException {
		Files.setPosixFilePermissions(authFile.toPath(), Set.of(OWNER_READ, OWNER_WRITE));
	}

	public enum Scope {
		/**
		 * to retrieve weather station data (Getstationsdata, Getmeasure)
		 */
		read_station,

		/**
		 * to retrieve thermostat data ( Homestatus, Getroommeasure...)
		 */
		read_thermostat,

		/**
		 * to set up the thermostat (Synchomeschedule, Setroomthermpoint...)
		 */
		write_thermostat,

		/**
		 * to retrieve Smart Indoor Cameradata (Gethomedata, Getcamerapicture...)
		 */
		read_camera,

		/**
		 * to inform the Smart Indoor Camera that a specific person or everybody has left the Home (Setpersonsaway, Setpersonshome)
		 */
		write_camera,

		/**
		 * to access the camera, the videos and the live stream.
		 * Netatmo cares a lot about users privacy and security. The "access" scope grants you access to sensitive data and is delivered by Netatmo teams on a per-app basis. To submit an access scope request, see <a href="https://dev.netatmo.com/request-scope-form">here</a>.
		 */
		access_camera,

		/**
		 * to retrieve Smart Outdoor Camera data (Gethomedata, Getcamerapicture...)
		 */
		read_presence,

		/**
		 * to access the camera, the videos and the live stream.
		 * Netatmo cares a lot about users privacy and security. The "access" scope grants you access to sensitive data and is delivered by Netatmo teams on a per-app basis. To submit an access scope request, see <a href="https://dev.netatmo.com/request-scope-form">here</a>.
		 */
		access_presence,

		/**
		 * to retrieve the Smart Smoke Alarm informations and events (Gethomedata, Geteventsuntil...)
		 */
		read_smokedetector,

		/**
		 * to read data coming from Smart Indoor Air Quality Monitor (gethomecoachsdata)
		 */
		read_homecoach,
	}
}
