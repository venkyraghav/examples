FROM eclipse-temurin:17-jdk

# Install Maven and unzip for debugging
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    curl \
    git \
    maven \
    unzip \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy source code
COPY . /app/

# Fix the directory structure - add missing src directory
RUN mkdir -p /app/src/main/resources/ && \
    if [ -d "/app/main" ]; then \
      cp -rv /app/main/* /app/src/main/ && \
      rm -rf /app/main; \
    fi

# Fix code style with Spotless and then build the JAR
# mvn spotless:apply && \
RUN mvn clean package && \
    ls -la target/*.jar

# Create an entrypoint script that uses the actual properties file path
RUN echo '#!/bin/bash\n\
echo "Running: $@"\n\
echo "JAR file exists: $(ls -la target/flink-datastream-firstapp-0.0.1-SNAPSHOT.jar 2>/dev/null || echo MISSING)"\n\
echo "Finding cloud.properties files:"\n\
find /app -name "cloud.properties" -type f\n\
echo "Starting application now."\n\
java -Dflink.properties=/app/target/classes/cloud.properties -jar target/flink-datastream-firstapp-0.0.1-SNAPSHOT.jar "$@"\n\
' > /app/entrypoint.sh && chmod +x /app/entrypoint.sh

# Use the script as entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]

# Default example class
CMD ["com.venkyraghav.examples.flink.Main"]