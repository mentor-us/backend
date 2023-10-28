# Running the Application Locally

- You must provide the `application.yml` at `/src/src/main/resources`

- Go to `/src/src/main/resources`
- Choose one of the following options to set up your environment:

  1. Using 1Password Cli (for the developer team only):

  - Linux and macOS: command `APP_ENV=<env> op inject -i application.yml.tpl -o application.yml`
  - PowerShell: `$Env:APP_ENV = "<env>" && op inject -i application.yml.tpl -o application.yml`
  - For more information, refer to: [Inject credentials using the 1Password CLI in different environments](https://developer.1password.com/docs/cli/secrets-config-files/#step-3-differentiate-between-environments)

  2. Ping @hieucckha for credential file

> **Note:** In the Minio section, you must provide the necessary information yourself from [INSTALL.md](../INSTALL.md#configuration-minio).

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
