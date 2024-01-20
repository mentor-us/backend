### List all groups

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/groups' \
--header 'Authorization: Bearer <access-token>' \

Default value:
  name = "",
  mentorEmail = "",
  menteeEmail = "",
  page = 0, 
  pageSize = 25
  type = "" // type can be "mentor" or "mentee" or ""(both)
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": {
    "totalItems": 4,
    "totalPages": 1,
    "groups": [
      {
        "id": "63f03e4260a7156f78b1fbbd",
        "name": "abcdefghijklmnopqrs",
        "createdDate": "2023-02-02T08:29:02.413+00:00",
        "updatedDate": "2023-02-18T02:56:02.154+00:00",
        "mentors": [],
        "mentees": [],
        "groupCategory": "63ec5e0e5e77c34f1c3b4867",
        "status": true,
        "timeStart": "2023-02-02T01:29:02.413+00:00",
        "timeEnd": "2023-02-10T01:35:02.413+00:00",
        "duration": "PT192H6M"
      },
      {
        "id": "63f084727f2f750707228846",
        "name": "abcdefghijklmnopqrst",
        "createdDate": "2023-02-02T08:29:02.413+00:00",
        "updatedDate": "2023-02-18T07:55:30.467+00:00",
        "mentors": [],
        "mentees": [],
        "groupCategory": "63ec5e0e5e77c34f1c3b4867",
        "status": true,
        "timeStart": "2023-02-02T08:29:02.413+00:00",
        "timeEnd": "2023-02-10T08:35:02.413+00:00",
        "duration": "PT192H"
      },
      {
        "id": "63f08cae0f73f25591038034",
        "name": "abcdefghijklmnopqrstu",
        "createdDate": "2023-02-02T08:29:02.413+00:00",
        "updatedDate": "2023-02-18T08:30:38.304+00:00",
        "mentors": [],
        "mentees": [],
        "groupCategory": "63ec5e0e5e77c34f1c3b4867",
        "status": true,
        "timeStart": "2023-02-02T08:29:02.413+00:00",
        "timeEnd": "2023-02-10T08:35:02.413+00:00",
        "duration": "PT192H6M"
      },
      {
        "id": "63f09bfc445a487916c8db23",
        "name": "abcdefghijklmnopqrstuv",
        "createdDate": "2023-02-02T08:29:02.413+00:00",
        "updatedDate": "2023-02-18T09:35:56.355+00:00",
        "mentors": [],
        "mentees": [],
        "groupCategory": "63ec5e0e5e77c34f1c3b4867",
        "status": true,
        "timeStart": "2023-02-02T08:29:02.413+00:00",
        "timeEnd": "2023-02-10T08:35:02.413+00:00",
        "duration": "PT192H6M"
      }
    ],
    "currentPage": 0
  },
  "returnCode": 200
}
```

### Create a group(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/groups/' \
--header 'Authorization: Bearer <access-token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "abcd",
    "createdDate": "2023-02-02T08:29:02.413Z",
    "mentorEmails": [
    ],
    "menteeEmails": [
    ],
    "status": true,
    "groupCategory": "63ec5e0e5e77c34f1c3b48678",
    "timeStart": "2023-02-02T08:29:02.413Z",
    "timeEnd": "2023-02-10T08:35:02.413Z"
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63f0a51795a5bb685fd9620f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-18T10:14:47.217+00:00",
    "mentors": [],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T08:29:02.413+00:00",
    "timeEnd": "2023-02-10T08:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Duplicated group:

```json
{
  "success": true,
  "message": "Group name has been duplicated",
  "data": null,
  "returnCode": 207
}
```

- Group Category not exists:

```json
{
  "success": true,
  "message": "Group category not exists",
  "data": null,
  "returnCode": 208
}
```

- Time end before time start:

```json
{
  "success": true,
  "message": "Time end can't be before time start",
  "data": null,
  "returnCode": 209
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
  "returnCode": 211
}
```

- Time start is too far from now:

```json
{
  "success": true,
  "message": "time start is too far from now",
  "data": null,
  "returnCode": 216
}
```

- Time end is too far from time start:

```json
{
  "success": true,
  "message": "time end is too far from time start",
  "data": null,
  "returnCode": 215
}
```

### List group (by name or mentor email or mentee email):

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/groups/find?name=abcd&mentorEmail=haiduc0147@gmail.com&menteeEmail=haiduc0147@gmail.com' \
--header 'Authorization: Bearer <access_token>' \

Default value:
  name = "",
  mentorEmail = "",
  menteeEmail = "",
  page = 0, 
  size = 25
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": {
    "totalItems": 1,
    "totalPages": 1,
    "groups": [
      {
        "id": "63db744e1585497b020152d7",
        "name": "abc",
        "createdDate": "2023-02-02T08:29:02.413+00:00",
        "updatedDate": "2023-02-02T08:29:02.601+00:00",
        "mentors": [
          "63db731f1585497b020152d5"
        ],
        "mentees": [
          "63db731f1585497b020152d5"
        ]
      }
    ],
    "currentPage": 0
  },
  "returnCode": 200
}
```

