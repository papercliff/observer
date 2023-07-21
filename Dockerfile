# Use selenium/standalone-firefox as the base image
FROM selenium/standalone-firefox:114.0

# Set the working directory
WORKDIR /app

USER root

# Install curl and OpenJDK 17
RUN apt-get update && apt-get install -y curl openjdk-17-jdk

# Install Leiningen
RUN mkdir -p /usr/local/bin/
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
RUN chmod a+x /usr/local/bin/lein

# Copy the project files necessary for Leiningen to fetch dependencies
COPY project.clj /app/
RUN lein deps

# Copy the rest of the application
COPY . /app

# Run your app
CMD ["lein", "run"]
