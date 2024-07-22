import platform
import subprocess
import signal
import sys

weather_stations = [
    {
        "ID": "StationAlpha",
        "latitude": 42.605589,
        "longitude": -83.149930
    },
    {
        "ID": "StationBeta",
        "latitude": 42.609589,
        "longitude": -83.154930
    },
    {
        "ID": "StationGamma",
        "latitude": 42.613589,
        "longitude": -83.159930
    },
    {
        "ID": "StationDelta",
        "latitude": 42.617589,
        "longitude": -83.164930
    },
    {
        "ID": "StationEpsilon",
        "latitude": 42.621589,
        "longitude": -83.169930
    }
]

# Determine the python command based on the operating system
python_cmd = "py" if platform.system() == "Windows" else "python3"

base_command = f"{python_cmd} mock_mqtt_publish.py --broker localhost --port 1883 --topic weather/data --interval 5"

processes = []

def signal_handler(sig, frame):
    print('Terminating all child processes...')
    for process in processes:
        process.terminate()
    for process in processes:
        process.wait()
    sys.exit(0)

# Register signal handlers
signal.signal(signal.SIGINT, signal_handler)
signal.signal(signal.SIGTERM, signal_handler)

for station in weather_stations:
    station_id = station['ID']
    latitude = station['latitude']
    longitude = station['longitude']
    command = f"{base_command} --ID {station_id} --latitude {latitude} --longitude {longitude}"
    print(f"Executing: {command}")
    process = subprocess.Popen(command, shell=True)
    processes.append(process)

# Wait for all processes to complete
for process in processes:
    process.wait()
