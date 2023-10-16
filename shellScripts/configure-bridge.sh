#!/bin/bash

# Get command-line arguments
bridgePort="$1"
bridgeTransportListenAddr="$2"
bridgeContact="$3"
bridgeNickname="$4"

# Get the directory of the script
script_dir=$(dirname "$0")

# Define the path to the torrc file relative to the script directory
torrc_file="$script_dir/../torrc/local-torrc-bridge"

# Check if the torrc file exists
if [ -f "$torrc_file" ]; then
    # Clear the torrc file
    > "$torrc_file"

    # Add the specified lines to the torrc file
    echo 'BridgeRelay 1' >> "$torrc_file"
    echo '' >> "$torrc_file"
    echo '# Replace "TODO1" with a Tor port of your choice.' >> "$torrc_file"
    echo '# This port must be externally reachable.' >> "$torrc_file"
    echo '# Avoid port 9001 because it''s commonly associated with Tor and censors may be scanning the Internet for this port.' >> "$torrc_file"
    echo "ORPort $bridgePort" >> "$torrc_file"
    echo '' >> "$torrc_file"
    echo 'ServerTransportPlugin obfs4 exec /usr/bin/obfs4proxy' >> "$torrc_file"
    echo '' >> "$torrc_file"
    echo '# Replace "TODO2" with an obfs4 port of your choice.' >> "$torrc_file"
    echo '# This port must be externally reachable and must be different from the one specified for ORPort.' >> "$torrc_file"
    echo '# Avoid port 9001 because it''s commonly associated with Tor and censors may be scanning the Internet for this port.' >> "$torrc_file"
    echo "ServerTransportListenAddr obfs4 0.0.0.0:$bridgeTransportListenAddr" >> "$torrc_file"
    echo '' >> "$torrc_file"
    echo '# Local communication port between Tor and obfs4.  Always set this to "auto".' >> "$torrc_file"
    echo '# "Ext" means "extended", not "external".  Don''t try to set a specific port number, nor listen on 0.0.0.0.' >> "$torrc_file"
    echo 'ExtORPort auto' >> "$torrc_file"
    echo '' >> "$torrc_file"
    echo '# Replace "<address@email.com>" with your email address so we can contact you if there are problems with your bridge.' >> "$torrc_file"
    echo '# This is optional but encouraged.' >> "$torrc_file"
    echo "ContactInfo $bridgeContact" >> "$torrc_file"
    echo '' >> "$torrc_file"
    echo '# Pick a nickname that you like for your bridge.  This is optional.' >> "$torrc_file"
    echo "Nickname $bridgeNickname" >> "$torrc_file"

    echo "torrc bridge configuration has been updated."
else
    echo "torrc bridge configuration file not found at: $torrc_file"
fi