# Dockerfile
FROM clojure:temurin-17-lein-alpine

WORKDIR /usr/src/app

COPY . .

RUN lein deps

CMD ["lein", "run"]
