### Create a task(Mentor in group)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/tasks' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--header 'Content-Type: application/json' \
--data-raw '{
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02",
    "userIds": ["63db731f1585497b020152d5"],
    "groupId": "63fb86964ab1be0218b6d2f1"
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": {
    "id": "6400afa19116264845087d3a",
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02.000+00:00",
    "assignerId": "63db731f1585497b020152d5",
    "assigneeIds": [
      {
        "userId": "63db731f1585497b020152d5",
        "status": "TO_DO"
      }
    ],
    "parentTask": null,
    "groupId": "63fb86964ab1be0218b6d2f1"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": "Not found group",
  "data": null,
  "returnCode": 501
}
```

- Not found parent task:

```json
{
  "success": true,
  "message": "Not found parent task",
  "data": null,
  "returnCode": 502
}
```

- Not found user in group:

```json
{
  "success": true,
  "message": "Not found user in group",
  "data": null,
  "returnCode": 503
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

- Not enough required fields(title, deadline):

```json
{
  "success": true,
  "message": "Not enough required fields",
  "data": null,
  "returnCode": 511
}
```

### Delete a task(Mentor in group)

```bash
curl --location --request DELETE 'http://localhost:8080/api/tasks/abc' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": {
    "id": "63ff5d7258a5396dedfee089",
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02.000+00:00",
    "assignerId": "63db731f1585497b020152d5",
    "assigneeIds": [
      {
        "userId": "abc",
        "status": "TO_DO"
      }
    ],
    "parentTask": null,
    "groupId": "63fb86964ab1be0218b6d2f1"
  },
  "returnCode": 200
}
```

- Not found task:

```json
{
  "success": true,
  "message": "Not found task",
  "data": null,
  "returnCode": 504
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

### Get a task(Mentor, mentee in group)

```bash
curl --location --request DELETE 'http://localhost:8080/api/tasks/abc' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": {
    "id": "63ff5d7258a5396dedfee089",
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02.000+00:00",
    "assignerId": "63db731f1585497b020152d5",
    "assigneeIds": [
      {
        "userId": "abc",
        "status": "TO_DO"
      }
    ],
    "parentTask": null,
    "groupId": "63fb86964ab1be0218b6d2f1"
  },
  "returnCode": 200
}
```

- Not found task:

```json
{
  "success": true,
  "message": "Not found task",
  "data": null,
  "returnCode": 504
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

### Update a task(Mentor in group)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/tasks/63ff67648dd7fd1f5f03573a' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--header 'Content-Type: application/json' \
--data-raw '{
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02",
    "assignees": [{"userId": "63db731f1585497b020152d5", "status": "TO_DO"}]
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": {
    "id": "6400afa19116264845087d3a",
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02.000+00:00",
    "assignerId": "63db731f1585497b020152d5",
    "assigneeIds": [
      {
        "userId": "63db731f1585497b020152d5",
        "status": "TO_DO"
      }
    ],
    "parentTask": null,
    "groupId": "63fb86964ab1be0218b6d2f1"
  },
  "returnCode": 200
}
```

- Not found parent task:

```json
{
  "success": true,
  "message": "Not found parent task",
  "data": null,
  "returnCode": 502
}
```

- Not found user in group:

```json
{
  "success": true,
  "message": "Not found user in group",
  "data": null,
  "returnCode": 503
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

- Not enough required fields(title, deadline):

```json
{
  "success": true,
  "message": "Not enough required fields",
  "data": null,
  "returnCode": 511
}
```

### Update status(assignee: TO_DO, IN_PROGRESS, DONE)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/tasks/63ff67648dd7fd1f5f03573a/IN_PROGRESS' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--data-raw ''
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": {
    "id": "63ff67648dd7fd1f5f03573a",
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02.000+00:00",
    "assignerId": "63db731f1585497b020152d5",
    "assigneeIds": [
      {
        "userId": "63db731f1585497b020152d5",
        "status": "IN_PROGRESS"
      }
    ],
    "parentTask": null,
    "groupId": "63fb86964ab1be0218b6d2f1"
  },
  "returnCode": 200
}
```

- Not found task:

```json
{
  "success": true,
  "message": "Not found task",
  "data": null,
  "returnCode": 504
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

### Update status by mentor(assignee: TO_DO, IN_PROGRESS, DONE)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/tasks/mentor/63ff67648dd7fd1f5f03573a/status' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
--header 'Content-Type: application/json' \
--data-raw '{
    "emailUserAssigned": "haiduc0147@gmail.com",
    "status": "DONE"
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": {
    "id": "63ff67648dd7fd1f5f03573a",
    "title": "abc",
    "description": "",
    "deadline": "2023-04-07T08:29:02.000+00:00",
    "assignerId": "63db731f1585497b020152d5",
    "assigneeIds": [
      {
        "userId": "63db731f1585497b020152d5",
        "status": "DONE"
      }
    ],
    "parentTask": null,
    "groupId": "63fb86964ab1be0218b6d2f1"
  },
  "returnCode": 200
}
```

- Not found task:

```json
{
  "success": true,
  "message": "Not found task",
  "data": null,
  "returnCode": 504
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

### Get task of current user

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/tasks/user' \
--header 'Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI2M2RiNzMxZjE1ODU0OTdiMDIwMTUyZDUiLCJpYXQiOjE2NzUzMjYyMzksImV4cCI6MTY4Mzk2NjIzOX0.kWxu4moS5awB1tLylutgwvrDYxYFf9wr83lI62VFve0LPWRAwola5D9zhJVIZq_lvizuOoCO4M6C4nQ4IA7VXg' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": "",
  "data": [
    {
      "id": "63ff67648dd7fd1f5f03573a",
      "title": "abc",
      "description": "",
      "deadline": "2023-04-07T08:29:02.000+00:00",
      "assignerId": "63db731f1585497b020152d5",
      "assigneeIds": [
        {
          "userId": "63db731f1585497b020152d5",
          "status": "DONE"
        }
      ],
      "parentTask": null,
      "groupId": "63fb86964ab1be0218b6d2f1"
    },
    {
      "id": "63ff67878dd7fd1f5f03573b",
      "title": "abc",
      "description": "",
      "deadline": "2023-04-07T08:29:02.000+00:00",
      "assignerId": "63db731f1585497b020152d5",
      "assigneeIds": [
        {
          "userId": "63db731f1585497b020152d5",
          "status": "TO_DO"
        }
      ],
      "parentTask": null,
      "groupId": "63fb86964ab1be0218b6d2f1"
    },
    {
      "id": "6400afa19116264845087d3a",
      "title": "abc",
      "description": "",
      "deadline": "2023-04-07T08:29:02.000+00:00",
      "assignerId": "63db731f1585497b020152d5",
      "assigneeIds": [
        {
          "userId": "63db731f1585497b020152d5",
          "status": "TO_DO"
        }
      ],
      "parentTask": null,
      "groupId": "63fb86964ab1be0218b6d2f1"
    }
  ],
  "returnCode": 200
}
```

