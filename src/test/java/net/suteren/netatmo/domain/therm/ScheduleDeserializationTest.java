package net.suteren.netatmo.domain.therm;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

class ScheduleDeserializationTest {

    private static final ObjectMapper MAPPER = JsonMapper.builder().findAndAddModules().build();

    @Test
    void scheduleGetIdShouldPreferIdAndFallbackToScheduleId() throws IOException {
        String json = """
            {
              "homes": [
                {
                  "id": "home-1",
                  "schedules": [
                    {
                      "id": "sched-by-id",
                      "name": "ById",
                      "zones": [],
                      "timetable": [],
                      "hg_temp": 7,
                      "away_temp": 12,
                      "default": false,
                      "type": "therm"
                    },
                    {
                      "schedule_id": "sched-by-schedule-id",
                      "name": "ByScheduleId",
                      "zones": [],
                      "timetable": [],
                      "hg_temp": 7,
                      "away_temp": 12,
                      "default": false,
                      "type": "therm"
                    }
                  ],
                  "rooms": [],
                  "modules": []
                }
              ],
              "user": null
            }
        """;

        HomesData homesData = MAPPER.readValue(json, HomesData.class);
        Home home = homesData.homes().get(0);
        List<Schedule> schedules = home.schedules();
        assertEquals(2, schedules.size(), "Očekáváme dva rozvrhy v testovacím JSONu");

        Schedule byId = schedules.get(0);
        assertEquals("sched-by-id", byId.effectiveId(), "effectiveId() má vracet hodnotu z pole 'id'");

        Schedule byScheduleId = schedules.get(1);
        assertEquals("sched-by-schedule-id", byScheduleId.effectiveId(), "effectiveId() má spadnout na hodnotu z pole 'schedule_id' pokud 'id' chybí");
    }
}
