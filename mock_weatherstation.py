import http.client
import urllib.parse
from typing import Dict
import time
import argparse
import random

class Response:
    def __init__(self, status_code: int, reason: str, headers: Dict[str, str], content: bytes, text: str) -> None:
        self.status_code = status_code
        self.reason = reason
        self.headers = headers
        self.content = content
        self.text = text

    def __str__(self) -> str:
        return f'<Response {self.status_code}>'

def get_request(url: str) -> Response:
    parsed_url = urllib.parse.urlparse(url)
    port = parsed_url.port or (443 if parsed_url.scheme == 'https' else 80)

    conn = http.client.HTTPSConnection(parsed_url.hostname, port) if parsed_url.scheme == 'https' else http.client.HTTPConnection(parsed_url.hostname, port)
    path = parsed_url.path + ('?' + parsed_url.query if parsed_url.query else '')

    conn.request('GET', path)
    response = conn.getresponse()
    
    status_code = response.status
    reason = response.reason
    headers = dict(response.getheaders())
    body = response.read()
    text = body.decode('utf-8')
    
    conn.close()
    
    return Response(status_code, reason, headers, body, text)

def update_numeric_values(params: Dict[str, str]) -> Dict[str, str]:
    params['temp'] = str(float(params['temp']) + random.uniform(-0.5, 0.5))
    params['humidity'] = str(float(params['humidity']) + random.uniform(-1, 1))
    params['dewptf'] = str(float(params['dewptf']) + random.uniform(-0.5, 0.5))
    params['absbarom'] = str(float(params['absbarom']) + random.uniform(-0.02, 0.02))
    params['barom'] = str(float(params['barom']) + random.uniform(-0.02, 0.02))
    params['windspeed'] = str(float(params['windspeed']) + random.uniform(-0.1, 0.1))
    params['windgust'] = str(float(params['windgust']) + random.uniform(-0.1, 0.1))
    params['winddir'] = str((float(params['winddir']) + random.uniform(-5, 5)) % 360)
    params['windspdmph_avg2m'] = str(float(params['windspdmph_avg2m']) + random.uniform(-0.1, 0.1))
    params['winddir_avg2m'] = str((float(params['winddir_avg2m']) + random.uniform(-5, 5)) % 360)
    params['windgustmph_10m'] = str(float(params['windgustmph_10m']) + random.uniform(-0.1, 0.1))
    params['windgustdir_10m'] = str((float(params['windgustdir_10m']) + random.uniform(-5, 5)) % 360)
    params['rain'] = str(max(0, float(params['rain']) + random.uniform(-0.01, 0.01)))
    params['dailyrain'] = str(max(0, float(params['dailyrain']) + random.uniform(-0.01, 0.01)))
    params['weeklyrain'] = str(max(0, float(params['weeklyrain']) + random.uniform(-0.01, 0.01)))
    params['monthlyrain'] = str(max(0, float(params['monthlyrain']) + random.uniform(-0.01, 0.01)))
    params['solarradiation'] = str(float(params['solarradiation']) + random.uniform(-0.1, 0.1))
    params['UV'] = str(float(params['UV']) + random.uniform(-0.1, 0.1))
    params['indoortemp'] = str(float(params['indoortemp']) + random.uniform(-0.5, 0.5))
    params['indoorhumidity'] = str(float(params['indoorhumidity']) + random.uniform(-1, 1))
    params['dewpoint'] = str(float(params['dewpoint']) + random.uniform(-0.5, 0.5))
    params['feelslike'] = str(float(params['feelslike']) + random.uniform(-0.5, 0.5))
    params['frostpoint'] = str(float(params['frostpoint']) + random.uniform(-0.5, 0.5))
    params['heatindex'] = str(float(params['heatindex']) + random.uniform(-0.5, 0.5))
    params['simmerindex'] = str(float(params['simmerindex']) + random.uniform(-0.5, 0.5))
    params['solarradiation_perceived'] = str(float(params['solarradiation_perceived']) + random.uniform(-0.1, 0.1))
    return params

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Mocking Weather Request CLI")

    # Config values
    parser.add_argument('--base-url', default='http://127.0.0.1:8080/data/report/?', help='Base URL for the request')
    parser.add_argument('--interval', default=5, help='Interval between requests in seconds')

    # Weather station values
    parser.add_argument('--ID', default='stationid', help='Station ID')
    parser.add_argument('--PASSWORD', default='stationkey', help='Password')
    parser.add_argument('--softwaretype', default='foo', help='Software Type')
    parser.add_argument('--indoortemp', default='72.5', help='Indoor temperature in Fahrenheit')
    parser.add_argument('--indoorhumidity', default='65.0', help='Indoor humidity percentage')
    parser.add_argument('--temp', default='73.2', help='Temperature in Fahrenheit')
    parser.add_argument('--humidity', default='53.0', help='Humidity percentage')
    parser.add_argument('--dewptf', default='55.0', help='Dew point in Fahrenheit')
    parser.add_argument('--absbarom', default='29.29', help='Absolute barometric pressure in inches')
    parser.add_argument('--barom', default='29.69', help='Barometric pressure in inches')
    parser.add_argument('--windspeed', default='0.0', help='Wind speed in mph')
    parser.add_argument('--windgust', default='0.0', help='Wind gust in mph')
    parser.add_argument('--winddir', default='203.0', help='Wind direction')
    parser.add_argument('--windspdmph_avg2m', default='0.0', help='Average wind speed over 2 minutes in mph')
    parser.add_argument('--winddir_avg2m', default='203.0', help='Average wind direction over 2 minutes')
    parser.add_argument('--windgustmph_10m', default='0.0', help='Wind gust over 10 minutes in mph')
    parser.add_argument('--windgustdir_10m', default='203.0', help='Wind gust direction over 10 minutes')
    parser.add_argument('--rain', default='0.0', help='Rainfall in inches')
    parser.add_argument('--dailyrain', default='0.0', help='Daily rainfall in inches')
    parser.add_argument('--weeklyrain', default='0.0', help='Weekly rainfall in inches')
    parser.add_argument('--monthlyrain', default='0.0', help='Monthly rainfall in inches')
    parser.add_argument('--solarradiation', default='0.0', help='Solar radiation')
    parser.add_argument('--UV', default='0.0', help='UV index')
    parser.add_argument('--action', default='updateraw', help='Action')
    parser.add_argument('--realtime', default='1.0', help='Realtime flag')
    parser.add_argument('--rtfreq', default='5.0', help='Realtime frequency')
    parser.add_argument('--beaufortscale', default='0', help='Beaufort scale')
    parser.add_argument('--dewpoint', default='55.05268957359718', help='Dew point')
    parser.add_argument('--feelslike', default='73.2', help='Feels like temperature')
    parser.add_argument('--frostpoint', default='49.43227622173363', help='Frost point')
    parser.add_argument('--frostrisk', default='No risk', help='Frost risk')
    parser.add_argument('--heatindex', default='72.711', help='Heat index')
    parser.add_argument('--humidex', default='26', help='Humidex')
    parser.add_argument('--humidex_perception', default='Little to no discomfort', help='Humidex perception')
    parser.add_argument('--humidityabs', default='0.0006757702890680047', help='Absolute humidity')
    parser.add_argument('--relative_strain_index', default='null', help='Relative strain index')
    parser.add_argument('--relative_strain_index_perception', default='null', help='Relative strain index perception')
    parser.add_argument('--simmerindex', default='80.32618400000003', help='Simmer index')
    parser.add_argument('--simmerzone', default='Comfortable', help='Simmer zone')
    parser.add_argument('--solarradiation_perceived', default='0.0', help='Perceived solar radiation')
    parser.add_argument('--thermalperception', default='Comfortable', help='Thermal perception')
    parser.add_argument('--windchill', default='null', help='Wind chill')
    parser.add_argument('--winddir_name', default='SSW', help='Wind direction name')

    args = parser.parse_args()
    
    params = vars(args)
    base_url = params.pop("base_url")
    interval = int(params.pop("interval"))

    print("=====================================")
    print("====== Mocking Weather Request ======")
    print("=====================================")
    print()
    
    while True:
        params = update_numeric_values(params)
        query_string = urllib.parse.urlencode(params)
        url = f"{base_url}{query_string}"
        
        print(f"Sending request to {url}")
        try:
            response = get_request(url)
            print(response)
        except Exception as e:
            print(f"Failed to send request: {e}")
        time.sleep(interval)
