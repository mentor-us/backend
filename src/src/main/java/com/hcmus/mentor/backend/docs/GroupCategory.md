### List group categories (all)

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/group-categories' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": "63eba623daac4a44cf48b795",
      "name": "abc",
      "description": "abcd",
      "status": true,
      "iconUrl": "abc.com"
    }
  ],
  "returnCode": 200
}
```

### Get a group category

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/group-categories/123' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--data-raw ''
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63eba623daac4a44cf48b795",
    "name": "abc",
    "description": "abcd",
    "status": true,
    "iconUrl": "abc.com"
  },
  "returnCode": 200
}
```

### Add a group category(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/group-categories' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "abc",
    "description": "abcd",
    "status": true,
    "iconUrl": "abc.com"
}'
```

- Response:
  - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63eba79edaac4a44cf48b796",
    "name": "abc",
    "description": "abcd",
    "status": true,
    "iconUrl": "abc.com"
  },
  "returnCode": 200
}
```
- Duplicated (by name):
```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 407
}
```

- Invalid permission:
```json
{
  "success": true,
  "message": "Invalid permission",
  "data": null,
  "returnCode": 100
}
```

- Not enough required fields:
```json
{
  "success": true,
  "message": "Not enough required fields",
  "data": null,
  "returnCode": 411
}
```

### Update a group category(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/group-categories' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "abc",
    "description": "abcd",
    "status": true,
    "iconUrl": "abc.com"
}'
```

- Response:
    - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63eba79edaac4a44cf48b796",
    "name": "abc",
    "description": "abcd",
    "status": true,
    "iconUrl": "abc.com"
  },
  "returnCode": 200
}
```
- Not found:
```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 404
}
``` 

- Invalid permission:
```json
{
  "success": true,
  "message": "Invalid permission",
  "data": null,
  "returnCode": 100
}
```

- Not enough required fields:
```json
{
  "success": true,
  "message": "Not enough required fields",
  "data": null,
  "returnCode": 411
}
```

### Delete a group category(Admin)

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/group-categories/63eba59095b35d3c0cbc92f3' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
```

- Response:
  - Success:
```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63eba59095b35d3c0cbc92f3",
    "name": "abc",
    "description": "abcd",
    "status": false,
    "iconUrl": "abc.com"
  },
  "returnCode": 200
}
```
- Not found:
```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 404
}
``` 

- Invalid permission:
```json
{
  "success": true,
  "message": "Invalid permission",
  "data": null,
  "returnCode": 100
}
```

- Not enough required fields:
```json
{
  "success": true,
  "message": "Not enough required fields",
  "data": null,
  "returnCode": 411
}
```

