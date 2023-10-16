## Deploy to server

`Require JDK > 17`

1. Go to src
2. Run below command

```shell
./mvnw clean
./mvnw package
docker stop mentorus-backend && docker rm mentorus-backend
docker build -t mentorus-backend:0.0.1 .
docker run -d --restart always --name mentorus-backend --network mentorus-network -p 7000:8080 -p 7001:8085 mentorus-backend:0.0.1
```
