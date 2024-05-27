#!/bin/bash

# This script is used to connect to two different servers using SSH.

# Keys location
KEY_SERVER_PATH="/home/german/Documents/clonedRepos/proyectoRedesII_23-24/keys/german/serverProyecto_keys.pem"
KEY_CLIENT_PATH="/home/german/Documents/clonedRepos/proyectoRedesII_23-24/keys/german/clientProyecto_keys.pem"

# .java files location
RCS="/home/german/Documents/clonedRepos/proyectoRedesII_23-24/src/RCS.java"
SERVERTHREAD="/home/german/Documents/clonedRepos/proyectoRedesII_23-24/src/ServerThread.java"
RCC="/home/german/Documents/clonedRepos/proyectoRedesII_23-24/src/RCC.java"

# Server and client public IPs
SERVER_IP="3.87.83.115"
CLIENT_IP="52.201.236.175"

# Remote path where the files will be stored in the instances (I chose /home/ubuntu/proyectoRedes in both instances)
REMOTE_PATH="/home/ubuntu/proyectoRedes"

# User for the instances
USER="ubuntu"

# Send files via SCP
# SERVER
scp -i "$KEY_SERVER_PATH" "$RCS" "$USER@$SERVER_IP:$REMOTE_PATH"
scp -i "$KEY_SERVER_PATH" "$SERVERTHREAD" "$USER@$SERVER_IP:$REMOTE_PATH"

# CLIENT
scp -i "$KEY_CLIENT_PATH" "$RCC" "$USER@$CLIENT_IP:$REMOTE_PATH"
