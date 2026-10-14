![gradle](https://github.com/konikvranik/netatmo4j/actions/workflows/gradle.yml/badge.svg)![codeql-analysis](https://github.com/konikvranik/netatmo4j/actions/workflows/codeql-analysis.yml/badge.svg)![release](https://github.com/konikvranik/netatmo4j/actions/workflows/gradle-publish.yml/badge.svg)[![codecov](https://codecov.io/gh/konikvranik/netatmo4j/graph/badge.svg?token=KXDRNR6D2N)](https://codecov.io/gh/konikvranik/netatmo4j)\
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=konikvranik_netatmo4j&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=konikvranik_netatmo4j)[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=konikvranik_netatmo4j&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=konikvranik_netatmo4j)[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=konikvranik_netatmo4j&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=konikvranik_netatmo4j)[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=konikvranik_netatmo4j&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=konikvranik_netatmo4j)[![Technical Debt](https://sonarcloud.io/api/project_badges/measure?project=konikvranik_netatmo4j&metric=sqale_index)](https://sonarcloud.io/summary/new_code?id=konikvranik_netatmo4j)

> [!WARNING]
> **This project is no longer maintained.**
>
> Development and maintenance of netatmo4j ended on 2026-10-14. The repository is now archived and read-only.
>
> - No new features, bug fixes, or security updates will be provided.
> - Existing releases remain available on [Maven Central](https://central.sonatype.com/artifact/net.suteren.netatmo/netatmo4j) and can still be used.
> - Issues and pull requests are disabled.
>
> If you want to continue development, feel free to fork this repository.


# netatmo4j

Library for calling the Netatmo API and providing data in Java classes.

Data models are in the package `net.suteren.netatmo.domain`.

Classes for calling Netatmo server are in `net.suteren.netatmo.therm`.
Currently only thermostat and valves are supported.

For authentication there is `net.suteren.netatmo.auth.AuthClient`.
It is required by API classes.
If you try to connect to the API, it opens Netatmo web page in the web browser,
let you log in and approve the application, and then it stores auth token and refresh token into the `~/.netatmoauth.json` file,
so you don't have to log in every request.

See [Netatmo Authentication](https://dev.netatmo.com/apidocumentation/oauth) for details.

There is also a simple commandline client and timeline visualizer. See the sections below.

## Basic usage

[neatmo4j artifact](https://central.sonatype.com/artifact/net.suteren.netatmo/netatmo4j) is hosted on [Maven Central](https://central.sonatype.com/)

```java

AuthClient authClient =	new AuthClient(clientId, clientSecret, List.of("read_thermostat", "write_thermostat"), "Netatmo tool", authconfig);

HomeClient homeClient = new HomeClient(authClient).getHomesData();
HomesData homesData = homeClient.getHomesData();

ScheduleClient scheduleClient = new ScheduleClient(authClient);
Schedule schedule = createSchedule();
scheduleClient.updateSchedule(homeId, schedule);
scheduleClient.setSchedule(homeId, scheduleId);

```

## Commandline client

This is a commandline tool for querying netatmo server and updating the schedule by configuration.
See `net.suteren.netatmo.cli.NetatmoCli`.

You can build fat-jar artifact which creates executable jar of this client:

```shell
gradle fatJar
```

Then you can execute this command:

```shell
./build/libs/netatmo4j-${VERSION}-fatjar.jar -h
```

## Timeline visualiser

If you want to visualize timeline of the schedule,
you can use groovy script [`scheduleRendered.groovy`](bin/scheduleRendered.groovy).
Pass Schedule JSON as a parameter.
Then it will render either SVG or PlantUml source, depending on presence of the `-i` flag.

Example of the result: ![timeline example](example_timeline.svg)