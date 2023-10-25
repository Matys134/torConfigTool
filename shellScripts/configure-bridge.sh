#!/bin/bash

# Get command-line arguments
bridgePort="$1"
bridgeTransportListenAddr="$2"
bridgeContact="$3"
bridgeNickname="$4"

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the torrc file relative to the script directory
torrc_file="$script_dir/../torrc/local-torrc-bridge"

# Check if the torrc file exists
if [ -f "$torrc_file" ]; then
    # Clear the torrc file
    > "$torrc_file"

    # Add the specified lines to the torrc file
    echo 'BridgeRelay 1' >> "$torrc_file"
    echo "ORPort $bridgePort" >> "$torrc_file"
    echo 'ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy' >> "$torrc_file"
    echo "ServerTransportListenAddr obfs4 0.0.0.0:$bridgeTransportListenAddr" >> "$torrc_file"
    echo 'ExtORPort auto' >> "$torrc_file"
    echo "ContactInfo $bridgeContact" >> "$torrc_file"
    echo "Nickname $bridgeNickname" >> "$torrc_file"

    echo "torrc bridge configuration has been updated."
else
    echo "torrc bridge configuration file not found at: $torrc_file"
fi