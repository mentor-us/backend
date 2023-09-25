# Install

## Prerequisite

- Jetbrain IntelliJ IDEA Ultimate

  - Install Jetbrain toolbox: https://www.jetbrains.com/toolbox-app/
  - Install IntelliJ Ultimate
  - Apply your educational license: https://www.jetbrains.com/shop/eform/students

- MongoDB:
  - Install MongoDB Community Edition: https://www.mongodb.com/docs/manual/administration/install-community/

## Install SDK

`Require: JDK > 17`

Step to install JDK on intellij

1. Open /src folder by intellij
2. Choose `File -> Project Structure`, Project Structure dialog will open
3. On option `SDK`: `Add SDK -> Download JDK`,
4. Choose version 20, vendor Azul Zulu Community
5. Click download button

## Install database

1. Open terminal
2. Type `mongosh`
3. Inside mongosh run this

```
use admin

db.createUser({
  "user": "root",
  "pwd": "password",
  "roles": [
    "userAdmin"
  ]
})

db.createCollection("mentordb")
```

## Deploy to server

`Require JDK > 17`

1. Go to src 
2. Run below command

```bash
./mvnw clean
./mvnw package
docker stop mentorus-backend && docker rm mentorus-backend
docker build -t mentorus-backend:0.0.1 .
docker run -d --restart always --name mentorus-backend --network mentorus-network -p 7000:8080 -p 7001:8085 mentorus-backend:0.0.1
```
