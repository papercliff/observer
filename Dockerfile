# Dockerfile
FROM clojure:temurin-17-lein-alpine

# Install necessary dependencies for wget, Chrome, and ChromeDriver
RUN apk add --no-cache curl unzip libexif udev chromium chromium-chromedriver

# Set the PATH environment variable for ChromeDriver
ENV PATH /usr/bin/chromedriver:$PATH

# Set Chrome's path
ENV CHROME_BIN /usr/bin/chromium

WORKDIR /usr/src/app

COPY . .

RUN lein deps

CMD ["lein", "run"]
