#!/bin/bash

# Check if the script is run with sudo privileges
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script with superuser privileges (sudo)."
    exit 1
fi

# Determine the full path of the script
script_dir="$(dirname "$(readlink -f "$0")")"

# Define the name of the script folder
script_folder="shellScripts"

# Define the full path to the script folder
script_folder_path="$script_dir/$script_folder"

# Check if the script folder exists
if [ -d "$script_folder_path" ]; then
    # Define the sudoers entry for the user who ran this script with sudo
    sudoers_entry="$(logname) ALL=(ALL) NOPASSWD: $script_folder_path/*"

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

        echo "Sudoers entry added for $(logname) to run scripts in $script_folder_path"
    else
        echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
    fi
else
    echo "$script_folder not found at: $script_folder_path"
fi
