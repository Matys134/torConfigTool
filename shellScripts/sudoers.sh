#!/bin/bash

# Check if the script is run with sudo privileges
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script with superuser privileges (sudo)."
    exit 1
fi

# Determine the full path of the script
script_dir="$(dirname "$(readlink -f "$0")")"

# Define the name of the script
script_name="configure-relay.sh"

# Define the full path to the configure-relay.sh script
configure_relay_script="$script_dir/$script_name"

# Check if the configure-relay.sh file exists
if [ -f "$script_dir" ]; then
    # Define the sudoers entry for the user who ran this script with sudo
    sudoers_entry="$(logname) ALL=(ALL) NOPASSWD: $script_dir/*"

    # Define the path to the sudoers file
    sudoers_file="/etc/sudoers.d/torsudoers"

    # Check if the sudoers file exists, and create it if it doesn't
    if [ ! -f "$sudoers_file" ]; then
        touch "$sudoers_file"
    fi

    # Check if the sudoers file is writable
    if [ -w "$sudoers_file" ]; then

        # Clear the sudoers file
        > "$sudoers_file"

        # Add the sudoers entry
        echo "$sudoers_entry" | sudo tee -a "$sudoers_file" > /dev/null

        echo "Sudoers entry added for $(logname) to run scripts in $script_dir"
    else
        echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
    fi
else
    echo "Script folder not found at: $script_dir"
fi
