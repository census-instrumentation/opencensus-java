FROM adoptopenjdk/openjdk12:slim

USER root

RUN apt-get -y upgrade && \
    apt-get -y update && \
    apt-get -y install unzip wget
