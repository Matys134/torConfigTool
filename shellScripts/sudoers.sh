#!/bin/bash

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the configure-relay.sh file relative to the script directory
configure_relay_script="$script_dir/configure-relay.sh"

# Check if the configure-relay.sh file exists
if [ -f "$configure_relay_script" ]; then
    # Define the sudoers entry
    sudoers_entry="$(whoami) ALL=(ALL) NOPASSWD: $configure_relay_script"

    # Define the path to the sudoers file
    sudoers_file="/etc/sudoers.d/torsudoers"

    # Check if the sudoers file exists, and create it if it doesn't
    if [ ! -f "$sudoers_file" ]; then
        touch "$sudoers_file"
    fi

    # Check if the sudoers file is writable
    if [ -w "$sudoers_file" ]; then
        # Add the sudoers entry
        echo "$sudoers_entry" | sudo tee -a "$sudoers_file" > /dev/null

        echo "Sudoers entry added for $configure_relay_script"
    else
        echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
    fi
else
    echo "configure-relay.sh file not found at: $configure_relay_script"
fi
