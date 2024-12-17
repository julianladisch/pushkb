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

# Implementor Information

## Infrastructure
- Resources
  - A single instance of PushKB requires roughly 
    - 150mb * (number of concurrent jobs) + 250mb to be safe
    - 2 threads * (number of concurrentjobs) + 2 threads
    - (Numbers subject to change, difficult to test atm)
- Scaling
  - A single pushKB can handle multiple pushes (not in alpha v1)
  - Multiple pushKBs can also be run to allow horizontal scaling (not in alpha v1)
  - NOTE - alpha 1 version is NOT scalable vertically or horizontally
- It also is currently ONLY compatible fully with Ramsons or beyond versions of mod-agreements.
  - Q may work but with significant bugs/degraded performance
- mod-agreements instances will need env var `INGRESS_TYPE=PushKB` configured
  - There should be a significantly lower strain placed on mod-agreements now it is not running the harvest.
  - Locally it has been observed running 30 tenants and accepting pushes to 15 of those in just under 1.5GB RAM.
  - With some investigation, it should be possible to cut the number of mod-agreements instances needed for large numbers of tenants DRAMATICALLY (without increasing RAM per instance) and save a considerable amount of resources.
    - 200 tenants using mod-agreements ingest with 5 tenants per instance and 4GB per instance would use 160GB
    - With PushKB one could deploy 20 instances running 10 pushes each and run 5 mod-agreements tenants each taking on 40 tenants, each using 4GB for a total of (1.75x20) + (4x5) = 55GB
    - Numbers are very rough there, could do with more testing and accurate feedback

## Config
In order to get data into PushKB there will eventually be a protected API (INFORMATION TO FOLLOW HERE) In early alpha versions however there instead must be a mounted `yml` file containing the GOKB(s), folio tenants etc as above, and pointed at with env var `MICRONAUT_CONFIG_FILES`.

## Env Vars
PushKB accepts multiple env vars
- MICRONAUT_CONFIG_FILES
  - Accepts a path string pointing at a YML file for bootstrapping as above
  - This will likely be removed once there is a proper API
- TASKSCHEDULER_INTERVAL
  - Accepts a Duration-parseable-string such as `PT10S` (10 seconds -- the default)
  - This allows configuration of how often the module requests a new task (if concurrency is not full)
    - This means that concurrency of 15 would regularly take 150 seconds to "fill up"
  - Duration string documentation [here](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)
- TASKSCHEDULER_REACTIVE_CONCURRENCY (Not available in alpha 1)
  - Configures the number of tasks the singular instance can carry out simultaneously (default 1)
  - RAM and thread resources need to increase with this setting.