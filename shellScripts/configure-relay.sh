#!/bin/bash

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the torrc file relative to the script directory
torrc_file="$script_dir/../torrc/local-torrc"

# Check if the torrc file exists
if [ -f "$torrc_file" ]; then
    # Clear the torrc file
    > "$torrc_file"

    # Add the specified lines to the torrc file
    echo 'Nickname    myNiceRelay  # Change "myNiceRelay" to something you like' >> "$torrc_file"
    echo 'ContactInfo your@e-mail  # Write your e-mail and be aware it will be published' >> "$torrc_file"
    echo 'ORPort      443          # You might use a different port, should you want to' >> "$torrc_file"
    echo 'ExitRelay   0' >> "$torrc_file"
    echo 'SocksPort   0' >> "$torrc_file"

    echo "torrc file has been cleared and updated."
else
    echo "torrc file not found at: $torrc_file"
fi
