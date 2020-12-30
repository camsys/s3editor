FROM java
ARG JAR_FILE
ENV _JAVA_OPTIONS "-Xms256m -Xmx512m -Djava.awt.headless=true"
COPY ${JAR_FILE} /opt/app.jar
WORKDIR /opt
EXPOSE 8080
ENTRYPOINT ["java","-jar","/opt/app.jar","--server.port=80","--production"]
