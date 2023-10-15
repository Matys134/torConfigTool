#!/bin/bash

# Check the EUID (effective user ID) to see who switched to su
if [ $EUID -eq 0 ]; then
    # You are currently the root user
    if [ -n "$SUDO_USER" ]; then
        # The user who switched to root is in $SUDO_USER
        user_to_grant_sudo="$SUDO_USER"

        # Define the sudoers entry for the specific user
        sudoers_entry="$user_to_grant_sudo ALL=(ALL) NOPASSWD: $configure_relay_script"

        # Define the path to the sudoers file
        sudoers_file="/etc/sudoers.d/torsudoers"

        # Check if the sudoers file exists, and create it if it doesn't
        if [ ! -f "$sudoers_file" ]; then
            touch "$sudoers_file"
        fi

        # Check if the sudoers file is writable
        if [ -w "$sudoers_file" ]; then
            # Add the sudoers entry for the specific user
            echo "$sudoers_entry" | sudo tee -a "$sudoers_file" > /dev/null

            echo "Sudoers entry added for $user_to_grant_sudo to run $configure_relay_script without a password."
        else
            echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
        fi
    else
        echo "You switched to root using 'su' without specifying a user. Please specify a user with 'su - user' and then run this script again."
    fi
else
    # You are not running as root
    echo "You are not running as root. Please switch to root using 'su' before running this script."
fi
