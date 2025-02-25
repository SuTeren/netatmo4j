package net.suteren.netatmo.domain.therm;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import net.suteren.netatmo.PairDeserializer;
import net.suteren.netatmo.PairSerializer;

/**
 * Representation of the Home entity from the NEtatmo API server.
 *
 * @param id homeId.
 * @param name of the dome.
 * @param altitude of the home.
 * @param coordinates of the home.
 * @param country the home balongs to.
 * @param timezone of the home.
 * @param rooms of the home.
 * @param modules in the home.
 * @param thermSetPointDefaultDuration default itme of the temperature override.
 * @param thermSetpointDefaultDuration default itme of the temperature override.
 * @param thermBoostDefaultDuration default itme of the temperature boost.
 * @param temperatureControlMode The temperature control mode.
 * @param thermMode The therm mode.
 * @param schedules The schedules.
 */
public record Home(
	String id,
	String name,
	Integer altitude,
	@JsonDeserialize(using = PairDeserializer.class)
	@JsonSerialize(using = PairSerializer.class)
	Pair<Double, Double> coordinates,
	String country,
	String timezone,
	List<Room> rooms,
	List<Module> modules,
	@JsonProperty("therm_set_point_default_duration") Integer thermSetPointDefaultDuration,
	@JsonProperty("therm_setpoint_default_duration") Integer thermSetpointDefaultDuration,
	@JsonProperty("therm_boost_default_duration") Integer thermBoostDefaultDuration,
	@JsonProperty("temperature_control_mode") String temperatureControlMode,
	@JsonProperty("therm_mode") String thermMode,
	List<Schedule> schedules
) {

	/**
	 * Get a single schedule if it exists.
	 *
	 * @param scheduleId to be retrieved.
	 * @return an {@link Optional} of s single {@link Schedule} if it exists.
	 */
	@JsonIgnore public Optional<Schedule> getScheduleById(String scheduleId) {
		return schedules.stream()
			.filter(s -> Objects.equals(s.id(), scheduleId))
			.findAny();
	}

	/**
	 * Get a list of all schedules with the same name as in the parameter.
	 *
	 * @param scheduleName the name of the schedule to be retrieved.
	 * @return a list of all {@link Schedule}s matching the <code>scheduleName</code>.
	 */
	@JsonIgnore public List<Schedule> getSchedulesByName(String scheduleName) {
		return schedules.stream()
			.filter(s -> Objects.equals(s.name(), scheduleName))
			.toList();
	}

	/**
	 * Get a single room if it exists.
	 *
	 * @param roomId to be retrieved.
	 * @return an {@link Optional} of a single {@link Room} if it exists.
	 */
	@JsonIgnore public Optional<Room> getRoomById(String roomId) {
		return rooms.stream()
			.filter(s -> Objects.equals(s.id(), roomId))
			.findAny();
	}

	/**
	 * Get a list of all {@link Room}s with the same name as in the parameter.
	 *
	 * @param roomName the name of the room to be retrieved.
	 * @return a list of all {@link Room}s matching the <code>roomName</code>.
	 */
	@JsonIgnore public List<Room> getRoomByName(String roomName) {
		return rooms.stream()
			.filter(s -> Objects.equals(s.name(), roomName))
			.toList();
	}

	/**
	 * Get a single module if it exists.
	 *
	 * @param moduleId to be retrieved.
	 * @return an {@link Optional} of a single {@link Module} if it exists.
	 */
	@JsonIgnore public Optional<Module> getModuleById(String moduleId) {
		return modules.stream()
			.filter(s -> Objects.equals(s.id(), moduleId))
			.findAny();
	}

}
