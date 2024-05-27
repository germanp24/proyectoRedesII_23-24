#!/bin/bash

# IP addresses
IP_SERVER=""
IP_CLIENT=""

# Key paths
KEY_PATH_SERVER=""
KEY_PATH_CLIENT=""

# Open a new terminal tab and execute the first ssh command
gnome-terminal --tab -- bash -c "ssh -i $KEY_PATH_SERVER ubuntu@$IP_SERVER; exec bash"

# Open a new terminal tab and execute the second ssh command
gnome-terminal --tab -- bash -c "ssh -i $KEY_PATH_CLIENT ubuntu@$IP_CLIENT; exec bash"

