#!/bin/bash

# Get the current user
current_user=$(whoami)

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the configure-relay.sh script relative to the script directory
configure_relay_script="$script_dir/configure-relay.sh"

# Check if the configure-relay.sh script exists
if [ -f "$configure_relay_script" ]; then
    # Create or append to the sudoers file
    echo "$current_user ALL=(ALL:ALL) NOPASSWD: $configure_relay_script" | sudo tee -a /etc/sudoers.d/torsudoers

    echo "User $current_user can run $configure_relay_script without a password."
else
    echo "configure-relay.sh script not found at: $configure_relay_script"
fi