### Add mentees(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/groups/63e1e6e24530646b949d2a1d/mentees' \
--header 'Authorization: Bearer <access_token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "emails": ["abc@gmail.com", "duc@gmail.com"]
}'
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1e6e24530646b949d2a1d",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T05:51:30.922+00:00",
    "mentors": [
      "63e1e6e24530646b949d2a1c"
    ],
    "mentees": [
      "63e1e6e24530646b949d2a1c",
      "63e1e5df4530646b949d2a1b"
    ],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found:

```json
{
  "success": false,
  "message": "Not found",
  "data": null,
  "returnCode": 204
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

### Add mentors(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentors' \
--header 'Authorization: Bearer <access_token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "emails": ["duc1@gmail.com", "duc@gmail.com"]
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e6e24530646b949d2a1c",
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [
      "63e1e6e24530646b949d2a1c"
    ],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found:

```json
{
  "success": false,
  "message": "Not found",
  "data": null,
  "returnCode": 204
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

### Delete mentee(Admin)

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentees/63e1e6e24530646b949d2a1c' \
--header 'Authorization: Bearer <access_token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
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

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentee:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 213
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

### Delete mentor(Admin)

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentors/63e1e4eb4530646b949d2a19' \
--header 'Authorization: Bearer <access_token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentor:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 214
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

### Promote to mentor(Admin)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentors/63db731f1585497b020152d5' \
--header 'Authorization: Bearer <access_token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18",
      "63db731f1585497b020152d5"
    ],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentee:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 213
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

### Demote to mentee(Admin)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentees/63db731f1585497b020152d5' \
--header 'Authorization: Bearer <access_token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [
      "63db731f1585497b020152d5"
    ],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentor:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 214
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

