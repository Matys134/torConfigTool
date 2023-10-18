#!/bin/bash

# Get command-line arguments
relayNickname="$1"
relayBandwidth="$2"
relayPort="$3"
relayContact="$4"

# Get the directory of the script
script_dir=$(dirname "$0")

# Generate a unique torrc file for each relay
torrc_file="$script_dir/../torrc/local-torrc-$relayNickname"

# Check if the torrc file exists
if [ -f "$torrc_file" ]; then
    # Stop Tor
    systemctl stop tor  # Assuming you're using systemd; adjust for your system

    if [ $? -eq 0 ]; then
        echo "Tor has been stopped."
    else
        echo "Failed to stop Tor. Make sure you have the necessary permissions."
        exit 1
    fi

    # Clear the torrc file
    > "$torrc_file"

    # Add the specified lines to the torrc file
    echo "Nickname $relayNickname" >> "$torrc_file"

    if [ -n "$relayBandwidth" ]; then
        echo "BandwidthRate ${relayBandwidth} KBytes" >> "$torrc_file"
    fi

    echo "ORPort $relayPort" >> "$torrc_file"
    echo "ContactInfo $relayContact" >> "$torrc_file"

    echo "torrc file has been cleared and updated."

    # Start Tor with the custom torrc file
    tor -f "$torrc_file"

    if [ $? -eq 0 ]; then
        echo "Tor has been started with the custom torrc file."
    else
        echo "Failed to start Tor. Make sure you have the necessary permissions."
    fi
else
    echo "torrc file not found at: $torrc_file"
fi
