package net.suteren.netatmo.cli.command.update;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import net.suteren.netatmo.ZonePresets;
import net.suteren.netatmo.cli.command.AbstractCommand;
import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.client.NetatmoResponse;
import net.suteren.netatmo.domain.therm.Home;
import net.suteren.netatmo.domain.therm.Schedule;
import net.suteren.netatmo.therm.ScheduleClient;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "schedule")
class UpdateScheduleCommand extends AbstractCommand {

	@Override public Integer call() throws Exception {
		ZonePresets zonePresets = new ZonePresets(getCfg());
		ScheduleClient scheduleClient = new ScheduleClient(getAuthClient());
		getHomeClient().getHomesData().getHomeById(getHomeId())
			.map(Home::schedules)
			.orElse(List.of())
			.forEach(schedule ->
				callUpdateSchedule(schedule, scheduleClient, zonePresets)
			);
		return 0;
	}

	private NetatmoResponse callUpdateSchedule(Schedule schedule, ScheduleClient scheduleClient, ZonePresets zonePresets) {
		try {
			return scheduleClient.updateSchedule(schedule.toBuilder()
					.zones(schedule.zones().stream()
						.map(zone ->
							zone.toBuilder()
   					.rooms(zone.rooms().stream()
   						.map(room ->
   							room.toBuilder()
   								.thermSetpointTemperature(zonePresets.getTemp(schedule.effectiveId(), zone.id(), room.id()))
   								.build())
   							.toList())
   						.build())
   					.toList())
   				.build(),
   			getHomeId());
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			log.error("The URI is wrong: ".concat(e.getMessage()));
			throw new RuntimeException(e);
		} catch (ConnectionException e) {
			log.error("There are issues connection to the server: ".concat(e.getMessage()));
			throw new RuntimeException(e);
		}
	}
}
