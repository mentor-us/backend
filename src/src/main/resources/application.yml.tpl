server-socket:
  host: 0.0.0.0
  port: 8085

server:
  tomcat:
    relaxed-query-chars: ["{", "}"]
  forward-headers-strategy: framework

springdoc:
  packages-to-scan: com.hcmus.mentor.backend.web.controller
  swagger-ui:
    operations-sorter: alpha
    tags-sorter: alpha

app:
  jwt:
    secret-key: op://mentorus/backend-$APP_ENV/jwt/secret-key
  cors:
    allowedOrigins: http://localhost:3000,http://localhost:8080
  oauth2:
    # After successfully authenticating with the OAuth2 Provider,
    # we'll be generating an auth token for the user and sending the token to the
    # redirectUri mentioned by the client in the /oauth2/authorize request.
    # We're not using cookies because they won't work well in mobile clients.
    authorizedRedirectUris: op://mentorus/backend-$APP_ENV/jwt/authorized-redirect-uris

s3-settings:
  bucket-name: op://mentorus/backend-$APP_ENV/s3/bucket-name
  access-key: op://mentorus/backend-$APP_ENV/s3/access-key
  secret-key: op://mentorus/backend-$APP_ENV/s3/secret-key
  region-name: op://mentorus/backend-$APP_ENV/s3/region-name
  service-url: op://mentorus/backend-$APP_ENV/s3/service-url
  force-path-style: op://mentorus/backend-$APP_ENV/s3/force-path-style

spring:
  mvc:
    pathmatch:
      matching-strategy: ant_path_matcher
  servlet:
    multipart:
      max-file-size: 5MB
      max-request-size: 5MB
  mail:
    host: smtp.gmail.com
    port: 587
    username: op://mentorus/backend-$APP_ENV/mail/username
    password: op://mentorus/backend-$APP_ENV/mail/password
    protocol: smtp
    tls: true
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
          ssl:
            trust: smtp.gmail.com
  data:
    mongodb:
      uri: op://mentorus/backend-$APP_ENV/mongodb-uri
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: op://mentorus/backend-$APP_ENV/oauth2/google-client-id
            client-secret: op://mentorus/backend-$APP_ENV/oauth2/google-client-secret
            redirect-uri: "{baseUrl}/oauth2/callback/{registrationId}"
            scope: openid,email,profile
          azure:
            client-id: op://mentorus/backend-$APP_ENV/oauth2/azure-client-id
            client-secret: op://mentorus/backend-$APP_ENV/oauth2/azure-client-secret
            redirect-uri: "{baseUrl}/oauth2/callback/{registrationId}"
            authorization-grant-type: authorization_code
            scope: openid,email,profile
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth?prompt=select_account
          azure:
            authorization-uri: https://login.microsoftonline.com/common/oauth2/v2.0/authorize
            token-uri: https://login.microsoftonline.com/common/oauth2/v2.0/token
            jwk-set-uri: https://login.microsoftonline.com/common/discovery/v2.0/keys
