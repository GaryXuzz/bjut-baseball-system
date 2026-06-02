FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src ./src
COPY lib ./lib
COPY config.properties ./config.properties

RUN mkdir -p bin \
    && find src -name "*.java" > sources.txt \
    && javac -encoding UTF-8 -d bin @sources.txt \
    && rm sources.txt

EXPOSE 8080

CMD ["java", "-cp", "bin:lib/mysql-connector-j.jar", "bjut.baseball.Main"]
