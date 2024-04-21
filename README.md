# Tor Configuration Web Application

**Project Overview**

This project, originally created as part of a bachelor thesis, provides a user-friendly web application for configuring various Tor services on a Raspberry Pi running Raspberry OS. These services include:

* **Onion Services** 
* **Bridges (obfs4, webtunnel, snowflake)**
* **Non-exit Relays**

**Prerequisites**

* Raspberry Pi Computer
* Raspberry OS (formerly known as Raspbian)

**Installation and Setup**

1. **Install Dependencies (Recommended):**
   * Run the `install.sh` script in the installation folder:
     ```bash
     cd installation
     sudo ./install.sh
     ```
   * This script automates the installation of required software.

2. **Create User Credentials:**
   * Run the `createUser.sh` script:
     ```bash
     sudo ./createUser.sh
     ```
   * Follow the prompts to create a username and password for the web application.

3. **Start the Application:**
   * Execute the JAR file:
     ```bash
     java -jar torConfigTool.jar
     ```
   * The application should now be running on port 8443. Access it in your web browser at `https://<your-raspberry-pi-ip>:8443`

**Manual Configuration (Optional)**

If you prefer to install dependencies and configure Tor services manually, you can do so and then modify the `config.txt` file:

* Edit `config.txt` to enable desired Tor services by changing their corresponding values from 0 to 1.  
* Refer to the Tor documentation for more details on specific services and configurations.

**Important Notes:**

* Replace `<your-raspberry-pi-ip>` in the web address with your Raspberry Pi's actual IP address.
* Ensure proper firewall rules are in place to allow access to port 8443 if needed. 
