import os
import threading
import time
import getpass

import requests
import stem
from stem.control import EventType, Controller

# Global variable to store the CSRF token
csrf_token = None

# Define the base API endpoint
BASE_API_ENDPOINT = "http://127.0.0.1:8080/api/data"


def main():
    # Define the directory containing Tor control files
    username = getpass.getuser()
    torrc_dir = f"/home/{username}/git/torConfigTool/torrc"
    control_ports = []

    # Create a thread for each relay and start monitoring
    threads = []

    while True:
        # Iterate through the files in the directory and collect control ports
        for filename in os.listdir(torrc_dir):
            control_port = read_control_port(os.path.join(torrc_dir, filename))
            if control_port and control_port not in control_ports:
                control_ports.append(control_port)
                thread = threading.Thread(target=monitor_traffic_and_flags, args=(control_port,))
                thread.daemon = True  # Set the thread as a daemon
                threads.append(thread)
                thread.start()

        # Wait for 5 seconds before scanning for new relays
        time.sleep(5)


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

                # Enable all events
                controller.set_conf('__LeaveStreamsUnattached', '1')  # Avoid attaching streams to circuits ourselves
                controller.set_conf('ControlPort', str(control_port))
                controller.set_conf('HashedControlPassword', '')
                controller.set_conf('CookieAuthentication', '1')

                # Enable log messages
                controller.set_conf('Log', ['NOTICE stdout'])

                # Add event listener for INFO events
                controller.add_event_listener(lambda event: _handle_event(controller, control_port, event),
                                              EventType.NOTICE)

                print(f"Monitoring relay on ControlPort {control_port}")

                while controller.is_alive():  # Check if the relay is still running
                    # Send the bandwidth data every second
                    relay_data_entry = _send_bandwidth_data(controller, control_port)
                    print(
                        f"Relay data entry for ControlPort {control_port}: {relay_data_entry}")  # Log the relay data entry
                    if relay_data_entry is not None:  # Only send data when there is new data to send
                        _send_relay_data_entry(control_port, relay_data_entry)
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


def _send_relay_data_entry(control_port, relay_data_entry):
    global csrf_token

    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # If CSRF token is not available, fetch it
    if not csrf_token:
        # Make a GET request to fetch the CSRF token
        response = requests.get(api_endpoint)

        # Extract the CSRF token from the cookie
        csrf_token = response.cookies.get('csrftoken')

    # Include the CSRF token in the headers
    headers = {
        'X-CSRFToken': csrf_token
    }

    # Send the relay data entry to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=relay_data_entry, headers=headers)

    if response.status_code == 200:
        print(f"Data sent for ControlPort {control_port}: {relay_data_entry}")
    else:
        print(f"Failed to send data for ControlPort {control_port}: {response.status_code} - {response.text}")


def relay_flags(controller):
    my_fingerprint = controller.get_info("fingerprint")  # Get the relay's fingerprint
    status = controller.get_network_status(default=my_fingerprint)  # Get the status entry for this relay
    flags = getattr(status, 'flags', [])  # Get the flags, return an empty list if not present
    return flags if isinstance(flags, list) else [flags]  # Convert to a list if not already


def _send_bandwidth_data(controller, control_port):
    # Get the initial total bytes read and written
    initial_download = int(controller.get_info("traffic/read"))
    initial_upload = int(controller.get_info("traffic/written"))

    # Wait for 1 second
    time.sleep(1)

    # Get the total bytes read and written after 1 second
    final_download = int(controller.get_info("traffic/read"))
    final_upload = int(controller.get_info("traffic/written"))

    # Calculate the per-second rates
    download_rate = final_download - initial_download
    upload_rate = final_upload - initial_upload

    # Get the relay flags
    flags = relay_flags(controller)

    # Print the data before sending it to the API
    print(
        f"Data to be sent for ControlPort {control_port}: Downloaded: {download_rate} bytes/s, Uploaded: {upload_rate} bytes/s, Flags: {flags}")

    # Get the uptime
    uptime = int(controller.get_info("uptime"))

    # Get the Tor version
    tor_version = controller.get_info("version")

    # Create a dictionary with the bandwidth data and an identifier
    data = {
        "download": download_rate,
        "upload": upload_rate,
        "flags": flags,  # Add the flags to the data
        "uptime": uptime,  # Add the uptime to the data
        "version": tor_version,  # Add the Tor version to the data
    }

    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # Send data to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=data)

    # Print the response object
    print(f"Response for ControlPort {control_port}: {response}")

    if response.status_code == 200:
        print(
            f"Data sent for ControlPort {control_port}: Downloaded: {download_rate} bytes/s, Uploaded: {upload_rate} bytes/s, Flags: {flags}")
    else:
        print(f"Failed to send data for ControlPort {control_port}: {response.status_code} - {response.text}")


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
        print(
            f"Data sent for ControlPort {control_port}: Downloaded: {download} bytes/s, Uploaded: {upload} bytes/s, Flags: {flags}")
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
        print(
            f"Data sent for ControlPort {control_port}: Downloaded: {download} bytes/s, Uploaded: {upload} bytes/s, Flags: {flags}")
    else:
        print(f"Failed to send data for ControlPort {control_port}: {response.status_code} - {response.text}")


def _handle_newconsensus_event(controller, control_port, event):
    flags = relay_flags(controller)

    # Create a dictionary with the flags data and an identifier
    data = {
        "flags": flags,  # Convert the list to a JSON array
    }

    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # Send data to the API endpoint for the corresponding relay
    response = requests.post(api_endpoint, json=data)

    if response.status_code == 200:
        print(f"Flags sent for ControlPort {control_port}: Flags: {flags}")
    else:
        print(f"Failed to send flags for ControlPort {control_port}: {response.status_code} - {response.text}")


if __name__ == '__main__':
    main()
