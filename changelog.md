# Changelog

## Version 1.0.0-alpha.5

### Additions
* [General]
	* Added metadata to pushes, namely pushableId and pushKbUrl, the latter of which comes from an environment variable ACCESSIBLE_URL

### Fixes
* [General]
	* Fixed wrong env var in application.yml

## Version 1.0.0-alpha.4

### Additions
* [General]
	* API (WIP)
	* scaling

### Changes
* [Build]
	* Bump proteus version to 3.0.0 (Seems to be working right now)
	* Small tweak to defaults on taskscheduler stuff
	* Accidental addition of quote marks into config variables where they don't belong
	* Include taskscheduler environment variable passthroughs in application.yml
* [Chore]
	* Changelog - Generate the changelog
	* Tweaks to spec (using new proteus features) and comment tweaks
	* Taskscheduler minor  commit bump
	* Taskscheduler logging
	* Update .gitlab-ci.yml file to force submodules to HTTPS
* [Docs]
	* env var README update
	* README section for implementor information

### Fixes
* [General]
	* FolioDestination Transactions

## Version 1.0.0-alpha.3

### Changes
* [Chore]
	* Changelog - Generate the changelog

### Fixes
* [General]
	* ProteusService was reading from path not classpath... fixed this with ResourceLoader

## Version 1.0.0-alpha.2

### Changes
* [Chore]
	* Added local_docker.env to gitignore and tweaked README

## Version 1.0.0-alpha.1

### Additions
* [General]
	* PushTasks for Package AND PCI both work with requisite transforms used
	* Config based bootstrapping
	* TemporaryPushTask
	* lastIngestStarted/lastIngestCompleted
	* folio_push
	* Folio Auth
	* GokbApiClient
	* Count for sourceRecordFeed
	* Errors
	* Error handling -- kinda
	* ClassConverters
	* PushTask
	* Move to port 8060
	* Attempt to chunk record "sending" -- WIP
	* UUID5
	* Destination
	* Proteus hooked up to database JsonNode!
	* Proteus
	* Refactors, timestamp
	* Save or update
	* LimitRate in place
	* Scroll
	* Built in experimental count
	* SourceRecord
	* GoKBAPIClient
	* GoKB page (WIP!!)
	* GoKB Fetch
	* Security first attempt
	* Keycloak 23
	* Initial commit

### Changes
* [Build]
	* Whoopsie, tweak to Proteus services had build implications
	* max-content-length
	* Added application-development.yml
	* Commented problem dep
* [Chore]
	* Changelog - Generate the changelog
	* Try without the specific file.
	* Build file
	* Comments and whitespacing
	* COnfig changes for env var exposure
	* Bump commit having added DOCKER_IMAGE build variable
	* updated
	* updated roadmap
	* Specifically add *.txt to gitignore
	* Swap back to Gokb full Bootstraps
	* Comment
	* Tweaking scheduling service, keeping some commented out
	* Added some statics to FOLIO API Client
	* Tweaked logging
	* ProteusService tweak
	* Change bootstraps file over to env vars to avoid fiddling on every reset (This will eventually be removed anyway)
	* Logging tweaks (Not really important rn)
	* Make infrastructure docker-compose params explicit
	* Return "Primary function" to ingest
	* Turn off noisy bootstrap logging
	* DB backups
	* Swap to multiple record transform
	* Logging changes
	* Added time logging, and attempted an error handling -- seems to do nothing
	* Removed unused method
	* Tweaks.
	* Removed comment
	* Quick rejig and tidy
	* Infrastructure change to match application-development tweak
	* Turn off security
	* Keycloak 16.1.0
	* Testing ID Autopopulation
	* buildSrc directory added, build.gradle tweaked
* [Ci]
	* Move infrastructure postgres to 6543 (and dev application yml) to avoid clases with folio instances
* [Docs]
	* README changes for future Ethan
* [Refactor]
	* Attempt to tweak how points work across gokb source feed, as they...
	* Folio Destination dignificant imrpovements
	* Tweaks to relative vs absolute path Gokb Scroll
	* Ensure default httpClient from application.yml
	* FolioApiClient cleanup
	* Moved error handling into separate utility function
	* Swap out Optional.map... still won't compile
	* Scheduling service cleanup
	* Interactions file layout
	* We no longer need to pass all the sourceRecords around, flattening instead to just earliest seen and latest seen
	* Code cleanup
	* Tweak PushService for performance/clarity
	* ProteusService
	* Refactor ProteusService
	* Interactions
	* Login tweak
	* Logging errors
	* PushService
	* Rewire SourceRecord ingest
	* Naive GOKB Feed fetching working
	* General ClassAttributeConverter
	* Fix generics and Bootstrapping
	* Sources Bootstrap
	* Bootstrap
	* Rework Boostrapping use cases
	* WORK IN PROGRESS -- Refactor to interfaces
	* Builder cleanup
	* Moved Service logic out of repository
	* Moved from core
	* Package changes
	* Remove DCB stuffs
* [Style]
	* Whitespace change
* [Wip]
	* Reworked to be more reactive in style
	* CHanged the model as there is no need to have GOKB record.

### Fixes
* [General]
	* Fixes... FolioApiClient now works!
	* Whoopsie in GokbAPIClient
	* Make use of Cookie expiry Instant if we have it to hand
	* Timeouts
	* Join
	* Remove script
	* Stupid mistake