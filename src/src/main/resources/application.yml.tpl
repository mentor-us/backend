server-socket:
  host: 0.0.0.0
  port: 8085

server:
  tomcat:
    relaxed-query-chars: ["{", "}"]
  forward-headers-strategy: framework

s3-settings:
  bucket-name: "op://mentorus/secret/$APP_ENV/s3-bucket-name"
  access-key: "op://mentorus/secret/$APP_ENV/s3-access-key"
  secret-key: "op://mentorus/secret/$APP_ENV/s3-private-key"
  region-name: "op://mentorus/secret/$APP_ENV/s3-region-name"
  service-url: "op://mentorus/secret/$APP_ENV/s3-service-url"
  force-path-style: "op://mentorus/secret/$APP_ENV/s3-force-path-style"

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
    username: "op://mentorus/secret/$APP_ENV/mail-username"
    password: "op://mentorus/secret/$APP_ENV/mail-password"
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
      uri: "op://mentorus/secret/$APP_ENV/mongodb-uri"
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: "op://mentorus/secret/$APP_ENV/google-client-secret"
            client-secret: "op://mentorus/secret/$APP_ENV/google-client-secret"
            redirect-uri: "{baseUrl}/oauth2/callback/{registrationId}"
            scope: openid,email,profile
          azure:
            client-id: "op://mentorus/secret/$APP_ENV/azure-client-id"
            client-secret: "op://mentorus/secret/$APP_ENV/azure-client-secret"
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

azure:
  active-directory:
    jwt-connect-timeout: 6789
    jwt-read-timeout: 6789

app:
  googleDrive:
    rootId: 1Nz9fca4S7eAVAogMBmLIP1Om0Bp3puIK
  auth:
    tokenSecret: 04ca023b39512e46d0c2cf4b48d5aac61d34302994c87ed4eff225dcf3b0a218739f3897051a057f9b846a69ea2927a587044164b7bae5e1306219d50b588cb1
    tokenExpirationMsec: 10800000
    refreshTokenExpirationMsec: 259200000
    allowedDomains: student.hcmus.edu.vn,fit.hcmus.edu.vn
  cors:
    allowedOrigins: http://localhost:3000,http://localhost:8080
  oauth2:
    # After successfully authenticating with the OAuth2 Provider,
    # we'll be generating an auth token for the user and sending the token to the
    # redirectUri mentioned by the client in the /oauth2/authorize request.
    # We're not using cookies because they won't work well in mobile clients.
    authorizedRedirectUris:
      - http://localhost:3000/oauth2/redirect
      - https://admin-mentorus.hieucckha.me/auth/redirect
      - https://mentor.fit.hcmus.edu.vn/auth/redirect
      - mentorus://oauth2/redirect

springdoc:
  packages-to-scan: com.hcmus.mentor.backend.controller
  swagger-ui:
    operations-sorter: alpha
    tags-sorter: alpha
