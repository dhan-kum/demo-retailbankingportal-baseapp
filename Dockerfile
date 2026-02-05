#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY pom.xml /home/app
#ENV SERVER_HOME=/home/app/server/public
#WORKDIR $SERVER_HOME
COPY ./package.json /home/app
COPY ./webpack.config.js /home/app
RUN mvn -f /home/app/pom.xml clean install -Dskiptest=true

#
# Package stage
#
FROM openjdk:21-ea-10-jdk-slim
COPY --from=build /home/app/target/classes /target/classes
COPY --from=build /home/app/target/baseapp-0.0.1-SNAPSHOT.jar /usr/local/lib/baseapp.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/usr/local/lib/baseapp.jar"]

