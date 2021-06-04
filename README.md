# Skyscraper Backend REST API


## Prerequisites
1. [Java SDK 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
2. [Oracle 18c](https://www.oracle.com/database/technologies/oracle18c-windows-180000-downloads.html)

## Configuration
The app configuration can be changed in `shortages-server/src/main/resources/application.properties`

| Key                        | Description           | Default Value                      |
|----------------------------|-----------------------|------------------------------------|
| spring.datasource.url      | the database url      | jdbc:oracle:thin:@localhost:1521:XE |
| spring.datasource.username | the database username | SKYSCRAPER                    |
| spring.datasource.password | the database password | SKYSCRAPER_PWD        |
| file.upload.dir | the folder to store uploaded file and return file | upload |

### Database setup

- goto *docker-db* run `docker-compose up` to startup database, it needs some time when first run (maybe 20 mins).

  If you encounter error:

  ```
  skyscraper-db | Database configuration failed. Check logs under '/opt/oracle/cfgtoollogs/dbca'.
  skyscraper-db | mv: cannot stat '/opt/oracle/product/18c/dbhomeXE/dbs/spfileXE.ora': No such file or directory
  ```

  Please try to increase Docker's [running resources](https://docs.docker.com/config/containers/resource_constraints/) to resolve it. It has been verified with 4 Intel i7 CPU, 10G memory, 1G swap.
- from base folder folder, run `sh ./docker-db/18.4.0/init-db.sh` to create the database.

## Local deployment

1. Set up the database following [Database Setup](#database-setup) section above

2. Make sure configuration is correct per your environment check [Configuration](#configuration) section above

3. Run `mvn clean install` under base folder. Make sure the `shortage-library` is available.

4. Insert mock data (DB tables will be created automatically at first run)

    ```
    cd shortages-server
    mvn exec:java -Dexec.mainClass="scripts.InsertMockData"
    ```

5. Run below command to run the API under base folder, 
    ```
    cd shortages-server
    mvn clean spring-boot:run -DskipTests
    ```

6. The API will be available on http://localhost:8080/

7. Swagger UI is accessible at http://localhost:8080/swagger-ui/index.html#/

## API Docs

<kbd> ![](/img/api.png) </kbd>