#!/bin/bash

# Check for root privilege
if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root (use sudo)."
    exit 1
fi

while true; do
    # Display menu to the user
    echo "Choose the option you want to install:"
    echo "1. Tor and obfs4proxy (Bridge)"
    echo "2. Tor and nginx (Onion)"
    echo "3. Only Tor"
    echo "4. Exit"

    read -p "Enter the number of your choice: " user_choice

    case $user_choice in
        1)
            components=("-t bullseye-backports obfs4proxy")
            ;;
        2)
            components=("nginx")
            ;;
        3)
            components=()
            ;;
        4)
            echo "Exiting."
            exit 0
            ;;
        *)
            echo "Invalid choice. Please enter a valid option."
            continue
            ;;
    esac

    # Check the architecture
    architecture=$(dpkg --print-architecture)

    if [[ $architecture != "amd64" && $architecture != "arm64" && $architecture != "i386" ]]; then
        echo "Unsupported architecture: $architecture"
        exit 1
    fi

    # Install apt-transport-https
    apt update
    apt install -y apt-transport-https

    # Get OS codename
    codename=$(lsb_release -c --short)

    # Get OS architecture
    architecture=$(dpkg --print-architecture)

    # Create and configure the tor.list file
    echo "deb     [arch=$architecture signed-by=/usr/share/keyrings/tor-archive-keyring.gpg] https://deb.torproject.org/torproject.org $codename main" > /etc/apt/sources.list.d/tor.list
    echo "deb-src [arch=$architecture signed-by=/usr/share/keyrings/tor-archive-keyring.gpg] https://deb.torproject.org/torproject.org $codename main" >> /etc/apt/sources.list.d/tor.list

    # Add the Tor Project GPG key
    wget -qO- https://deb.torproject.org/torproject.org/A3C4F0F979CAA22CDBA8F512EE8CBC9E886DDD89.asc | gpg --dearmor | tee /usr/share/keyrings/tor-archive-keyring.gpg >/dev/null

    # Update the package list and install Tor and the Tor Project keyring
    apt update
    # Install the components
    for component in "${components[@]}"; do
      apt install -y "$component"

      # If the component is nginx, modify the nginx.conf file
      if [[ "$component" == "nginx" ]]; then
        # Print a message to the user command line
        echo "Modifying nginx.conf file..."
        # Get the current username
        current_user=$(whoami)

        # Replace 'user www-data;' with "user $current_user;" in nginx.conf
        sed -i "s/user www-data;/user $current_user;/" /etc/nginx/nginx.conf
      fi
    done
    apt install -y tor deb.torproject.org-keyring

    echo "Tor installation completed successfully."

    # Install unattended-upgrades and apt-listchanges
    apt-get update
    apt-get install -y unattended-upgrades apt-listchanges

# Configure 50unattended-upgrades
cat > /etc/apt/apt.conf.d/50unattended-upgrades <<EOF
Unattended-Upgrade::Origins-Pattern {
    "origin=Debian,codename=\${distro_codename},label=Debian-Security";
    "origin=TorProject";
};
Unattended-Upgrade::Package-Blacklist {
};
EOF

    # Configure 20auto-upgrades
    cat > /etc/apt/apt.conf.d/20auto-upgrades <<EOF
APT::Periodic::Update-Package-Lists "1";
APT::Periodic::AutocleanInterval "5";
APT::Periodic::Unattended-Upgrade "1";
APT::Periodic::Verbose "1";
EOF

    echo "Automatic updates and unattended upgrades have been configured."

    # Trigger the initial unattended-upgrades run
    unattended-upgrade -d

    echo "Installation and configuration completed successfully."
done
