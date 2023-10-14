#!/bin/bash

# Check for root privilege
if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root (use sudo)."
    exit 1
fi

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

# Create and configure the tor.list file
echo "deb     [signed-by=/usr/share/keyrings/tor-archive-keyring.gpg] https://deb.torproject.org/torproject.org $codename main" > /etc/apt/sources.list.d/tor.list
echo "deb-src [signed-by=/usr/share/keyrings/tor-archive-keyring.gpg] https://deb.torproject.org/torproject.org $codename main" >> /etc/apt/sources.list.d/tor.list

# Add the Tor Project GPG key
wget -qO- https://deb.torproject.org/torproject.org/A3C4F0F979CAA22CDBA8F512EE8CBC9E886DDD89.asc | gpg --dearmor | tee /usr/share/keyrings/tor-archive-keyring.gpg >/dev/null

# Update the package list and install Tor and the Tor Project keyring
apt update
apt install -y tor deb.torproject.org-keyring

echo "Installation completed successfully."
