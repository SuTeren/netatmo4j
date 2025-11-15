package net.suteren.netatmo.domain.therm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
public record Schedule(
	@JsonProperty("id") String id,
	@JsonProperty("home_id") String homeId,
	@JsonProperty("schedule_id") String scheduleId,
	String name,
	List<Zone> zones,
	List<TimetableEntry> timetable,
	@JsonProperty("hg_temp") Integer hgTemp,
	@JsonProperty("away_temp") Integer awayTemp,
	@JsonProperty("default") Boolean isDefaultZone,
	Boolean selected,
	String type
) {

	@JsonIgnore public String effectiveId() {
		return Optional.ofNullable(id).orElse(scheduleId);
	}

	@JsonIgnore public Optional<Zone> getZoneById(int zoneId) {
		return zones.stream()
			.filter(z -> Objects.equals(z.id(), zoneId))
			.findAny();
	}
}
