:: SCRIPT TO SEND SOURCE CODES TO THE INSTANCES QUICKLY

setlocal

:: Keys location
set KEY_SERVER_PATH=C:\Users\german\ClonedRepos\proyectoRedesII_23-24\keys\german\serverProyecto_keys.pem
set KEY_CLIENT_PATH=C:\Users\german\ClonedRepos\proyectoRedesII_23-24\keys\german\clientProyecto_keys.pem

:: .java files location
set RCS=C:\Users\german\ClonedRepos\proyectoRedesII_23-24\src\RCS.java
set SERVERTHREAD=C:\Users\german\ClonedRepos\proyectoRedesII_23-24\src\ServerThread.java
set RCC=C:\Users\german\ClonedRepos\proyectoRedesII_23-24\src\RCC.java

:: Server and client public IPs
set SERVER_IP=54.83.69.115
set CLIENT_IP=44.223.29.1

:: Remote path where the files will be stored in the instances (I chose /home/ubuntu/proyectoRedes in both instances)
set REMOTE_PATH=/home/ubuntu/proyectoRedes

:: User for the instances
set USER=ubuntu

:: Send files via SCP
:: SERVER
scp -i %KEY_SERVER_PATH% %RCS% %USER%@%SERVER_IP%:%REMOTE_PATH%
scp -i %KEY_SERVER_PATH% %SERVERTHREAD% %USER%@%SERVER_IP%:%REMOTE_PATH%

:: CLIENT
scp -i %KEY_CLIENT_PATH% %RCC% %USER%@%CLIENT_IP%:%REMOTE_PATH%

endlocal