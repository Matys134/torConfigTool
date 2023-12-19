import json
import os
import functools
import threading

import stem
from stem.control import EventType, Controller
import requests
import time

# Define the base API endpoint
BASE_API_ENDPOINT = "http://127.0.0.1:8081/api/relay-data"

def main():
    # Define the directory containing Tor control files
    torrc_dir = "/home/matys/git/torConfigTool/torrc"
    control_ports = []

    # Iterate through the files in the directory and collect control ports
    for filename in os.listdir(torrc_dir):
        control_port = read_control_port(os.path.join(torrc_dir, filename))
        if control_port:
            control_ports.append(control_port)

    # Create a thread for each relay and start monitoring
    threads = []
    for control_port in control_ports:
        thread = threading.Thread(target=monitor_traffic_and_flags, args=(control_port,))
        thread.setDaemon(True)  # Set the thread as a daemon
        threads.append(thread)
        thread.start()

    # Wait for user input to stop monitoring
    input("Press Enter to stop monitoring")

def read_control_port(file_path):
    # Read the control port from the Tor configuration file
    try:
        with open(file_path, 'r') as file:
            for line in file:
                if line.startswith("ControlPort"):
                    parts = line.strip().split()
                    if len(parts) == 2:
                        return int(parts[1])
    except FileNotFoundError:
        print(f"File not found: {file_path}")
    except Exception as e:
        print(f"Error reading control port from {file_path}: {str(e)}")
    return None

def monitor_traffic_and_flags(control_port):
    while True:
        try:
            with Controller.from_port(port=control_port) as controller:
                controller.authenticate()
                bw_event_handler = functools.partial(_handle_bandwidth_event, controller, control_port)
                controller.add_event_listener(bw_event_handler, EventType.BW)

                controller.add_event_listener(lambda event: _handle_event(controller, control_port, event), EventType)

                print(f"Monitoring relay on ControlPort {control_port}")

                while controller.is_alive():  # Check if the relay is still running
                    # Get the relay's status entry
                    flags = relay_flags(controller)
                    print(f"Relay flags: {flags}")
                    time.sleep(1)  # Wait for 1 second to collect data

                print(f"Relay on ControlPort {control_port} has stopped.")
        except stem.SocketError as e:
            print(f"Error connecting to ControlPort {control_port}: {e}")

            # Sleep for a while before retrying
            time.sleep(5)  # Sleep for 5 seconds before retrying
        except Exception as e:
            print(f"An unexpected error occurred for ControlPort {control_port}: {e}")

            # Sleep for a while before retrying
            time.sleep(5)  # Sleep for 5 seconds before retrying

def relay_flags(controller):
    my_fingerprint = controller.get_info("fingerprint")  # Get the relay's fingerprint
    status = controller.get_network_status(default=my_fingerprint)  # Get the status entry for this relay
    flags = getattr(status, 'flags', [])  # Get the flags, return an empty list if not present
    return flags if isinstance(flags, list) else [flags]  # Convert to a list if not already


def _handle_bandwidth_event(controller, control_port, event):
    download = event.read
    upload = event.written
    flags = relay_flags(controller)

    # Create a dictionary with the bandwidth data and an identifier
    data = {
        "download": download,
        "upload": upload,
        "flags": flags,  # Convert the list to a JSON array
    }

    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # Send data to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=data)

    if response.status_code == 200:
        print(f"Data sent for ControlPort {control_port}: Downloaded: {download} bytes/s, Uploaded: {upload} bytes/s, Flags: {flags}")
    else:
        print(f"Failed to send data for ControlPort {control_port}: {response.status_code} - {response.text}")

def _handle_event(controller, control_port, event):
    # Create a dictionary with the event data and an identifier
    data = {
        "event": str(event),
    }

    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}/event"

    # Send data to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=data)

    if response.status_code == 200:
        print(f"Event sent for ControlPort {control_port}: {event}")
    else:
        print(f"Failed to send event for ControlPort {control_port}: {response.status_code} - {response.text}")


    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # Send data to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=data)

    if response.status_code == 200:
        print(f"Data sent for ControlPort {control_port}: Downloaded: {download} bytes/s, Uploaded: {upload} bytes/s, Flags: {flags}")
    else:
        print(f"Failed to send data for ControlPort {control_port}: {response.status_code} - {response.text}")


if __name__ == '__main__':
    main()