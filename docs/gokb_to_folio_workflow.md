# GOKB -> FOLIO data workflow

This document aims to capture in depth the steps required to set up GOKB data ingest and pushes to FOLIO tenants

## FOLIO Setup

In order to set up a FOLIO environment to accept data from PushKB, a few things are necessary. The current
implementation assumes a running version of mod-agreements from Sunflower or beyond. Whilst some of the endpoints are
available in Ramsons, Sunflower is suggested for what will become version 1.0.0 of PushKB.

- mod-agreements v7.2.x or higher (see above)
	- The environment variable `INGRESS_TYPE=PushKB` set for all instances
		- This will turn off the "harvest" operations
- A system user with permission `erm.pushkb.manage` per tenant that data needs to be pushed to.
	- This permission grants the logged in user access to the `/erm/pushKB*` endpoints, which is one method of ingress for
		data into the local KB.
	- The credentials for these system users will end up being stored in PushKB's secrets vault, and thus granting access
		to FOLIO API for PushKB.

## GOKB set up

PushKB assumes that it is pulling from a GOKB which exposes
the [OpenSearch API](https://github.com/openlibraryenvironment/gokb/wiki/Opensearch-API).

## PushKB configuration steps

Once FOLIO and GOKB are ready to go, it's time to start configuring PushKB via the API. This _assumes_ that PushKB has
been configured to work with a Keycloak authentication and Vault setup as detailed in the README.

### GOKB step

PushKB must first be configured to start pulling data in from GOKB. In order to do this, a `POST` to
`/sources/gokbsource` is required, configuring a `Gokb` object (which is a named GOKB implementation, consisting
of a baseUrl), specific information about the kind of GokbSource in hand, and a name. For basic FOLIO functionality,
BOTH a `PACKAGE` and `TIPP` GokbSource must be created and point at the same Gokb object.

Example POST:

```json
{
	"gokbSourceType": "PACKAGE",
	"gokb": {
		"name": "GOKB_TEST",
		"baseUrl": "https://gokbt.gbv.de"
	},
	"name": "GOKBT_PKG"
}
```

Once both of these are set up (one for PACKAGE and one for TIPP), PushKB will create DutyCycleTask objects for ingesting
the source records. These can be found by hitting the `/dutycycletasks` endpoint, and the response should contain two
tasks like:

```json
[
	{
		"id": "276cc20c-b273-5700-a0f2-0d9f586988ff",
		"taskStatus": "IDLE",
		"reference": "35956c55-6aa1-539a-8c45-27000c4853c5",
		"taskType": "REACTIVE",
		"taskInterval": 3600000,
		"taskBeanName": "IngestScheduledTask",
		"failedAttempts": 0,
		"additionalData": {
			"source": "35956c55-6aa1-539a-8c45-27000c4853c5",
			"source_class": "com.k_int.pushKb.interactions.gokb.model.GokbSource"
		}
	},
	{
		"id": "e8d60bc6-6f02-5773-b8b6-c481c1594d6e",
		"taskStatus": "IDLE",
		"reference": "cbb3858e-c751-5bfa-bc2c-543a2335a673",
		"taskType": "REACTIVE",
		"taskInterval": 3600000,
		"taskBeanName": "IngestScheduledTask",
		"failedAttempts": 0,
		"additionalData": {
			"source": "cbb3858e-c751-5bfa-bc2c-543a2335a673",
			"source_class": "com.k_int.pushKb.interactions.gokb.model.GokbSource"
		}
	}
]
```

These are the tasks responsible for managing the continuous ingest of source records into the system. While ingesting, a
PushKB will show logs that look something like:

```
17:28:42.460 [reactor-tcp-epoll-10] INFO  c.k.p.i.g.services.GokbFeedService - GokbFeedService::fetchSourceRecords called for GokbSource: GokbSource(id=cbb3858e-c751-5bfa-bc2c-543a2335a673, gokbSourceType=TIPP, gokb=Gokb(id=1098b5bd-c14e-51e2-871a-74b39fb286fa, baseUrl=https://gokbt.gbv.de, name=GOKB_TEST), name=GOKBT_TIPP, pointer=2022-04-28T16:34:09Z, lastIngestStarted=2026-02-18T17:28:42.452757018Z, lastIngestCompleted=null)
17:28:42.462 [reactor-tcp-epoll-10] INFO  c.k.p.i.g.services.GokbFeedService - LOGDEBUG RAN FOR SOURCE(cbb3858e-c751-5bfa-bc2c-543a2335a673, TIPP) AT: 2026-02-18T17:28:42.462856320Z
17:28:42.462 [reactor-tcp-epoll-10] DEBUG c.k.p.i.gokb.GokbApiClient - CHANGEDSINCE: 2022-04-28T16:34:09Z
17:28:42.463 [reactor-tcp-epoll-10] INFO  c.k.p.i.gokb.GokbApiClient - SENDING SCROLL REQUEST WITH CHANGEDSINCE: 2022-04-28T16:34:09Z AND SCROLL ID: null
...
17:29:01.406 [reactor-tcp-epoll-9] INFO  c.k.p.storage.SourceRecordRepository - Record with id(84d0a39c-2449-5431-9eb2-532f7eb049da) already exists, updating
17:29:01.406 [reactor-tcp-epoll-10] INFO  c.k.p.storage.SourceRecordRepository - Record with id(e2120c99-a8e3-5366-9b36-fd8889398ae3) already exists, updating
17:29:01.406 [reactor-tcp-epoll-8] INFO  c.k.p.storage.SourceRecordRepository - Record with id(a0627dde-bd74-561d-bdc3-2dc5f86f856d) already exists, updating
17:29:01.406 [reactor-tcp-epoll-7] INFO  c.k.p.storage.SourceRecordRepository - Record with id(c37ad502-9061-5fc4-b854-c3047cbdd16f) already exists, updating
17:29:01.407 [reactor-tcp-epoll-4] INFO  c.k.p.storage.SourceRecordRepository - Record with id(4dc8b495-4574-56f6-af8b-1edaa33e5cdd) already exists, updating

```
