# Server Documentation

## Overview
This project is part of the UpHill Backend Interview Challenge and serves as a server application that listens to client connections. It's designed to meet the specifications outlined in the challenge requirements.

## Prerequisits
- Java Development Kit (JDK) 17 or higher

## Building the Server
To build the server from source code, follow these steps:
  - cd server
  - ./mvnw clean package

## Running the Server
After building the project using Maven, the executable JAR file is generated in the target folder. You can find the JAR file named server.jar in the target directory.
To run the server, execute the following commands:
  - cd target
  - java -jar server.jar
    ### Options
  - -d or --debug: Enable debug mode for additional logging.

## Pre-built JAR
If you prefer not to build the server from source, you can download the pre-built JAR file from the [releases page](https://github.com/pauloviana26/uphill-backend-challenge/releases) of this repository.
