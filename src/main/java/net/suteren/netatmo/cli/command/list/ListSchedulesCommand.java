package net.suteren.netatmo.cli.command.list;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;

import net.suteren.netatmo.client.ConnectionException;
import net.suteren.netatmo.domain.therm.Home;
import net.suteren.netatmo.domain.therm.Schedule;
import picocli.CommandLine;

@CommandLine.Command(name = "schedules")
class ListSchedulesCommand extends AbstractListCommand<Schedule> {
	@Override protected Collection<Schedule> getObjects() throws IOException, URISyntaxException, InterruptedException, ConnectionException {
		return getHomeClient().getHomesData().getHomeById(getHomeId())
			.map(Home::schedules)
			.stream()
			.flatMap(Collection::stream)
			.toList();
	}

	@Override public String format(Schedule schedule) {
		return String.format("%25s : %s", schedule.effectiveId(), schedule.name());
	}
}
