# Use instrumentisto/geckodriver as the base image
FROM instrumentisto/geckodriver:115.0.2

# Set the working directory
WORKDIR /app

# Install curl, OpenJDK 17, and Firefox
RUN apt-get update && apt-get install -y curl openjdk-17-jdk firefox-esr

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
