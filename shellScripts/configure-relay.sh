#!/bin/bash

# Get the directory of the shell script
script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Define the path to the torrc file relative to the script location
torrc_file="$script_dir/torrc/local-torrc"

# Clear the torrc file
> "$torrc_file"

# Add the desired configuration lines to the torrc file
cat <<EOF > "$torrc_file"
Nickname    myNiceRelay  # Change "myNiceRelay" to something you like
ContactInfo your@e-mail  # Write your e-mail and be aware it will be published
ORPort      443          # You might use a different port, should you want to
ExitRelay   0
SocksPort   0
EOF

echo "torrc/local-torrc has been updated."
