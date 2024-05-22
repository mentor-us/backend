# Installation

**Prerequisites:**

- JDK 21 or higher (You can choose any distribution of OpenJDK from various vendors):

  - Microsoft OpenJDK: [Installation guide](https://learn.microsoft.com/en-us/java/openjdk/install)
  - Eclipse Temurin: [Installation guide](https://adoptium.net/installation/)
  - Azul Zulu: [Package download](https://www.azul.com/downloads/?package=jdk#zulu)

- Postgres 15:

  - Install postgres database: [Installation guide](https://www.postgresql.org/download/) or [Docker](https://hub.docker.com/_/postgres)

- Minio:

  - Install Minio server: [Installation guide](https://min.io/download) or [Docker](https://min.io/docs/minio/container/index.html)

- Firebase credential (Admin SDK).

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

- Connect to postgres, run the application (MentorUS backend) to automation apply the migration, create the database name `MentorUS` and run this query below:

```sql
-- System Config
INSERT INTO "public"."system-config" ("id", "name", "description", "type", "key", "value")
VALUES ('1', 'Domain hợp lệ', 'Các domain cho phép đăng nhập trên hệ thống', 'java.util.ArrayList', 'valid_domain', '["fit.hcmus.edu.vn","student.hcmus.edu.vn","fit.gmail.com.vn"]');
VALUES ('2', 'Thời gian học tối đa', 'Thời gian học tối đa của sinh viên', 'java.lang.Integer', 'valid_max_year', '7');

-- Admin User
INSERT INTO "public"."users" ("id", "name", "email", "image_url", "wallpaper", "email_verified", "password", "provider", "provider_id", "status", "phone", "birth_date", "created_date", "training_point", "has_english_cert", "studying_point", "initial_name", "gender", "updated_date")
VALUES (gen_random_uuid(), 'Admin', '<email-of-admin>', 'https://i.pravatar.cc/150?img=3', '', 't', '', 'local', '', 't', '', NULL, current_timestamp, 0, NULL, '0', NULL, 'MALE', current_timestamp);

-- Role for admin
INSERT INTO "public"."user_roles" ("user_id", "roles")
  SELECT id, 1
  FROM "public"."users"
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
