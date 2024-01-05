#!/bin/bash
# Commands that require root permissions
sudo rm -f /etc/nginx/sites-available/onion-service-$1
sudo rm -f /etc/nginx/sites-enabled/onion-service-$1