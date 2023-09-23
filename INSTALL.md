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
