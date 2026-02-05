


## Command to Build and Run
	Spring boot and react js app - 
		Build - mvn clean install
		Run  -  java -jar .\target\baseapp-0.0.1-SNAPSHOT.jar
		mvnw spring-boot:run

	
## Rest API Endpoint
	Fetch All Accounts -  http://localhost:8080/api/bankaccounts/
	Fetch Account by Id - http://localhost:8080/api/bankaccounts/1
	post transfer amount - http://localhost:8080/api/bankaccounts/transfer

## React js app endpoint
	http://localhost:8080/

## Push app on  github
	Click on repo icon on left side of VS
	Click on initialize repository
	Set git conf -
		 git config --global user.name "Your Name"
		git config --global user.email "youremail@yourdomain.com"
	Add remote repo  -
		git remote add origin https://github.com/amit-4-kumar/webapp.git
	Click on commit all
	Click on push

## Scan project for static code analysis and Security scan

Maven Project - mvn clean verify sonar:sonar -Dsonar.projectKey=baseapp -Dsonar.host.url=http://localhost:8000 -Dsonar.login=sqp_1d0e5fc2db8d5607c3d2c149cfab971a5de52e6c

Other Project - sonar-scanner.bat -D"sonar.projectKey=baseapp-react" -D"sonar.sources=." -D"sonar.host.url=http://localhost:8000" -D"sonar.login=sqp_32adab78b184c38dc10bc0cd674e86cf07528a7c" -D"sonar.java.binaries=target/classes"

Install sonar scanner - Visit official site - https://redirect.sonarsource.com/doc/download-scanner.html to download the latest version, and add the bin directory to the %PATH% environment variable

## Add lombok to editor
	ex - In visual studio  pree Ctrl + Shift + X to open the extension manager.
	     Search lombok and install and reload visual studio.

## Prompt and service calls for react componet
	ex - Create a responsive React.js component with account selection dropdowns that consumes the transfer API
	Fetch All Accounts -  http://localhost:8080/api/bankaccounts/