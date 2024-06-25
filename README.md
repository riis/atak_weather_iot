# atak_weather_iot

An ATAK plugin to display data from IoT weather stations.

## Supported Weather Stations

This plugin was built specifically for the [RainmanWeather IoT Weather Station](https://www.rainmanweather.com/rainmanweather-iot-professional-lora-weather-station-wifi-wireless), however it should work for any weather station that allows you to upload data to a custom server in the Wunderground format.

## Architecture

The weather data must pass through several intermediary services before reaching the ATAK plugin. The sequence goes as follows:

1. **Weather Station**
  - Collects data from sensors.
  - Sends data via LoRa to the Weather Console.

2. **Weather Console (Screen)**
  - Receives data from the Weather Station.
  - Uses HTTP GET to send data to the ecowitt2mqtt Server.

3. **ecowitt2mqtt Server**
  - Receives data from the Weather Console.
  - Publishes data via MQTT to the Mosquitto MQTT Broker.

4. **Mosquitto (MQTT Broker)**
  - Receives data from the ecowitt2mqtt Server.
  - Forwards data via MQTT to the ATAK Plugin.

5. **ATAK Plugin**
  - Receives data from the Mosquitto MQTT Broker.
  - Displays data on screen

Here is a visual representation of the sequence:

```mermaid
graph TD
    A[Weather Station] -->|LoRa| B[Weather Console \(Screen\)]
    B -->|HTTP GET| C[ecowitt2mqtt Server]
    C -->|MQTT| D[Mosquitto \(MQTT Broker\)]
    D -->|MQTT| E[ATAK Plugin]
```

## Setting Up Your Weather Station

Follow your manufacturers instructions for setting up your weather station. Somewhere in the process, you'll have the opportunity to customize where the weather console uploads its data. For your device, you may need to play around with these values slightly. However, for the RainmanWeather station, you'll want to specify the following:

| **Field**              | **Value**                             |
|------------------------|---------------------------------------|
| Protocol Type Same As  | Wunderground                          |
| Server IP / Hostname*  | Your computer's ip address            |
| Path**                 | GET /data/report/?softwaretype=foo&   |
| Station ID***          | stationid                             |
| Station Key***         | stationkey                            |
| Port                   | 8080                                  |
| SSL                    | leave unchecked                       |
| Upload Interval        | 16 Seconds                            |

- `*` For the server ip, this will be the ip address of the computer running the ecowitt2mqtt server. If the weather console and the server are on the same network, you'll want to use your local ip, which may look something like `192.168.1.2`.
- `**` The path field is probably going to be the one that needs to be changed the most from weather station to weather station. For the RainmanWeather station, we need to explicitly set the HTTP `GET` method, as well as define the `softwaretype=foo` value to get ecowitt2mqtt to accept our data. For other weather stations, you can likely omit the `GET` as well as everything after `/report/?`.
- `***` You can put anything in for the Station ID and Station Key fields. These fields are meant to uniquely identify your weather station on wunderground.com, but aren't useful to us if we aren't using other weather stations. If you are using multiple weather stations, then choose unique values for these from station to station.
- Make sure none of the fields are blank. Some weather stations are very rigid in what they accept and don't have great error messages for when a field is set incorrectly.

## Required Services

As hinted at above, several services need to be brought online to get this workflow running. For the purposes of getting a development environment set up, we'll be running both of these services on the same machine, using Docker.

### Mosquitto

[Eclipse Mosquitto](https://mosquitto.org/) is a lightweight, open source MQTT broker that facilitates communication between the ecowitt2mqtt server and the ATAK plugin. To get it running, first create a `mosquitto.conf` file in the directory you intend on running the command in (a sample `mosquitto.conf` can be found in the root of this repository). For convenience, here is the contents of that sample file:

```conf
# Allow anonymous access
allow_anonymous true

# Listen on port 1883
listener 1883

# Set persistence to false
persistence false

# Allow connections over WebSockets (optional, if needed)
listener 9001
protocol websockets

# Log all events
log_type all
```

Note that this configuration file is not appropriate for a production environment since it allows anonymous connections and doesn't use any authentication.

Now, to bring Mosquitto online, we can run the following Docker command:

```
docker run -d --network host -v .\mosquitto.conf:/mosquitto/config/mosquitto.conf --name mosquitto eclipse-mosquitto
```

Note that you may need to change `.\mosquitto.conf` to `./mosquitto.conf` if you're a Linux, MacOS, or WSL user. Your Mosquitto service should now be running in the background.

## ecowitt2mqtt

[ecowitt2mqtt](https://github.com/bachya/ecowitt2mqtt) is an open source web server that receives HTTP requests from the weather console, parses the data, and sends it to the Mosquitto MQTT broker. Most home weather stations only support sending data via HTTP to a custom server, so it's not possible to directly connect the weather console to the MQTT broker without special hardware. By default, ecowitt2mqtt will run on port `8080`. You can refer to their [documentation](https://github.com/bachya/ecowitt2mqtt) if you'd like to customize the behaviour further.

Depending on your weather station, the steps to connect it to the ecowitt2mqtt server may differ slightly. Some weather stations handle their configuration slightly differently, so you may need to play around with your device if you're not using the RainmanWeather station. This is discussed more later on.

To get the server running, run the following docker command:

```
docker run -it --rm -e ECOWITT2MQTT_MQTT_BROKER=127.0.0.1 -e ECOWITT2MQTT_MQTT_TOPIC=weather/data -e ECOWITT2MQTT_INPUT_DATA_FORMAT=wunderground --network host bachya/ecowitt2mqtt:latest
```

Note that we pass in the variable `127.0.0.1` for the ip address of the MQTT broker (modify as needed in your case), and `weather/data` for the MQTT topic. We also pass in the `-it` flag to run this container interactively. You can pass in `-d` instead if you prefer it to run in the background.

## Testing The Workflow

To test that everything is working, you can run the following Docker command to subscribe to the `weather/data` MQTT topic (which is what the ATAK plugin subcribes to). 

```
docker run -it --rm --network host eclipse-mosquitto mosquitto_sub -h 127.0.0.1 -t weather/data
```

You should see new data coming in at regular intervals, depending on your weather station's configuration.

## A Note About Docker and `--network host`

We run these services using Docker with the host network adapter for convenience. In a production environment, it may be more appropriate to use something like Docker Compose, or installing the services on bare metal.

### Getting `--network host` Working

Using the host network adapter is considered a Beta Feature in Docker Desktop (Windows and MacOS users), and must be turned on before it can be used. If you're not seeing your weather data hit the ecowitt2mwtt server, this may be why. Unfortunately, Docker Desktop will not display an error when host networking is not enabled, so make sure it's enabled before proceeding.

To enable host networking, you'll first need to login on the Docker Desktop app, then navigate to `Settings -> Features in development`. If unchecked, check the box that says "Enable Host Networking" and hit the button to apply and restart.
