# IP addresses
$IP_SERVER = ""
$IP_CLIENT = ""

# Key paths
$KEY_PATH_SERVER = ""
$KEY_PATH_CLIENT = ""

# Open a PowerShell window and execute the first ssh command
Start-Process powershell -ArgumentList "-NoExit", "-Command", "ssh -i $KEY_PATH_SERVER ubuntu@$IP_SERVER"

# Open a new PowerShell window and execute the second ssh command
Start-Process powershell -ArgumentList "-NoExit", "-Command", "ssh -i $KEY_PATH_CLIENT ubuntu@$IP_CLIENT"
