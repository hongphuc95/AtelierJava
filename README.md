# Atelier Java Embarqué 

## Description
Develop an Android application that allows the downloading of a video file from the web and streaming among Android devices. 

For the purpose of this project you will need at least two Android devices that will act as a server and client as described below.

**1st Device (server)**
- Downloads a video file from a location on the internet.
- Saves the video file locally. 
- Serves the downloaded video file to nearby devices

**2nd Device (client)**
- Discovers and connects to nearby devices that serve the video file. The discovery and communication should happen locally without using any 3rd party entity. 
- Streams the video file from the connected peer (server) using a custom-built process and not by calling an installed media player.