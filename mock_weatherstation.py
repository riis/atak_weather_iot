import http.client
import urllib.parse
from typing import Dict
import time

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
    # Parse the URL into components
    parsed_url = urllib.parse.urlparse(url)
    
    # Set the default port if not specified
    port = parsed_url.port
    if port is None:
        port = 443 if parsed_url.scheme == 'https' else 80
    
    # Determine the connection type
    if parsed_url.scheme == 'https':
        conn = http.client.HTTPSConnection(parsed_url.hostname, port)
    else:
        conn = http.client.HTTPConnection(parsed_url.hostname, port)
    
    # Construct the path
    path = parsed_url.path
    if parsed_url.query:
        path += '?' + parsed_url.query

    # Send the GET request
    conn.request('GET', path)
    
    # Get the response
    response = conn.getresponse()
    status_code = response.status
    reason = response.reason
    headers = dict(response.getheaders())
    body = response.read()
    text = body.decode('utf-8')  # Decode the body using UTF-8
    
    # Close the connection
    conn.close()
    
    # Return an instance of Response
    return Response(status_code, reason, headers, body, text)

if __name__ == '__main__':
    # TODO: get ip and port from the command line
    # TODO: better organize mock values
    url = "http://127.0.0.1:8080/data/report/?softwaretype=foo&ID=STATION_ID&PASSWORD=PASSWORD&dateutc=now&tempf=70.5&humidity=40&dewptf=42.8&winddir=160&windspeedmph=1.5&windgustmph=3.0&rainin=0.00&dailyrainin=0.00&baromin=29.92&indoortempf=68.0&indoorhumidity=45&action=updateraw"
    print("=====================================")
    print("====== Mocking Weather Request ======")
    print("=====================================")
    print()
    while True:
        response = get_request(url)
        time.sleep(5)
        print(response)