package net.suteren.netatmo.therm;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import net.suteren.netatmo.auth.AuthClient;
import net.suteren.netatmo.client.AbstractApiClient;
import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.client.NetatmoResponse;
import net.suteren.netatmo.domain.therm.Schedule;

public final class ScheduleClient extends AbstractApiClient {

	public ScheduleClient(AuthClient auth) {
		super(auth);
	}

	public NetatmoResponse updateSchedule(Schedule schedule) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return updateSchedule(schedule, null);
	}

	public NetatmoResponse updateSchedule(Schedule schedule, String homeId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		schedule = schedule.toBuilder()
			.scheduleId(schedule.effectiveId())
			.id(null)
			.isDefaultZone(null)
			.type(null)
			.homeId(Optional.ofNullable(schedule.homeId())
				.filter(string -> !string.isBlank())
				.orElse(homeId))
			.zones(schedule.zones().stream().map(z -> z.toBuilder().roomstemp(null).build()).toList())
			.build();
		return OBJECT_MAPPER.readValue(post("synchomeschedule", null, OBJECT_MAPPER.writeValueAsString(schedule), "application/json"), NetatmoResponse.class);
	}

	public NetatmoResponse setSchedule(String homeId, String scheduleId) throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return OBJECT_MAPPER.readValue(
			post("switchhomeschedule", null, queryParams(Map.of("home_id", homeId, "schedule_id", scheduleId)), URLENCODED_CHARSET_UTF_8),
			NetatmoResponse.class);
	}
}
