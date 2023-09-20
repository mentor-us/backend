### List all users:

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/users/all' \
--header 'Authorization: Bearer <access_token>' \
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": "63db731f1585497b020152d5",
      "name": "Thới Hải Đức 1",
      "email": "haiduc0147@gmail.com",
      "imageUrl": "abcd",
      "emailVerified": true,
      "password": "",
      "provider": "google",
      "providerId": "101224525184439385427"
    }
  ],
  "returnCode": 200
}
```

### List users by email:

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/users?email=duc&page=0&size=25' \
--header 'Authorization: Bearer <access_token>' \

Default value:
  email = "",
  page = 0, 
  size = 25
```

- Response:
  - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "totalItems": 1,
    "totalPages": 1,
    "groups": [
      {
        "id": "63db731f1585497b020152d5",
        "name": "Thới Hải Đức 1",
        "email": "haiduc0147@gmail.com",
        "imageUrl": "abcd",
        "emailVerified": true,
        "password": "",
        "provider": "google",
        "providerId": "101224525184439385427"
      }
    ],
    "currentPage": 0
  },
  "returnCode": 200
}
```


### Delete User:

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/users/63db731f1585497b020152d4' \
--header 'Authorization: Bearer <access_token>' \
```

- Response:
  - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63db731f1585497b020152d5",
    "name": "Thới Hải Đức",
    "email": "haiduc0147@gmail.com",
    "imageUrl": null,
    "emailVerified": true,
    "password": "",
    "provider": "google",
    "providerId": "101224525184439385427"
  },
  "returnCode": 200
}
```

- Not Found:
```json
{
  "success": false,
  "message": "No Account",
  "data": null,
  "returnCode": 200
}
```

### Update User:

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/users/63db731f1585497b020152d5/update' \
--header 'Authorization: Bearer <access_token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Thới Hải Đức"
}'
```

- Response:
  - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63db731f1585497b020152d6",
    "name": "abc",
    "email": "abc@gmail.com",
    "imageUrl": "",
    "emailVerified": true,
    "password": "",
    "provider": "google",
    "providerId": "101224525184439385428"
  },
  "returnCode": 200
}
```
  - Not Found:
```json
{
  "success": false,
  "message": "No Account",
  "data": null,
  "returnCode": 104
}
```

### Add User:

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/users/' \
<<<<<<< HEAD
--header 'Authorization: Bearer <access_token>' \
=======
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
>>>>>>> main
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Hai Duc",
    "emailAddress": "duc@gmail.com",
    "role": 0
}'

P/s: role admin = 1
```

- Response:
  - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1e222cc949f41d061d5b2",
    "name": "Hai Duc",
    "email": "duc@gmail.com",
    "imageUrl": "",
    "emailVerified": true,
    "password": "",
    "provider": "local",
    "providerId": ""
  },
<<<<<<< HEAD
  "returnCode": 200
=======
  "returnCode": 100
>>>>>>> main
}
```
- Duplicate user (duplicate email):
```json
{
  "success": false,
  "message": "Duplicate user",
  "data": null,
  "returnCode": 107
}
```
