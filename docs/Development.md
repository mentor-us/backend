# Running the Application Locally

> Note: if you are from class, please ping the developer to get the credential file (Hiếu Nguyễn (hieucckha@gmail.com))

- Go to `/src/src/main/resources`
- Run this command to initial the credential:
```shell
op inject -i application-common.yml.template -o application-common.yml
op inject -i application-local.yml.template -o application-local.yml
```

> For more information, refer to: [Inject credentials using the 1Password CLI in different environments](https://developer.1password.com/docs/cli/secrets-config-files/#step-3-differentiate-between-environments)

- Add firebase credential file in `/src/main/resources/` with name `mentorus-firebase-adminsdk-20240402.json`

## Using JetBrains IntelliJ Ultimate:

- Open IntelliJ:
  ![Welcome screen](imgs/intellij-screen-welcome.png)
- Open the `src` folder:
  ![Open source folder](imgs/choose-source-backend.png)
- Press the start button to run the application:
  ![Run or Debug](imgs/run-or-debug-program.png)
- The application runs successfully:
  ![Application runs successfully](imgs/application-ran-successfully.png)

> Swagger URL: `http://localhost:8080/swagger-ui/index.html`

## Using the Terminal:

- Open the terminal and navigate to the project folder:
  ![Go to project folder](imgs/go-to-project-folder.png)
- Go to the `src` folder:
  ![Go to src folder](imgs/go-to-src-folder.png)
- Run the command `./mvnw spring-boot:run` (use `mvnw.cmd` for running on Windows):
  ![Command to run the application](imgs/cli-command-run-application.png)
  ![Run successfully](imgs/cli-runs-successfully.png)
