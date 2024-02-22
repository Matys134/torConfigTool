#!/bin/bash

# Check for root privilege
if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root (use sudo)."
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
apt install -y tor deb.torproject.org-keyring -t bullseye-backports obfs4proxy nginx snowflake-proxy

# Get the user who launched the script
current_user=$SUDO_USER

# Replace 'user www-data;' with "user $current_user;" in nginx.conf
sed -i "s/user www-data;/user $current_user;/" /etc/nginx/nginx.conf

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

# Disable tor and nginx services from starting at boot
sudo systemctl disable tor.service
sudo systemctl disable nginx.service

# Define the sudoers entry for the specified user to control Tor without a password
sudoers_tor_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl start tor, /bin/systemctl stop tor, /bin/systemctl restart tor""

# Define the sudoers entry for the specified user to launch tor using tor -f without a password
sudoers_tor_entry="$current_user ALL=(ALL) NOPASSWD: /usr/bin/tor -f /etc/tor/torrc"

# Define the sudoers entry for the specified user to edit the nginx config and restart nginx without a password
sudoers_edit_nginx_entry="$current_user ALL=(ALL) NOPASSWD: /bin/cp /etc/nginx/sites-available/default /etc/nginx/sites-available/*.conf, /bin/systemctl restart nginx"

# Define the path to the sudoers file
sudoers_file="/etc/sudoers.d/tor_nginx_sudoers"

# Check if the sudoers file exists, and create it if it doesn't
if [ ! -f "$sudoers_file" ]; then
    touch "$sudoers_file"
fi

# Define the sudoers entry for the specified user to control Nginx without a password
sudoers_nginx_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl start nginx, /bin/systemctl stop nginx, /bin/systemctl restart nginx"

# Check if the sudoers file is writable
if [ -w "$sudoers_file" ]; then
    # Clear the sudoers file
    > "$sudoers_file"

    # Add the sudoers entries
    {
        echo "$sudoers_tor_entry"
        echo "$sudoers_edit_nginx_entry"
        echo "$sudoers_nginx_entry" # Add this line
    } | sudo tee "$sudoers_file" > /dev/null

    echo "Sudoers entries added for $current_user to control Tor, Nginx, copy Nginx config files, edit Nginx config files, and restart Nginx without a password."
else
    echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
fi

# Change the ownership of the nginx default site configuration file
sudo chown matys /etc/nginx/sites-available/default

# Check if email argument is provided
if [ -z "$1" ]
  then echo "Please provide an email address as an argument"
  exit
fi

# Step 1: Install acme.sh
sudo -u $SUDO_USER curl https://get.acme.sh | sh -s email=$1

# Step 2: Install golang
sudo apt install golang

# Step 3: Clone the webtunnel repository and build the server
git clone https://gitlab.torproject.org/tpo/anti-censorship/pluggable-transports/webtunnel
cd webtunnel/main/server
go build
sudo cp server /usr/local/bin/webtunnel

# Step 4: Edit /boot/cmdline.txt
echo "apparmor=1 security=apparmor" | sudo tee -a /boot/cmdline.txt

# Step 5: Add a line to /etc/apparmor.d/system_tor
awk '/# directories via check_private_dir().  Let it./ { print; printf "/usr/local/bin/webtunnel ix,\n"; next }1' /etc/apparmor.d/system_tor > temp && mv temp /etc/apparmor.d/system_tor

# Step 6: Reload the AppArmor profiles
sudo apparmor_parser -r /etc/apparmor.d/system_tor

echo "Please enter a password for the web application:"
read -s APP_PASSWORD
export APP_PASSWORD