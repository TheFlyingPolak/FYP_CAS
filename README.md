# FYP_CAS

This repository contains the Central Administration Server (CAS), the element of the Final Year Project which collect data logged by the [agent](https://github.com/TheFlyingPolak/FYP_Agent), as well as the web UI which displays the information collected and allows users to remotely send basic commands to agents. The CAS stores logs as JSON documents in a MongoDB database.

## Project Structure
- sec/main/java: Contains the CAS source code
- src/main/resources/templates: Contains HTML pages of the web UI

## CAS Structure
### com.gajewski.admin
- ```DataController```: Defines the HTTP methods used to send and receive data to and from the database and to send commands to agents. It also includes methods to check for unused ID numbers when agents initialise contact with the CAS and to check whether a given ID is in use.
- ```WebController```: Defines mappings to the pages of the web UI.
- ```ConflictFinder```: Defines methods used to search through all data collected and find duplicated packages with out of sync versions, also referred to as conflicts. Two methods are used in the process of finding conflicts:
  - ```readPackages``` reads all logs stored in the database and returns a map with the OS name as key and an inner map as value. The inner map uses package name and version as key and a list of agents running that package version as value.
  - ```getConflicts``` for each OS name recorded in the system, creates a map of package names as key and lists of versions as value.
