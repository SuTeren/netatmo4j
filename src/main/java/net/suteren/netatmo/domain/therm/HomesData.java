package net.suteren.netatmo.domain.therm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Data about all available homes for the current user.
 *
 * @param homes list of homes with theyir data as {@link Home}.
 * @param user
 */
public record HomesData(
	List<Home> homes,
	User user
) {

	/**
	 * Get single {@link Home} by homeId if it exists.
	 *
	 * @param homeId of the home to be retrieved.
	 * @return optional of a single {@link Home} instance.
	 */
	@JsonIgnore public Optional<Home> getHomeById(String homeId) {
		return homes.stream()
			.filter(h -> Objects.equals(h.id(), homeId))
			.findAny();
	}
}
