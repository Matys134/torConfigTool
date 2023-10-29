import functools
from stem.control import Controller
import requests

# Function to create a new listener for a relay
def create_relay_listener(port):
    def relay_listener(controller, event):
        download = event.read
        upload = event.written

        data = {'download': download, 'upload': upload}
        api_url = f'http://localhost:{port}/api/relay-data'
        response = requests.post(api_url, json=data)

        print(f"Downloaded: {event.read} bytes/s, Uploaded: {event.written} bytes/s")

        if response.status_code == 200:
            print('Data sent successfully')
        else:
            print('Failed to send data')

    return relay_listener

def main():
    # Read control ports from torrc/guard files
    control_ports = []
    with open('torrc/guard', 'r') as file:
        for line in file:
            if line.startswith("ControlPort"):
                parts = line.split()
                if len(parts) == 2:
                    control_ports.append(int(parts[1]))

    # Create listeners for each relay
    listeners = []
    for port in control_ports:
        with Controller.from_port(port=port) as controller:
            controller.authenticate()
            relay_listener = create_relay_listener(port)
            controller.add_event_listener(relay_listener, EventType.BW)
            listeners.append(controller)

    try:
        input("Press Enter to exit")
    except KeyboardInterrupt:
        pass  # the user hit ctrl+c

if __name__ == '__main__':
    main()
