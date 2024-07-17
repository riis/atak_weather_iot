try:
    import paho.mqtt.client as mqtt
except ImportError:
    print("The 'paho-mqtt' library is not installed. Please install it using 'pip install paho-mqtt'.")
    exit(1)

import argparse
import random
import json
import time
from typing import Dict, Union

def update_numeric_values(params: Dict[str, Union[str, float, int]]) -> Dict[str, Union[str, float, int]]:
    params['temp'] = float(params['temp']) + random.uniform(-0.5, 0.5)
    params['humidity'] = float(params['humidity']) + random.uniform(-1, 1)
    params['dewptf'] = float(params['dewptf']) + random.uniform(-0.5, 0.5)
    params['absbarom'] = float(params['absbarom']) + random.uniform(-0.02, 0.02)
    params['barom'] = float(params['barom']) + random.uniform(-0.02, 0.02)
    params['windspeed'] = float(params['windspeed']) + random.uniform(-0.1, 0.1)
    params['windgust'] = float(params['windgust']) + random.uniform(-0.1, 0.1)
    params['winddir'] = (float(params['winddir']) + random.uniform(-5, 5)) % 360
    params['windspdmph_avg2m'] = float(params['windspdmph_avg2m']) + random.uniform(-0.1, 0.1)
    params['winddir_avg2m'] = (float(params['winddir_avg2m']) + random.uniform(-5, 5)) % 360
    params['windgustmph_10m'] = float(params['windgustmph_10m']) + random.uniform(-0.1, 0.1)
    params['windgustdir_10m'] = (float(params['windgustdir_10m']) + random.uniform(-5, 5)) % 360
    params['rain'] = max(0, float(params['rain']) + random.uniform(-0.01, 0.01))
    params['dailyrain'] = max(0, float(params['dailyrain']) + random.uniform(-0.01, 0.01))
    params['weeklyrain'] = max(0, float(params['weeklyrain']) + random.uniform(-0.01, 0.01))
    params['monthlyrain'] = max(0, float(params['monthlyrain']) + random.uniform(-0.01, 0.01))
    params['solarradiation'] = float(params['solarradiation']) + random.uniform(-0.1, 0.1)
    params['UV'] = float(params['UV']) + random.uniform(-0.1, 0.1)
    params['indoortemp'] = float(params['indoortemp']) + random.uniform(-0.5, 0.5)
    params['indoorhumidity'] = float(params['indoorhumidity']) + random.uniform(-1, 1)
    params['dewpoint'] = float(params['dewpoint']) + random.uniform(-0.5, 0.5)
    params['feelslike'] = float(params['feelslike']) + random.uniform(-0.5, 0.5)
    params['frostpoint'] = float(params['frostpoint']) + random.uniform(-0.5, 0.5)
    params['heatindex'] = float(params['heatindex']) + random.uniform(-0.5, 0.5)
    params['simmerindex'] = float(params['simmerindex']) + random.uniform(-0.5, 0.5)
    params['solarradiation_perceived'] = float(params['solarradiation_perceived']) + random.uniform(-0.1, 0.1)
    return params

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Mocking Weather MQTT Publisher")

    # MQTT broker details
    parser.add_argument('--broker', default='localhost', help='MQTT broker address')
    parser.add_argument('--port', type=int, default=1883, help='MQTT broker port')
    parser.add_argument('--topic', default='weather/data', help='MQTT topic to publish to')
    parser.add_argument('--interval', type=int, default=5, help='Interval between messages in seconds')

    # Weather station values

    parser.add_argument('--ID', default='stationid', help='Station ID')
    parser.add_argument('--PASSWORD', default='stationkey', help='Password')
    parser.add_argument('--latitude', type=float, default=42.605589, help='latitude of the weather station')
    parser.add_argument('--longitude', type=float, default=-83.149930, help='longitude of the weather station')
    parser.add_argument('--softwaretype', default='foo', help='Software Type')
    parser.add_argument('--indoortemp', type=float, default=72.5, help='Indoor temperature in Fahrenheit')
    parser.add_argument('--indoorhumidity', type=float, default=65.0, help='Indoor humidity percentage')
    parser.add_argument('--temp', type=float, default=73.2, help='Temperature in Fahrenheit')
    parser.add_argument('--humidity', type=float, default=53.0, help='Humidity percentage')
    parser.add_argument('--dewptf', type=float, default=55.0, help='Dew point in Fahrenheit')
    parser.add_argument('--absbarom', type=float, default=29.29, help='Absolute barometric pressure in inches')
    parser.add_argument('--barom', type=float, default=29.69, help='Barometric pressure in inches')
    parser.add_argument('--windspeed', type=float, default=0.0, help='Wind speed in mph')
    parser.add_argument('--windgust', type=float, default=0.0, help='Wind gust in mph')
    parser.add_argument('--winddir', type=float, default=203.0, help='Wind direction')
    parser.add_argument('--windspdmph_avg2m', type=float, default=0.0, help='Average wind speed over 2 minutes in mph')
    parser.add_argument('--winddir_avg2m', type=float, default=203.0, help='Average wind direction over 2 minutes')
    parser.add_argument('--windgustmph_10m', type=float, default=0.0, help='Wind gust over 10 minutes in mph')
    parser.add_argument('--windgustdir_10m', type=float, default=203.0, help='Wind gust direction over 10 minutes')
    parser.add_argument('--rain', type=float, default=0.0, help='Rainfall in inches')
    parser.add_argument('--dailyrain', type=float, default=0.0, help='Daily rainfall in inches')
    parser.add_argument('--weeklyrain', type=float, default=0.0, help='Weekly rainfall in inches')
    parser.add_argument('--monthlyrain', type=float, default=0.0, help='Monthly rainfall in inches')
    parser.add_argument('--solarradiation', type=float, default=0.0, help='Solar radiation')
    parser.add_argument('--UV', type=float, default=0.0, help='UV index')
    parser.add_argument('--action', default='updateraw', help='Action')
    parser.add_argument('--realtime', type=float, default=1.0, help='Realtime flag')
    parser.add_argument('--rtfreq', type=float, default=5.0, help='Realtime frequency')
    parser.add_argument('--beaufortscale', type=int, default=0, help='Beaufort scale')
    parser.add_argument('--dewpoint', type=float, default=55.05268957359718, help='Dew point')
    parser.add_argument('--feelslike', type=float, default=73.2, help='Feels like temperature')
    parser.add_argument('--frostpoint', type=float, default=49.43227622173363, help='Frost point')
    parser.add_argument('--frostrisk', default='No risk', help='Frost risk')
    parser.add_argument('--heatindex', type=float, default=72.711, help='Heat index')
    parser.add_argument('--humidex', type=int, default=26, help='Humidex')
    parser.add_argument('--humidex_perception', default='Little to no discomfort', help='Humidex perception')
    parser.add_argument('--humidityabs', type=float, default=0.0006757702890680047, help='Absolute humidity')
    parser.add_argument('--relative_strain_index', default='null', help='Relative strain index')
    parser.add_argument('--relative_strain_index_perception', default='null', help='Relative strain index perception')
    parser.add_argument('--simmerindex', type=float, default=80.32618400000003, help='Simmer index')
    parser.add_argument('--simmerzone', default='Comfortable', help='Simmer zone')
    parser.add_argument('--solarradiation_perceived', type=float, default=0.0, help='Perceived solar radiation')
    parser.add_argument('--thermalperception', default='Comfortable', help='Thermal perception')
    parser.add_argument('--windchill', default='null', help='Wind chill')
    parser.add_argument('--winddir_name', default='SSW', help='Wind direction name')

    args = parser.parse_args()
    
    params = vars(args)
    broker = params.pop("broker")
    port = params.pop("port")
    topic = params.pop("topic")
    interval = int(params.pop("interval"))

    client = mqtt.Client()
    client.connect(broker, port, 60)
    client.loop_start()

    print("=====================================")
    print("====== Mocking Weather Publisher ====")
    print("=====================================")
    print()
    
    try:
        while True:
            params = update_numeric_values(params)
            payload = json.dumps(params)
            print(f"\n\nPublishing to {topic}:\n{payload}")
            client.publish(topic, payload)
            time.sleep(interval)
    except KeyboardInterrupt:
        print("Exiting...")
    finally:
        client.loop_stop()
        client.disconnect()
