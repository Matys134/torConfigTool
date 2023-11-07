#!/bin/bash

# Check if the script is run with sudo privileges
if [ "$EUID" -ne 0 ]; then
    echo "Please run this script with superuser privileges (sudo)."
    exit 1
fi

# Define the username of the user who will have sudo permissions
user_name="$(logname)"

# Define the sudoers entry for the specified user to control Tor without a password
sudoers_entry="$user_name ALL=(ALL) NOPASSWD: /bin/systemctl start tor, /bin/systemctl stop tor, /bin/systemctl restart tor"

# Define the sudoers entry for the specified user to copy the apache2 config without a password
sudoers_cp_entry="$user_name ALL=(ALL) NOPASSWD: /bin/cp /etc/apache2/sites-available/000-default.conf /etc/apache2/sites-available/*.conf"

# Define the path to the sudoers file
sudoers_file="/etc/sudoers.d/tor_sudoers"

# Check if the sudoers file exists, and create it if it doesn't
if [ ! -f "$sudoers_file" ]; then
    touch "$sudoers_file"
fi

# Check if the sudoers file is writable
if [ -w "$sudoers_file" ]; then
    # Clear the sudoers file
    > "$sudoers_file"

    # Add the sudoers entries
    {
        echo "$sudoers_entry"
        echo "$sudoers_cp_entry"
    } | sudo tee "$sudoers_file" > /dev/null

    echo "Sudoers entries added for $user_name to control Tor and copy apache2 config files without a password."
else
    echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
fi
