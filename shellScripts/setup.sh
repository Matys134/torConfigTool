#!/bin/bash

# Prompt for superuser password
su -c "echo 'Please enter your superuser password:'" root

# Run superuser commands (replace with your specific package installation and update commands)
su -c "apt-get update" root
su -c "apt-get install -y package1 package2 package3" root
# Add more commands as needed

# Exit superuser
exit
