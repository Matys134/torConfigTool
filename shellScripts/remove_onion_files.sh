#!/bin/bash
# Commands that require root permissions
rm -f /etc/nginx/sites-available/onion-service-$1
rm -f /etc/nginx/sites-enabled/onion-service-$1