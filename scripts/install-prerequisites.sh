#!/bin/bash

# Function to check if a command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to print status messages
print_status() {
    echo "===> $1"
}

# Check operating system
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    print_status "Detected macOS system"
    
    # Check if Homebrew is installed
    if ! command_exists brew; then
        print_status "Installing Homebrew..."
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
    else
        print_status "Updating Homebrew..."
        brew update
    fi

    # Install Java if not present
    if ! command_exists java; then
        print_status "Installing OpenJDK 17..."
        brew install openjdk@17
        sudo ln -sfn $(brew --prefix)/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    fi

    # Install Maven if not present
    if ! command_exists mvn; then
        print_status "Installing Maven..."
        brew install maven
    fi

    # Install PostgreSQL if not present
    if ! command_exists psql; then
        print_status "Installing PostgreSQL..."
        brew install postgresql@14
        brew services start postgresql@14
    fi

    # Install Redis if not present
    if ! command_exists redis-cli; then
        print_status "Installing Redis..."
        brew install redis
        brew services start redis
    fi

    # Install Kafka if not present
    if ! command_exists kafka-topics; then
        print_status "Installing Kafka..."
        brew install kafka
        brew services start zookeeper
        brew services start kafka
    fi

    # Install Elasticsearch if not present
    if ! command_exists elasticsearch; then
        print_status "Installing Elasticsearch..."
        brew install elasticsearch
        brew services start elasticsearch
    fi

    # Install Docker if not present
    if ! command_exists docker; then
        print_status "Installing Docker..."
        brew install --cask docker
    fi

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    # Linux
    print_status "Detected Linux system"

    # Update package list
    print_status "Updating package list..."
    sudo apt-get update

    # Install Java if not present
    if ! command_exists java; then
        print_status "Installing OpenJDK 17..."
        sudo apt-get install -y openjdk-17-jdk
    fi

    # Install Maven if not present
    if ! command_exists mvn; then
        print_status "Installing Maven..."
        sudo apt-get install -y maven
    fi

    # Install PostgreSQL if not present
    if ! command_exists psql; then
        print_status "Installing PostgreSQL..."
        sudo apt-get install -y postgresql postgresql-contrib
        sudo systemctl start postgresql
        sudo systemctl enable postgresql
    fi

    # Install Redis if not present
    if ! command_exists redis-cli; then
        print_status "Installing Redis..."
        sudo apt-get install -y redis-server
        sudo systemctl start redis-server
        sudo systemctl enable redis-server
    fi

    # Install Kafka
    if ! command_exists kafka-topics; then
        print_status "Installing Kafka..."
        # Add Kafka repository
        wget https://downloads.apache.org/kafka/3.6.1/kafka_2.13-3.6.1.tgz
        tar xzf kafka_2.13-3.6.1.tgz
        sudo mv kafka_2.13-3.6.1 /opt/kafka
        rm kafka_2.13-3.6.1.tgz
        
        # Create systemd service for Zookeeper and Kafka
        sudo tee /etc/systemd/system/zookeeper.service <<EOF
[Unit]
Description=Apache Zookeeper server
Documentation=http://zookeeper.apache.org
Requires=network.target remote-fs.target
After=network.target remote-fs.target

[Service]
Type=simple
ExecStart=/opt/kafka/bin/zookeeper-server-start.sh /opt/kafka/config/zookeeper.properties
ExecStop=/opt/kafka/bin/zookeeper-server-stop.sh
Restart=on-abnormal

[Install]
WantedBy=multi-user.target
EOF

        sudo tee /etc/systemd/system/kafka.service <<EOF
[Unit]
Description=Apache Kafka Server
Documentation=http://kafka.apache.org/documentation.html
Requires=zookeeper.service
After=zookeeper.service

[Service]
Type=simple
Environment="JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64"
ExecStart=/opt/kafka/bin/kafka-server-start.sh /opt/kafka/config/server.properties
ExecStop=/opt/kafka/bin/kafka-server-stop.sh
Restart=on-abnormal

[Install]
WantedBy=multi-user.target
EOF

        sudo systemctl daemon-reload
        sudo systemctl start zookeeper
        sudo systemctl start kafka
        sudo systemctl enable zookeeper
        sudo systemctl enable kafka
    fi

    # Install Elasticsearch if not present
    if ! command_exists elasticsearch; then
        print_status "Installing Elasticsearch..."
        wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | sudo apt-key add -
        echo "deb https://artifacts.elastic.co/packages/8.x/apt stable main" | sudo tee /etc/apt/sources.list.d/elastic-8.x.list
        sudo apt-get update
        sudo apt-get install -y elasticsearch
        sudo systemctl start elasticsearch
        sudo systemctl enable elasticsearch
    fi

    # Install Docker if not present
    if ! command_exists docker; then
        print_status "Installing Docker..."
        curl -fsSL https://get.docker.com -o get-docker.sh
        sudo sh get-docker.sh
        sudo usermod -aG docker $USER
        rm get-docker.sh
    fi
else
    print_status "Unsupported operating system"
    exit 1
fi

# Create required PostgreSQL databases
print_status "Creating PostgreSQL databases..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    createuser -s postgres 2>/dev/null || true
fi

psql -U postgres -c "CREATE DATABASE user_service;" 2>/dev/null || true
psql -U postgres -c "CREATE DATABASE movie_service;" 2>/dev/null || true
psql -U postgres -c "CREATE DATABASE theatre_service;" 2>/dev/null || true
psql -U postgres -c "CREATE DATABASE booking_service;" 2>/dev/null || true
psql -U postgres -c "CREATE DATABASE payment_service;" 2>/dev/null || true
psql -U postgres -c "CREATE DATABASE notification_service;" 2>/dev/null || true

# Verify installations
print_status "Verifying installations..."
echo "Java version: $(java -version 2>&1 | head -n 1)"
echo "Maven version: $(mvn -version | head -n 1)"
echo "PostgreSQL version: $(psql --version)"
echo "Redis version: $(redis-cli --version)"
echo "Elasticsearch: $(curl -s localhost:9200 | grep number || echo 'Not running')"
echo "Docker version: $(docker --version)"

# Verify services are running
print_status "Checking service status..."
if [[ "$OSTYPE" == "darwin"* ]]; then
    brew services list | grep postgresql
    brew services list | grep redis
    brew services list | grep elasticsearch
    brew services list | grep kafka
else
    sudo systemctl status postgresql | grep Active
    sudo systemctl status redis-server | grep Active
    sudo systemctl status elasticsearch | grep Active
    sudo systemctl status kafka | grep Active
fi

print_status "Installation complete! Please check above for any errors."
print_status "Note: You may need to restart your terminal for some changes to take effect."
