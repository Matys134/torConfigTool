#!/bin/bash

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the torrc file relative to the script directory
torrc_file="$script_dir/../torrc/local-torrc-onion-service"

# Check if the torrc file exists
if [ -f "$torrc_file" ]; then
    # Clear the torrc file
    > "$torrc_file"

    # Add the specified lines to the torrc file
    echo 'HiddenServiceDir /var/lib/tor/my_website/' >> "$torrc_file"
    echo 'HiddenServicePort 80 127.0.0.1:80' >> "$torrc_file"

    echo "torrc file has been cleared and updated."
else
    echo "torrc file not found at: $torrc_file"
fi