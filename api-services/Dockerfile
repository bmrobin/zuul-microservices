FROM openjdk:8u131
COPY target/api-services-*.jar /opt/app.jar
ENTRYPOINT exec java $JAVA_OPTS -jar /opt/app.jar
