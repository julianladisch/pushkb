# Running as dev
To run the module as a developer, first navigate to `/infrastructure` and run `docker compose up`. This will configure a testing DB with a postgres and a keycloak (WIP, kKC is not in use right now... to be worked on).

Postgres should start on port `6543` to avoid clashes with any local postgres instances for FOLIO or such.

Then use `./gradlew run` to start the module.