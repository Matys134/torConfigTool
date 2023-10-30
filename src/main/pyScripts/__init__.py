import os
import functools
import threading
from stem.control import EventType, Controller
import requests
import time

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
    with Controller.from_port(port=control_port) as controller:
        controller.authenticate()
        bw_event_handler = functools.partial(_handle_bandwidth_event, controller)
        controller.add_event_listener(bw_event_handler, EventType.BW)

        print(f"Monitoring relay on ControlPort {control_port}")

        while True:
            time.sleep(1)  # Wait for 1 second to collect data
            # In this loop, the script continuously prints traffic per second

def _handle_bandwidth_event(controller, event):
    download = event.read
    upload = event.written

    print(f"Downloaded: {event.read} bytes/s, Uploaded: {event.written} bytes/s")

if __name__ == '__main__':
    main()
