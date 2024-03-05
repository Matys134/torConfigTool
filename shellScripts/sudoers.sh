# Define the sudoers entry for the specified user to control Tor without a password
sudoers_tor_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl start tor, /bin/systemctl stop tor, /bin/systemctl restart tor"

# Define the sudoers entry for the specified user to launch tor using tor -f without a password
sudoers_tor_entry="$current_user ALL=(ALL) NOPASSWD: /usr/bin/tor -f /etc/tor/torrc"

# Define the sudoers entry for the specified user to edit the nginx config and restart nginx without a password
sudoers_edit_nginx_entry="$current_user ALL=ALL) NOPASSWD: /bin/cp /etc/nginx/sites-available/default /etc/nginx/sites-available/*.conf, /bin/systemctl restart nginx"

# Define the sudoers entry for the specified user to reload Nginx without a password
sudoers_nginx_reload_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl reload nginx"

sudoers_nginx_start_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl start nginx"

sudoers_nginx_stop_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl stop nginx"

# Define the sudoers entry for the specified user to control snowflake-proxy without a password
sudoers_snowflake_proxy_entry="$current_user ALL=(ALL) NOPASSWD: /bin/systemctl start snowflake-proxy, /bin/systemctl stop snowflake-proxy"

sudoers_chown_entry="$current_user ALL=(ALL) NOPASSWD: /bin/chown -R"

# Define the sudoers entry for the specified user to use kill without a password
sudoers_kill_entry="$current_user ALL=(ALL) NOPASSWD: /bin/kill"

# Define the sudoers entry for the specified user to use ln -s without a password
sudoers_ln_entry="$current_user ALL=(ALL) NOPASSWD: /bin/ln -s"

# Define the path to the sudoers file
sudoers_file="/etc/sudoers.d/tor_nginx_sudoers"

# Define the sudoers entry for the specified user to run rm without a password
sudoers_rm_entry="$current_user ALL=(ALL) NOPASSWD: /bin/rm"

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
        echo "$sudoers_nginx_entry"
        echo "$sudoers_snowflake_proxy_entry"
        echo "$sudoers_chown_entry"
        echo "$sudoers_nginx_reload_entry"
        echo "$sudoers_kill_entry"
        echo "$sudoers_nginx_start_entry"
        echo "$sudoers_nginx_stop_entry"
        echo "$sudoers_ln_entry"
        echo "$sudoers_rm_entry"
    } | sudo tee "$sudoers_file" > /dev/null

    echo "Sudoers entries added for $current_user to control Tor, Nginx, copy Nginx config files, edit Nginx config files, and restart Nginx without a password."
else
    echo "Cannot write to $sudoers_file. Please run this script with superuser privileges."
fi