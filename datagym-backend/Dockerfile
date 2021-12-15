FROM openjdk:17-slim
MAINTAINER datagym.ai

# ffmpeg/ffprobe for video labelling
RUN apt-get update && apt-get -y install ffmpeg

# Copy artifacts provided by maven plugin
COPY maven/ /app/

EXPOSE 8080

WORKDIR /app

ENTRYPOINT ["java", "-Xmx512m", "-jar",  "/app/datagym.jar"]
