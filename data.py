import os
import socket
import threading
import time

import requests
import stem
from stem.control import EventType, Controller


def get_local_ip():
    hostname = socket.gethostname()
    local_ip = socket.gethostbyname(hostname)
    return local_ip


# Define the base API endpoint
BASE_API_ENDPOINT = f"https://{get_local_ip()}:8443/relay-data/relays"


def main():
    """
    Main function that starts monitoring threads for each relay.
    """

    # Get the current script's directory
    script_dir = os.path.dirname(os.path.realpath(__file__))
    # Define the directory containing Tor control files
    torrc_dir = os.path.join(script_dir, 'torrc')
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

        # Wait for 10 seconds before scanning for new relays
        time.sleep(10)


def read_control_port(file_path):
    """
    Read the control port from the Tor configuration file.

    :param file_path: Path to the Tor configuration file.
    :return: Control port number if found, None otherwise.
    """
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
    """
    Monitor traffic and flags for a given control port.

    :param control_port: Control port number to monitor.
    """
    while True:
        try:
            with Controller.from_port(port=control_port) as controller:
                controller.authenticate()

                # Enable all events
                controller.set_conf('__LeaveStreamsUnattached', '1')
                controller.set_conf('ControlPort', str(control_port))
                controller.set_conf('HashedControlPassword', '')
                controller.set_conf('CookieAuthentication', '1')

                # Enable log messages
                controller.set_conf('Log', ['NOTICE stdout'])

                # Add event listener for INFO events
                controller.add_event_listener(lambda event: _handle_event(control_port, event),
                                              EventType.NOTICE)

                while controller.is_alive():  # Check if the relay is still running
                    try:
                        # Send the bandwidth data every second
                        relay_data_entry = _send_bandwidth_data(controller, control_port)
                        if relay_data_entry is not None:  # Only send data when there is new data to send
                            _send_relay_data_entry(control_port, relay_data_entry)
                    except stem.SocketError as e:
                        print(f"Error connecting to ControlPort {control_port}: {e}")
                        time.sleep(10)  # Sleep for 10 seconds before retrying
                    except Exception as e:
                        print(f"An unexpected error occurred for ControlPort {control_port}: {e}")
                        time.sleep(10)  # Sleep for 10 seconds before retrying

        except Exception as e:
            print(f"An unexpected error occurred: {e}")
            time.sleep(10)  # Sleep for 10 seconds before retrying


def _send_relay_data_entry(control_port, relay_data_entry):
    """
    Send relay data entry to the API endpoint.

    :param control_port: Control port number.
    :param relay_data_entry: Relay data entry to send.
    """
    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}"

    # Send the relay data entry to the API endpoint for the corresponding relay
    requests.post(api_endpoint, json=relay_data_entry, verify=False)


def relay_flags(controller):
    """
    Get the relay flags.

    :param controller: Controller object.
    :return: List of relay flags.
    """
    my_fingerprint = controller.get_info("fingerprint")  # Get the relay's fingerprint
    status = controller.get_network_status(default=my_fingerprint)  # Get the status entry for this relay
    flags = getattr(status, 'flags', [])  # Get the flags, return an empty list if not present
    return flags if isinstance(flags, list) else [flags]  # Convert to a list if not already


def _send_bandwidth_data(controller, control_port):
    """
    Send bandwidth data to the API endpoint.

    :param controller: Controller object.
    :param control_port: Control port number.
    :return: Data dictionary.
    """
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
    requests.post(api_endpoint, json=data, verify=False)

    return data


def _handle_event(control_port, event):
    """
    Handle event and send it to the API endpoint.

    :param control_port: Control port number.
    :param event: Event to handle.
    """
    # Create a dictionary with the event data and an identifier
    data = {
        "event": str(event),
    }

    # Construct the complete API endpoint URL with the relayId
    api_endpoint = f"{BASE_API_ENDPOINT}/{control_port}/event"

    # Send data to the API endpoint for the corresponding relay
    requests.post(api_endpoint, json=data, verify=False)


if __name__ == '__main__':
    main()
