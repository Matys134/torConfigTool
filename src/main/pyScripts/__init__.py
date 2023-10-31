import os
import functools
import threading

import stem
from requests import RequestException
from stem.control import EventType, Controller
import requests
import time


# Define the base API endpoint
BASE_API_ENDPOINT = "http://192.168.2.117:8081/api/relay-data"

def main():
    # Define the directory containing Tor control files
    torrc_dir = "/home/matys/IdeaProjects/torConfigTool/torrc/guard"
    control_ports = []

    # Iterate through the files in the directory and collect control ports
    for filename in os.listdir(torrc_dir):
        control_port = read_control_port(os.path.join(torrc_dir, filename))
        if control_port:
            control_ports.append(control_port)

    # Create a thread for each relay and start monitoring
    threads = []
    for control_port in control_ports:
        thread = threading.Thread(target=monitor_traffic, args=(control_port,))
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

def monitor_traffic(control_port):
    while True:
        try:
            with Controller.from_port(port=control_port) as controller:
                controller.authenticate()
                bw_event_handler = functools.partial(_handle_bandwidth_event, controller, control_port)
                controller.add_event_listener(bw_event_handler, EventType.BW)

                print(f"Monitoring relay on ControlPort {control_port}")

                while controller.is_alive():  # Check if the relay is still running
                    time.sleep(1)  # Wait for 1 second to collect data

                print(f"Relay on ControlPort {control_port} has stopped.")
        except stem.SocketError as e:
            print(f"Error connecting to ControlPort {control_port}: {e}")

            # Sleep for a while before retrying
            time.sleep(5)  # Sleep for 60 seconds before retrying
        except Exception as e:
            print(f"An unexpected error occurred for ControlPort {control_port}: {e}")

            # Sleep for a while before retrying
            time.sleep(5)  # Sleep for 60 seconds before retrying



def _handle_bandwidth_event(controller, control_port, event):
    download = event.read
    upload = event.written

    
    # Create a dictionary with the bandwidth data and an identifier
    data = {
        "download": download,
        "upload": upload,
    }


    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # Send data to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=data)

    if response.status_code == 200:
        print(f"Data sent for ControlPort {control_port}: Downloaded: {download} bytes/s, Uploaded: {upload} bytes/s")
    else:
        print(f"Failed to send data for ControlPort {control_port}: {response.status_code} - {response.text}")

if __name__ == '__main__':
    main()
