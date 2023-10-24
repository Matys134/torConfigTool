import functools
from stem.control import EventType, Controller
import requests

def main():
    with Controller.from_port(port=9051) as controller:
        controller.authenticate()
        try:
            bw_event_handler = functools.partial(_handle_bandwidth_event, controller)
            controller.add_event_listener(bw_event_handler, EventType.BW)
            input("Press Enter to exit")
        except KeyboardInterrupt:
            pass  # the user hit ctrl+c

def _handle_bandwidth_event(controller, event):
    download = event.read
    upload = event.written

    # Send the data to your Java web application's API
    data = {'download': download, 'upload': upload}
    api_url = 'http://your-java-app-hostname/api/relay-data'
    response = requests.post(api_url, json=data)

    print(f"Downloaded: {event.read} bytes/s, Uploaded: {event.written} bytes/s")

    if response.status_code == 200:
        print('Data sent successfully')
    else:
        print('Failed to send data')

if __name__ == '__main__':
    main()
