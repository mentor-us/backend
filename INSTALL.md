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

## JDK

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

## Initial database

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
use mentordb
db['system-config'].insertMany([
  {
    "_id": "650fa417608f687c69b69b71",
    "name": "Domain hợp lệ",
    "description": "Các domain cho phép đăng nhập trên hệ thống",
    "type": "java.util.ArrayList",
    "key": "valid_domain",
    "value": "[ \"fit.hcmus.edu.vn\", \"student.hcmus.edu.vn\", \"fit.gmail.com.vn\" ]",
    "_class": "com.hcmus.mentor.backend.entity.SystemConfig"
  },
  {
    "_id": "650fa417608f687c69b69b72",
    "name": "Thời gian học tối đa",
    "description": "Thời gian học tối đa của sinh viên",
    "type": "java.lang.Integer",
    "key": "valid_max_year",
    "value": "7",
    "_class": "com.hcmus.mentor.backend.entity.SystemConfig"
  }
])
db.user.insertOne({name: "Admin",email:"<YOUR EMAIL>","roles": ["SUPER_ADMIN","ROLE_USER"]})
```

**Note**: Please replace `<YOUR EMAIL>` with your actual email address.

## Configuration Minio

> Access Policy

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "AWS": ["*"]
      },
      "Action": ["s3:GetObject"],
      "Resource": ["arn:aws:s3:::*"]
    }
  ]
}
```

1. Go to the Minio admin panel, usually located at this URL: `http://localhost:9001`.
2. Log in and create a bucket.
   ![Go to bucket](docs/imgs/install-minio/go-to-bucket.png)
3. Name the bucket and save its name for later.
   ![Name the bucket](docs/imgs/install-minio/name-the-bucket.png)
4. Click on the bucket to view its details.
   ![View detail bucket](docs/imgs/install-minio/view-detail-bucket.png)
5. Edit the access policy and choose 'Custom.'
   ![Edit access policy](docs/imgs/install-minio/edit-poliicy.png)
6. Paste the access policy provided above and save it.
   ![Save policy](docs/imgs/install-minio/save-policy.png)

**Note:** After that you have

- URL of Minio, usually located at this URL: http://localhost:9000
- Name of the bucket (from step 3)
- Access key and secret key (from step 9)

## Next Steps

- Developing the application locally: [Development](docs/Development.md)
- Deploying the application to the server: [Deploy](docs/Deploy.md)
