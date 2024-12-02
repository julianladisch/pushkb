# Developer information

## Bootstrapping
As a temporary measure, Sources, Destinations and PushTasks must be bootstrapped into the module by way of `micronaut.config.files` (Or running with a custom profile yml)

The `.yml` file should take the form
```
### SOURCES ###
## GOKB ##
sources:
  gokbs:
    -
      name: EXAMPLE_GOKB
      url: https://the-gokb-url.org
  gokbsources:
    -
      name: EXAMPLE_GOKB_PKG
      gokb: EXAMPLE_GOKB
      type: PACKAGE
    -
      name: EXAMPLE_GOKB_TIPP
      gokb: EXAMPLE_GOKB
      type: TIPP


### DESTINATIONS ###
## FOLIO ##
destinations:
  foliotenants:
    -
      name: EXAMPLE_TENANT
      authtype: OKAPI
      tenant: tenant-name
      baseurl: http://the-folio-url.com
      user: usernamegoeshere
      password: passwordgoeshere
  foliodestinations:
    -
      name: EXAMPLE_FOLIO_PACKAGE
      foliotenant: EXAMPLE_TENANT
      destinationtype: PACKAGE
    -
      name: EXAMPLE_FOLIO_PCI
      foliotenant: EXAMPLE_TENANT
      destinationtype: PCI


### PUSHABLES ###
pushables:
  pushtasks:
    -
      transform: example_transform
      source: EXAMPLE_GOKB_PKG
      destination: EXAMPLE_FOLIO_PACKAGE
    -
      transform: example_transform
      source: EXAMPLE_GOKB_TIPP
      destination: EXAMPLE_FOLIO_PCI
```

## Running as dev
To run the module as a developer, first navigate to `/infrastructure` and run `docker compose up`. This will configure a testing DB with a postgres and a keycloak (WIP, kKC is not in use right now... to be worked on).

Postgres should start on port `6543` to avoid clashes with any local postgres instances for FOLIO or such.

Then use `./gradlew run` to start the module.

## Migrations
Migrations in PushKB are handled by Flyway.
### Naming
Migration files are usually named in the following way `V(migration_number)__name_for_migration_file` as per Flyway migration versioning, see https://documentation.red-gate.com/fd/migrations-184127470.html

### Strategy
The aim is to keep migration files relatively minimal. Flyway will handle the running in increasing order, we should aim to keep migration files small so that they are easily parseable by a dev scanning through them, and easy to see what each file is doing. Also allows easier use of "undo" migrations should they become necessary.