### Add mentees(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/groups/63e1e6e24530646b949d2a1d/mentees' \
--header 'Authorization: Bearer <access-token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "emails": ["abc@gmail.com", "duc@gmail.com"]
}'
```

- Response:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1e6e24530646b949d2a1d",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T05:51:30.922+00:00",
    "mentors": [
      "63e1e6e24530646b949d2a1c"
    ],
    "mentees": [
      "63e1e6e24530646b949d2a1c",
      "63e1e5df4530646b949d2a1b"
    ],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found:

```json
{
  "success": false,
  "message": "Not found",
  "data": null,
  "returnCode": 204
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

### Add mentors(Admin)

- Request:

```bash
curl --location --request POST 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentors' \
--header 'Authorization: Bearer <access-token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "emails": ["duc1@gmail.com", "duc@gmail.com"]
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e6e24530646b949d2a1c",
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [
      "63e1e6e24530646b949d2a1c"
    ],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found:

```json
{
  "success": false,
  "message": "Not found",
  "data": null,
  "returnCode": 204
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

### Delete mentee(Admin)

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentees/63e1e6e24530646b949d2a1c' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentee:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 213
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

### Delete mentor(Admin)

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentors/63e1e4eb4530646b949d2a19' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentor:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 214
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

### Promote to mentor(Admin)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentors/63db731f1585497b020152d5' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-07T06:12:01.326+00:00",
    "mentors": [
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18",
      "63db731f1585497b020152d5"
    ],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentee:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 213
}
```

### Demote to mentee(Admin)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/groups/63e1ebb1d4659c7b1a584a2f/mentees/63db731f1585497b020152d5' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63e1ebb1d4659c7b1a584a2f",
    "name": "abc",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-18T02:56:02.154+00:00",
    "mentors": [
      "63e1e4eb4530646b949d2a19",
      "63e1e4c64530646b949d2a18"
    ],
    "mentees": [
      "63db731f1585497b020152d5"
    ],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Not found group:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 204
}
```

- Not found mentor:

```json
{
  "success": true,
  "message": null,
  "data": null,
  "returnCode": 214
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

### Get import-groups template

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/groups/import' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:

```json
{
  "path": "src/main/resources/templates/import-groups.xlsx"
}
```

### Import groups(Admin)

- Request:

```bash
curl --location --request GET 'http://localhost:8080/api/groups/import' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": [
    {
      "id": "63f474929bd5147dd289aca7",
      "name": "DATN",
      "createdDate": "2023-02-21T07:36:50.434+00:00",
      "updatedDate": "2023-02-21T07:36:50.434+00:00",
      "mentors": [
        "63f0fa640028fb0840aadc07"
      ],
      "mentees": [
        "63e1e6e24530646b949d2a1c"
      ],
      "groupCategory": "63ec5e0e5e77c34f1c3b4867",
      "status": true,
      "timeStart": "2023-02-02T00:00:00.000+00:00",
      "timeEnd": "2023-02-20T00:00:00.000+00:00",
      "duration": "PT432H"
    },
    {
      "id": "63f474929bd5147dd289aca8",
      "name": "KLTN",
      "createdDate": "2023-02-21T07:36:50.438+00:00",
      "updatedDate": "2023-02-21T07:36:50.438+00:00",
      "mentors": [
        "63f0fa5f0028fb0840aadc06"
      ],
      "mentees": [
        "63db731f1585497b020152d5"
      ],
      "groupCategory": "63eba79edaac4a44cf48b796",
      "status": true,
      "timeStart": "2023-02-02T00:00:00.000+00:00",
      "timeEnd": "2023-02-02T00:00:00.000+00:00",
      "duration": "PT0S"
    }
  ],
  "returnCode": 200
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

Beside that, it has some failure case like creating group(duplicated name, not found group category)(

### Update group(Admin)

- Request:

```bash
curl --location --request PATCH 'http://localhost:8080/api/groups/63f03e4260a7156f78b1fbbd' \
--header 'Authorization: Bearer <access-token>' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "abcde",
    "status": false,
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "timeStart": "2023-02-02T01:29:02",
    "timeEnd": "2023-02-10T01:35:02"
}'
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63f03e4260a7156f78b1fbbd",
    "name": "abcde",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-18T02:56:02.154+00:00",
    "mentors": [],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": false,
    "timeStart": "2023-02-02T01:29:02.000+00:00",
    "timeEnd": "2023-02-10T01:35:02.000+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Group not found:

```json
{
  "success": true,
  "message": "Group not found",
  "data": null,
  "returnCode": 404
}
```

- Group category not exists:

```json
{
  "success": true,
  "message": "Group category not exists",
  "data": null,
  "returnCode": 208
}
```

- Time end before time start:

```json
{
  "success": true,
  "message": "Time end can't be before time start",
  "data": null,
  "returnCode": 209
}
```

- Time start is too far from now:

```json
{
  "success": true,
  "message": "time start is too far from now",
  "data": null,
  "returnCode": 216
}
```

- Time end is too far from time start:

```json
{
  "success": true,
  "message": "time end is too far from time start",
  "data": null,
  "returnCode": 215
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

### Delete group(Admin)

- Request:

```bash
curl --location --request DELETE 'http://localhost:8080/api/groups/63f03e4260a7156f78b1fbbd' \
--header 'Authorization: Bearer <access-token>' \
```

- Response:
    - Success:

```json
{
  "success": true,
  "message": null,
  "data": {
    "id": "63f03e4260a7156f78b1fbbd",
    "name": "abcdefghijklmnopqrs",
    "createdDate": "2023-02-02T08:29:02.413+00:00",
    "updatedDate": "2023-02-18T02:56:02.154+00:00",
    "mentors": [],
    "mentees": [],
    "groupCategory": "63ec5e0e5e77c34f1c3b4867",
    "status": true,
    "timeStart": "2023-02-02T01:29:02.413+00:00",
    "timeEnd": "2023-02-10T01:35:02.413+00:00",
    "duration": "PT192H6M"
  },
  "returnCode": 200
}
```

- Group not found:

```json
{
  "success": true,
  "message": "Group not found",
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
