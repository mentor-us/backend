# Installation

**Prerequisites:**

- JDK 17 or higher (You can choose any distribution of OpenJDK from various vendors):

  - Microsoft OpenJDK: [Installation guide](https://learn.microsoft.com/en-us/java/openjdk/install)
  - Eclipse Temurin: [Installation guide](https://adoptium.net/installation/)
  - Azul Zulu: [Package download](https://www.azul.com/downloads/?package=jdk#zulu)

- MongoDB:

  - Install MongoDB Community Edition: [Installation guide](https://www.mongodb.com/docs/manual/administration/install-community/)

- Minio:

  - Install Minio server: [Installation guide](https://min.io/download)

**Optional**

- JetBrains IntelliJ IDEA Ultimate (For an easier way to run a Spring Boot application):

  - Install JetBrains Toolbox: https://www.jetbrains.com/toolbox-app/
  - Install IntelliJ Ultimate
  - Apply your educational license: [License application](https://www.jetbrains.com/shop/eform/students) (Currently, email addresses ending in @student.hcmus.edu.vn are not eligible)

## Installing JDK

- Please verify that your Java version on your machine is 17 or higher:

```shell
java -version
```

- Example output:

```shell
openjdk version "17.0.8.1" 2023-08-24 LTS
OpenJDK Runtime Environment Zulu17.44+53-CA (build 17.0.8.1+1-LTS)
OpenJDK 64-Bit Server VM Zulu17.44+53-CA (build 17.0.8.1+1-LTS, mixed mode, sharing)
```

## Installing the Database

1. Open the terminal.
2. Run command `mongosh`
3. Inside mongosh, run the following commands:

```shell
use admin
db.createUser({
  "user": "root",
  "pwd": "password",
  "roles": [
    "userAdmin"
  ]
})
db.createCollection("mentordb")
db.user.insertOne({name: "Admin",email:"<YOUR EMAIL>","roles": ["SUPER_ADMIN","ROLE_USER"]})
```

**Note**: Please replace `<YOUR EMAIL>` with your actual email address.

## Next Steps

- Developing the application locally: [Development](docs/Development.md)
- Deploying the application to the server: [Deploy](docs/Deploy.md)
