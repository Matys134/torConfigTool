#!/bin/bash

# Determine the full path of the script
script_dir="$(dirname "$(readlink -f "$0")")"

# Define the name of the script
script_name="configure-relay.sh"

# Define the full path to the configure-relay.sh script
configure_relay_script="$script_dir/$script_name"

# Check if the configure-relay.sh file exists
if [ -f "$configure_relay_script" ]; then

    # Determine the original user who invoked sudo
    original_user="$SUDO_USER"

    if [ -n "$original_user" ]; then
        # Define the sudoers entry with the original user's username
        sudoers_entry="$original_user ALL=(ALL) NOPASSWD: $configure_relay_script"

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

            echo "Sudoers entry added for $configure_relay_script, assigned to user: $original_user"
        else
            echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
        fi
    else
        echo "Unable to determine the original user who invoked sudo."
    fi
else
    echo "$script_name not found at: $configure_relay_script"
fi
