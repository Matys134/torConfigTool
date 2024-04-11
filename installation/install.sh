#!/bin/bash

# Check for root privilege
if [[ $EUID -ne 0 ]]; then
    echo "This script must be run as root (use sudo)."
    exit 1
fi

# Get the user who launched the script
current_user=$SUDO_USER



# Install Python Stem library
apt install -y python3-stem

# Function for installing a package including its dependencies
install_package() {
    if ! dpkg -s "$1" >/dev/null 2>&1; then # Check if package is installed
        apt update
        apt install -y "$*"
    else
        echo "$1 is already installed."
    fi
}

# Display menu for user selection
echo "Select the types of Tor services you want to set up (separate choices by space):"
echo "1. Non-exit node or proxy"
echo "2. Onion service"
echo "3. obfs4 bridge"
echo "4. Snowflake bridge"
echo "5. WebTunnel bridge"
read -p "Enter your choices (1-5): " input

# Convert the choices to an array
choices=($input)


# Install apt-transport-https
install_package apt-transport-https

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
install_package tor
install_package deb.torproject.org-keyring

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

# Initialize the config.txt file with all services set to 0
echo "Onion 0" >> config.txt
echo "obfs4 0" >> config.txt
echo "Snowflake 0" >> config.txt
echo "WebTunnel 0" >> config.txt

# Install software based on user selection
for choice in "${choices[@]}"; do
    case $choice in
    1)  # Non-exit node
        ;;
    2)  # Onion service
        install_package nginx
        sed -i 's/Onion 0/Onion 1/' config.txt
        ;;
    3)  # obfs4 bridge
        install_package obfs4proxy
        sed -i 's/obfs4 0/obfs4 1/' config.txt
        ;;
    4)  # Snowflake bridge
        install_package snowflake-proxy
        sed -i 's/Snowflake 0/Snowflake 1/' config.txt
        ;;
    5)  # WebTunnel bridge
        # Prompt the user for their email address
        read -p "Enter your email address for acme protocol: " email
        install_package nginx
        install_package golang

        # Step 1: Install acme.sh
        sudo -u $SUDO_USER bash -c "curl https://get.acme.sh | sh -s email=$email"

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
        sed -i '/# directories via check_private_dir().  Let it./a \\  /usr/local/bin/webtunnel ix,' /etc/apparmor.d/system_tor

        # Step 6: Reload the AppArmor profiles
        sudo apparmor_parser -r /etc/apparmor.d/system_tor
        sed -i 's/WebTunnel bridge 0/WebTunnel bridge 1/' config.txt
        ;;
    *)
        echo "Invalid selection."
        ;;
esac
done

echo "Installation complete!"

# Remove the default file from sites-available
rm /etc/nginx/sites-available/default

# Remove the default file from sites-enabled
rm /etc/nginx/sites-enabled/default

# Add user to the www-data group
usermod -a -G www-data "$current_user"

sudo chmod o+x /home/$current_user

echo "Tor installation completed successfully."

echo "Installation and configuration completed successfully."

# Disable tor and nginx services from starting at boot
sudo systemctl disable tor.service
sudo systemctl disable nginx.service

# Change the ownership of the nginx default site configuration file
sudo chown $SUDO_USER /etc/nginx/sites-available/default

sudo systemctl restart nginx

echo "Running UserHasher..."
read -p "Enter your username: " username
read -p "Enter your password: " password

sudo -u $current_user bash -c "/home/$current_user/.sdkman/candidates/java/current/bin/java -jar userHasherApplication.jar $username $password"