# Developer information

## Cloning
At the moment, this module relies on an as-yet unreleased module "taskscheduler". This is included as a git submodule, and so the options are _either_ to clone with submodules `git clone <this repo> --recurse-submodules` or after running `git clone` to also run `git submodule init` followed by `git submodule update`.

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

## Running
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
  - A single pushKB can handle multiple pushes (not in Alpha 3)
  - Multiple pushKBs can also be run to allow horizontal scaling (not in Alpha 3)
  - NOTE - Alpha 3 version is NOT scalable vertically or horizontally
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

### Understanding the yml file
The `yml` file listed above can be understood as follows.

- Sources (For V1 this will only be gokbSources)
  - `gokbs`
  	- These are the GOKB servers themselves.
    - They should have a `name` and a `url`
    	- `url` MUST be the root url of the gokb, ie https://gokb.org NOT https://gokb.org/gokb/oai/index)
    	- `name` given will be important below, it is the name stored in PushKBs DB for identification
  - `gokbsources`
  	- These define the "sources" which will be ingested from
    - PushKB uses the scroll API, and so we treat TIPPs and Packages as separate source streams
    - These comprise of a `name`, a `gokb` and a `type`
    	- `name` is a unique name for this source, say "GOKB_PKG".
  			- This will be used to link up sources and destinations below in the pushables section
      	- Purely for organisational reasons
    	- `gokb` must match the	`name` of a GOKB defined above
    	- `type` must be either "PACKAGE" or "TIPP" (You will likely want one of each)
- Destinations (For V1 wthis will only be foliodestinations)
	- `foliotenants`
		- These define individual tenants for a folio system, ie each login which requires data to be pushed to it
    - They comprise of a `name`, `authtype`, `tenant`, `baseurl`, `username` and `password`
    	- `name` is the name for this tenant in the PushKB DB, will be important later
    	- `authtype` must be either "OKAPI" or "NONE". For Eureka I believe authtype "OKAPI" should still suffice as logins are "backwards compatible"
   		- `tenant` is the configured tenant name in FOLIO. For folio-snapshot this would be "diku"
    	- `baseurl` is the access url for external API calls of this folio.
    		- Eventually the structure may split out folio server and folio tenant
      	- That will likely be after this method of bootstrapping is deprecated
    	- `username` and `password` are the login credentials for this folio tenant
  			- Currently these are stored in the database, eventually these should be handled in a nicer way
	- `foliodestinations`
  	- These are the individual "push sites" for a tenant, you will need one for pushing PCIs and one for pushing Packages
    - These comprise of a `name`, a `foliotenant` and a `destinationtype`
    	- `name` is used for linking sources and destinations in the pushable section
      - `foliotenant` must be the `name` of a `foliotetenant` defined in the previous section
      - `destinationtype` must be "PACKAGE" or "PCI"
- Pushables (PushTasks and TemporaryPushTasks, but the latter are not bootstrappable)
	- `pushtasks`
		- These are the link between a source and a destination, allowing pushKB to push the queue of records.
    - Comprised of `transform`, `source` and `destination`
			- `transform` is for defining the JSON schema used to transform source data into destination data
    		- Uses "proteus", a CIIM team tool
      	- As of Alpha 7, this string is irrelevant in bootstrapping, as the transforms are currently hardcoded.
    	- `source` is a named source from the above section
      	- Must align with `source.name`
      - `destination` is a named destination from the above section
				- Must align with `destination.name`
## Env Vars
PushKB accepts multiple env vars

| environment variable                 | accepts                                                                                                                         | description                                                                                                                                                                         | default                                                                                                                             | notes                                                                                                                                                                                                |
|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| MICRONAUT_CONFIG_FILES               | Path string pointing at a YML file                                                                                              | File for bootstrapping as above.                                                                                                                                                    |                                                                                                                                     | This will likely be removed once there is a proper API                                                                                                                                               |
| TASKSCHEDULER_INTERVAL               | A [Duration-parseable-string](https://docs.oracle.com/javase/8/docs/api/java/time/Duration.html#parse-java.lang.CharSequence-)  | This allows configuration of how often the module requests a new task (if concurrency is not full). This means that concurrency of 15 would regularly take 150 seconds to "fill up" | `PT10S` (10 seconds -- the default)                                                                                                 | <ul><li>As of newest SNAPSHOT image this is mandatory, defaulting not behaving as expected</li><li>Not available in Alpha 3</li></ul>                                                                |
| TASKSCHEDULER_REACTIVE_CONCURRENCY   | An integer                                                                                                                      | Configures the number of tasks the singular instance can carry out simultaneously                                                                                                   | 1                                                                                                                                   | <ul><li>As of newest SNAPSHOT image this is mandatory, defaulting not behaving as expected</li><li>Not available in Alpha 3<li>RAM and thread resources need to increase with this setting</li></ul> |                                                                                                               |
| ACCESSIBLE_URL                       | URI String                                                                                                                      | This is to allow the pushes from pushKB to contain information about where to send http requests. May be required for now if externally accessible URL is behind a proxy etc.       | Return from [EmbeddedServer.getUrl()](https://docs.micronaut.io/4.1.4/api/io/micronaut/runtime/server/EmbeddedServer.html#getURL()) | <ul><li>Not available in Alpha 3</li><li>Not mandatory but needed for V1 package sync work</li></ul>                                                                                                 |

## Docker images
 "Alpha 3" as referenced above can be found at https://docker.libsdev.k-int.com/pushkb:1.0-alpha.3
 "SNAPSHOT" version is found at https://docker.libsdev.k-int.com/knowledgeintegration/pushkb:next
## Choosing Alpha
 For initial testing, Alpha 3 is preferred as it is known stable, the ability to get the module up and running and pointing at a Gokb/FOLIO is the first thing to test. From there Alpha 5 introduces scaling and initial API work, but the features are still under heavy construction. As of right this second Alpha 3 is the last tagged alpha released, but SNAPSHOT versions are releasing as expected. These will obviously be less stable than a tagged release.
