#!/bin/bash

# Get command-line arguments
relayNickname="$1"
relayBandwidth="$2"
relayPort="$3"
relayContact="$4"

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the torrc file relative to the script directory
torrc_file="$script_dir/../torrc/guard/local-torrc-$relayNickname"

# Check if the torrc file exists or create it if it doesn't
if [ -e "$torrc_file" ]; then
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
    # Create the torrc file
    echo "Creating torrc file at: $torrc_file"

    # Create the directory if it doesn't exist
    mkdir -p "$script_dir/../torrc"

    # Create the torrc file
    touch "$torrc_file"

    # Add the specified lines to the torrc file
    echo "Nickname $relayNickname" >> "$torrc_file"

    if [ -n "$relayBandwidth" ]; then
        echo "BandwidthRate ${relayBandwidth} KBytes" >> "$torrc_file"
    fi

    echo "ORPort $relayPort" >> "$torrc_file"
    echo "ContactInfo $relayContact" >> "$torrc_file"

    echo "torrc file has been created and updated."

    # Start Tor with the custom torrc file
    tor -f "$torrc_file"

    if [ $? -eq 0 ]; then
        echo "Tor has been started with the custom torrc file."
    else
        echo "Failed to start Tor. Make sure you have the necessary permissions."
    fi
fi
