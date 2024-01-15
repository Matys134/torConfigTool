#!/bin/bash

# Check if the script is run with sudo
if [ "$EUID" -ne 0 ]
  then echo "Please run as root"
  exit
fi

# Check if email argument is provided
if [ -z "$1" ]
  then echo "Please provide an email address as an argument"
  exit
fi

# Step 1: Install acme.sh
curl https://get.acme.sh | sh -s email=$1

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