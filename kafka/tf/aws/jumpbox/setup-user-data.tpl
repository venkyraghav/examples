#!/bin/bash
sudo su - ubuntu
cd /home/ubuntu

apt update -y

# Install bootstrap software set
apt install openjdk-21-jdk curl gnupg nginx python3 python3-pip unzip software-properties-common apt-transport-https ca-certificates nginx-extras -y

# Install ansible
add-apt-repository --yes --update ppa:ansible/ansible
apt install ansible -y

# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install
rm -Rf awscliv2.zip aws

# Install Confluent CLI
mkdir -p /etc/apt/keyrings
curl https://packages.confluent.io/confluent-cli/deb/archive.key | gpg --dearmor -o /etc/apt/keyrings/confluent-cli.gpg
chmod go+r /etc/apt/keyrings/confluent-cli.gpg
echo "deb [signed-by=/etc/apt/keyrings/confluent-cli.gpg] https://packages.confluent.io/confluent-cli/deb stable main" | tee /etc/apt/sources.list.d/confluent-cli.list >/dev/null
apt update -y
apt install confluent-cli -y

# Install kubectl
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.30/deb/Release.key | gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.30/deb/ /" | tee /etc/apt/sources.list.d/kubernetes.list
apt install -y kubectl
apt-mark hold kubectl
