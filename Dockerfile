FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/clojback-0.0.1-SNAPSHOT-standalone.jar /clojback/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/clojback/app.jar"]
