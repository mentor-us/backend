# Install

## Prerequisite

- Jetbrain IntelliJ IDEA Ultimate

  - Install Jetbrain toolbox: https://www.jetbrains.com/toolbox-app/
  - Install IntelliJ Ultimate
  - Apply your educational license: https://www.jetbrains.com/shop/eform/students

- MongoDB:
  - Install MongoDB Community Edition: https://www.mongodb.com/docs/manual/administration/install-community/

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
