#!/bin/bash

# Update the system
sudo yum update -y

# Install Python 3, Git and pip
sudo yum install -y python3
sudo alternatives --set python /usr/bin/python3
sudo yum install git -y

# Ensure pip is up-to-date
python3 -m pip install --upgrade pip

# Install and start cron
sudo yum install cronie -y
sudo service crond start
sudo systemctl enable crond

# Add Zulu's yum repository
sudo rpm --import https://repos.azul.com/azul-repo.key
sudo curl -o /etc/yum.repos.d/zulu.repo https://repos.azul.com/zulu/rhel/zulu.repo

# Install OpenJDK 21
sudo yum install -y zulu21-jdk

# Install Docker
sudo amazon-linux-extras install docker -y

# Start Docker service
sudo service docker start

# Enable Docker to start on boot
sudo systemctl enable docker
sudo usermod -aG docker $USER

echo "If you installed the script with a user that isn't root, you might need to reboot"

# Install aenv
PACKAGE_NAME="aenv"
pip3 install $PACKAGE_NAME

# Github setup key and
ssh-keygen -t rsa -b 4096 -C "repowatcher@7mw.de"
eval "$(ssh-agent -s)"
ssh-add ~/.ssh/id_rsa
chmod 700 ~/.ssh
chmod 600 ~/.ssh/id_rsa
chmod 644 ~/.ssh/id_rsa.pub

echo "Copy the public key and allow read access to the repo"
sleep 5
cat ~/.ssh/id_rsa.pub

# Setup caddy
yum -y install yum-plugin-copr
yum -y copr enable @caddy/caddy epel-7-$(arch)
yum -y install caddy
sudo mkdir /etc/caddy
sudo nano /etc/caddy/Caddyfile
sudo systemctl daemon-reload
sudo systemctl enable --now caddy
systemctl status -l caddy

# Verify installations
echo "Python version: $(python3 --version)"
echo "Pip version: $(pip3 --version)"
echo "Docker version: $(docker --version)"
echo "Java version: $(java --version)"

echo "Setup completed successfully."

echo "Don't forget to add the watcher script to crontab ->  crontab -e -> */5 * * * * /path/to/your/script.sh"