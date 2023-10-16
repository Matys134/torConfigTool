#!/bin/bash

# Get command-line arguments
relayNickname="$1"
relayBandwidth="$2"
relayPort="$3"
relayContact="$4"

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the torrc file relative to the script directory
torrc_file="$script_dir/../torrc/local-torrc"

# Check if the torrc file exists
if [ -f "$torrc_file" ]; then
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
else
    echo "torrc file not found at: $torrc_file"
fi
