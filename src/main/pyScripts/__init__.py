import time
from stem.control import Controller

def monitor_traffic():
    with Controller.from_port(port=9051) as controller:
        controller.authenticate()  # Use your authentication method here

        while True:
            network_status = controller.get_network_status()
            traffic_read = network_status.read
            traffic_written = network_status.written

            print(f"Traffic Read: {traffic_read} bytes")
            print(f"Traffic Written: {traffic_written} bytes")

            time.sleep(5)  # Adjust the update interval as needed

if __name__ == '__main__':
    monitor_traffic()
