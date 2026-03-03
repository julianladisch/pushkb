# PushKB

PushKB is a microservice designed to allow the pushing of data from a source to a destination, with the initial use case
being pushing from GOKB to FOLIO. It is designed to be flexible and extensible, allowing for the addition of new
sources, destinations and transforms as needed. It is also designed to be scalable, allowing for multiple pushes to be
handled simultaneously per instance and for multiple instances to be run if needed.

# Implementor Information

This section is split, the first half is about configuration as a quick guide, the latter stage is a more detailed
workflow for setup.

## Docker images

"Alpha 3" as referenced below can be found at `docker.libsdev.k-int.com/knowledgeintegration/pushkb:1.0-alpha.3`
"SNAPSHOT" version is found at `docker.libsdev.k-int.com/knowledgeintegration/pushkb:next`. The alpha builds did not
continue to be released properly past alpha.3, and so it is recommended to use the `-SNAPSHOT` images for feature
testing, and final marked releases for production use. As of 2026-02-09, `v1.0.0` is not yet released, and so the
`-SNAPSHOT` images are the best way to run this.

## Infrastructure

- Resources
	- A single instance of PushKB requires roughly
		- 150mb * (number of concurrent jobs) + 250mb to be safe
		- 2 threads * (number of concurrentjobs) + 2 threads
		- (Numbers subject to change, difficult to test atm)
- Scaling
	- A single pushKB can handle multiple pushes
	- Multiple pushKBs can also be run to allow horizontal scaling
	- NOTE - Alpha 3 version is NOT scalable vertically or horizontally
- It also is currently ONLY compatible fully with Ramsons or beyond versions of mod-agreements.
	- Q may work but with significant bugs/degraded performance
- mod-agreements instances will need env var `INGRESS_TYPE=PushKB` configured
	- There should be a significantly lower strain placed on mod-agreements now it is not running the harvest.
	- Locally it has been observed running 30 tenants and accepting pushes to 15 of those in just under 1.5GB RAM.
	- With some investigation, it should be possible to cut the number of mod-agreements instances needed for large
		numbers of tenants DRAMATICALLY (without increasing RAM per instance) and save a considerable amount of resources.
		- 200 tenants using mod-agreements ingest with 5 tenants per instance and 4GB per instance would use 160GB
		- With PushKB one could deploy 20 instances running 10 pushes each and run 5 mod-agreements tenants each taking on
			40 tenants, each using 4GB for a total of (1.75x20) + (4x5) = 55GB
		- Numbers are very rough there, could do with more testing and accurate feedback
- Keycloak
	- PushKB assumes a Keycloak realm has been configured for it.
	- It will then use `client-credentials` authentication to authenticate ANY user in that realm for non-public API calls
	- Suggested realm PushKB with client pushKB, but these can be configured below
- Vault
	- PushKB assumes by default that a vault has been configured for it with a V1 secret engine, a policy allowing
		read/write access to that secret engine, and an auth method with that policy attached.
	- The configurations for these are specified below
	- If the `VAULT_INSECURE_MODE` env var has been set to true, then PushKB can be run without a vault needing to be
		configured, in this case passwords will instead be stored in the database in plaintext. This is not recommended for
		production use, but can be useful for testing and development.

## Config

In order to get data into PushKB there are two options. Option 1 is a protected API, requiring Keycloak setup. Option 2
is a YML file which bootstraps destinations and sources into the system. This is NOT recommended for production use, as
it will not keep nicely in sync, removing tenants etc, it was instead a fast way to bootstrap data into the system
while the API was still being worked on. Please note that if the bootstrap file is removed, the data is not changed on
next startup. The inverse is also true, removing data with the API while it exists in a bootstrap file will result in
the data being recreated on next startup.

### Bootstrap YML

In order to configure the PushKB to start up with bootstrapped yml file, there must be a mounted `yml` file containing
the GOKB(s), folio tenants etc as above, and pointed at with env var `MICRONAUT_CONFIG_FILES`.

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
			- `gokb` must match the  `name` of a GOKB defined above
			- `type` must be either "PACKAGE" or "TIPP" (You will likely want one of each)
- Destinations (For V1 wthis will only be foliodestinations)
	- `foliotenants`
		- These define individual tenants for a folio system, ie each login which requires data to be pushed to it
	- They comprise of a `name`, `authtype`, `tenant`, `baseurl`, `username` and `password`
		- `name` is the name for this tenant in the PushKB DB, will be important later
		- `authtype` must be either "OKAPI" or "NONE". For Eureka I believe authtype "OKAPI" should still suffice as logins
			are "backwards compatible"
		- `tenant` is the configured tenant name in FOLIO. For folio-snapshot this would be "diku"
		- `baseurl` is the access url for external API calls of this folio.
			- Eventually the structure may split out folio server and folio tenant
			- That will likely be after this method of bootstrapping is deprecated
		- `username` and `password` are the login credentials for this folio tenant
	- `foliodestinations`
		- These are the individual "push sites" for a tenant, you will need one for pushing PCIs and one for pushing
			Packages
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

## Vault Configuration

After a vault has been configured to be used alongside PushKB, additional configurations should be made to ensure
authentication and secret storage works correctly, this can either be done through the CLI or UI found at the vault
address.

- A secret engine of type KV version 1 should be created, which should be used for the storage of credentials. Further
	documentation: https://developer.hashicorp.com/vault/docs/secrets/kv/kv-v1
- A policy will need to be written that allows both create and read permissions to the previously created secret engine:
	Further documentation: https://developer.hashicorp.com/vault/docs/concepts/policies
- An authentication method needs to be created (userpass or kubernetes) with which has the policy that was created.
	- [kubernetes auth](https://developer.hashicorp.com/vault/docs/auth/kubernetes)
	- [userpass auth](https://developer.hashicorp.com/vault/docs/auth/userpass)
	- [further documentation](https://developer.hashicorp.com/vault/docs/auth)

- The details for the vault address, secret engine path and authentication credentials should then be supplied to pushKb
	either through the yml or env vars.

## Env Vars

PushKB accepts multiple env vars

### Core service settings

| Variable                             | Required | Default                                                                                                                             | Notes                                                                             |
|:-------------------------------------|:--------:|:------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------------------------------------|
| `MICRONAUT_CONFIG_FILES`             |    No    | -                                                                                                                                   | Path to YML for bootstrapping. Likely to be deprecated once the API is finalized. |
| `TASKSCHEDULER_REACTIVE_CONCURRENCY` | **Yes**  | `1`                                                                                                                                 | Number of simultaneous tasks. **Note:** Mandatory in current SNAPSHOT builds.     |
| `ACCESSIBLE_URL`                     |    No    | Return from [EmbeddedServer.getUrl()](https://docs.micronaut.io/4.1.4/api/io/micronaut/runtime/server/EmbeddedServer.html#getURL()) | Needed for V1 package sync work to define callback/request locations.             |

### Identity and access (Keycloak)

| Variable              | Required | Default                 | Notes                                   |
|:----------------------|:--------:|:------------------------|:----------------------------------------|
| `OIDC_ISSUER_DOMAIN`  |   Yes    | `http://localhost:8088` | Base URL for your Keycloak instance.    |
| `KEYCLOAK_REALM`      |   Yes    | `PushKB`                | The specific Realm dedicated to PushKB. |
| `OAUTH_CLIENT_ID`     |   Yes    | `pushKB`                | The client ID (name) within the Realm.  |
| `OAUTH_CLIENT_SECRET` |   Yes    | `secret`                | The client secret for authentication.   |

### Secrets management

PushKB uses Hashicorp Vault to securely store source and destination credentials.

| Variable                             | Required | Default | Notes                                                                                                                                           |
|:-------------------------------------|:--------:|:--------|:------------------------------------------------------------------------------------------------------------------------------------------------|
| `VAULT_INSECURE`                     |    No    | `false` | Set to `true` to be able to run without vault and store passwords in plaintext DB (Dev/Test only). If vault is configured it will still be used |
| `VAULT_HASHICORP_URL`                | **Yes*** | -       | Required if `VAULT_INSECURE` is `false`.                                                                                                        |
| `VAULT_HASHICORP_SECRET_ENGINE_PATH` | **Yes*** | -       | Path to your KV V1 engine (e.g., `pushKB`).                                                                                                     |
| `VAULT_HASHICORP_AUTHTYPE`           | **Yes*** | -       | Options: `token`, `userpass`, or `kubernetes`.                                                                                                  |

#### Vault Authentication Credentials

The following variables are required depending on the `VAULT_HASHICORP_AUTHTYPE` selected above.

| Variable                                                | Required For | Description                                                                                      |
|:--------------------------------------------------------|:-------------|:-------------------------------------------------------------------------------------------------|
| `VAULT_HASHICORP_USERNAME`                              | `userpass`   | The username for vault authentication.                                                           |
| `VAULT_HASHICORP_PASSWORD`                              | `userpass`   | The password for vault authentication.                                                           |
| `VAULT_HASHICORP_TOKEN`                                 | `token`      | Root vault token or token with engine access.                                                    |
| `VAULT_HASHICORP_KUBERNETES_ROLE`                       | `kubernetes` | The role name configured in Vault.                                                               |
| `VAULT_HASHICORP_KUBERNETES_MOUNT_PATH`                 | `kubernetes` | The mount path for the K8s auth method.                                                          |
| `VAULT_HASHICORP_KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH` | `kubernetes` | Path to a file containing the service account JWT, which must be mounted along with the service. |

# Implementor Workflow

To get PushKB running in a production-ready state, follow this sequence to configure identity and secret management.

## 1. Keycloak Configuration

PushKB uses Keycloak for service-to-service authentication via the `client_credentials` flow.

1. **Create a Realm:** Create a new realm named `PushKB`.
2. **Create a Client:**
	* **ID:** `pushKB`
	* **Capability Config:** Enable **Service Accounts Roles**.
	* **Access Type:** Confidential.
3. **Get Client Secret:** Navigate to the "Credentials" tab and copy the `Secret`.
4. **Configure PushKB:** Set your environment variables to match:
	* `OIDC_ISSUER_DOMAIN`: Your Keycloak URL.
	* `KEYCLOAK_REALM`: `PushKB`
	* `OUTH_CLIENT_ID`: `pushKB`
	* `OAUTH_CLIENT_SECRET`: Your copied secret.

## 2. Vault Configuration (Invisible Secrets Storage)

PushKB uses Vault as a silent backend for credentials. Once configured, you never need to touch Vault manually; PushKB
will draw from and write to it automatically through normal API calls.

### Step 1: Set up the Secret Engine

1. In the Vault UI, go to **Secrets Engines** > **Enable new engine**.
2. Select **KV** and click **Next**.
3. Set the **Path** to `pushkb`.
4. **Important:** Under **Method Options**, ensure **Version** is set to `1`.
5. Click **Enable Engine**.

### Step 2: Configure the Access Policy

1. Navigate to **Policies** > **Create ACL policy**.
2. **Name:** `pushkb-policy`.
3. **Policy Body:** This can be configured in either HCL or JSON

```hcl
		path "pushkb/*" {
			capabilities = ["create", "read", "update", "delete", "list"]
		}
```

```json
{
	"path": {
		"pushKb/*": {
			"capabilities": [
				"create",
				"read",
				"update",
				"delete",
				"list"
			]
		}
	}
}
```

4. Click **Create policy**. Attach this policy to your chosen Auth Method (Kubernetes role or Userpass user).

### Step 3: Configure the Authentication Methods

Currently, the supported auth methods are **token**, **kubernetes** and **userpass**. We recommend **kubernetes** or *
*userpass** for production.

#### If using userpass

1. Navigate to **Access** > **Authentication Methods**
2. Click **Enable new method**
3. Select **Generic - Userpass**
4. **Important:** Ensure path is left as default **userpass**
5. Click **Enable method**
6. From the **Method Options** page, click **View method**
7. Click **Create user**
8. Configure the desired username and password within the fields
9. Select the **Tokens** dropdown
10. Within the **Generated Token's Policies**, add the name of the access policy previously created
11. Click **Save**, your vault should now be configured to allow for the created user to access the pushkb secret engine

### Step 4: Configure PushKB Env Vars

Point PushKB to the engine path.

| Variable                             | Value                      | Description                      |
|:-------------------------------------|:---------------------------|:---------------------------------|
| `VAULT_HASHICORP_URL`                | `http://vault:8200`        | Your Vault address.              |
| `VAULT_HASHICORP_SECRET_ENGINE_PATH` | `pushkb`                   | Must match the path from Step 1. |
| `VAULT_HASHICORP_AUTHTYPE`           | `kubernetes` \| `userpass` | Choose your auth method.         |

#### If using `VAULT_HASHICORP_AUTHTYPE=kubernetes`

| Variable                                                | Value                                                                                                                     |
|:--------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------|
| `VAULT_HASHICORP_KUBERNETES_ROLE`                       | The role name configured in Vault (e.g., `pushkb-role`).                                                                  |
| `VAULT_HASHICORP_KUBERNETES_MOUNT_PATH`                 | `kubernetes` (The mount path of the K8s auth method).                                                                     |
| `VAULT_HASHICORP_KUBERNETES_SERVICE_ACCOUNT_TOKEN_PATH` | The path to a file containing the JWT token used to prove PushKB's identity, which must also be mounted with the service. |

#### If using `VAULT_HASHICORP_AUTHTYPE=userpass`

| Variable                   | Value                             |
|:---------------------------|:----------------------------------|
| `VAULT_HASHICORP_USERNAME` | The username created in Vault.    |
| `VAULT_HASHICORP_PASSWORD` | The password for that Vault user. |

#### Verification

To see if Vault is running and configured correctly, the `/health` endpoint can give an indication.
Scenarios:

- Vault is configured correctly, running, and PushKB is in "secure mode"
	- This is the recommended configuration and should work invisibly and as expected. A Vault outage will cause errors
		and prevent READ/WRITE of credentials, but when back up service should resume as normal.
	- Health response: 200

```json
{
	...
	"secrets": {
		"name": "push-kb",
		"status": "UP",
		"details": {
			"mode": "SECURE"
		}
	}
}
```

- Vault is configured correctly, running, but PushKB is in "insecure mode"
	- This should work as intended in most circumstances, but in the event of a Vault outage, the system would fall back
		to
		storing passwords in plaintext in the database.
	- health response: 200

```json
{
	...
	"secrets": {
		"name": "push-kb",
		"status": "UP",
		"details": {
			"warning": "Vault is configured, but insecure mode is active, and so a vault failure will result in insecure secret storage",
			"mode": "SECURE"
		}
	}
}
```

- Vault is configured incorrectly, or not running, with PushKB in "insecure mode"
	- This will result in PushKB storing credentials in plaintext in the database.
	- health response: 200
		- NOTE: The vault error may change depending on what happened. For more information check the
			logs from startup

```json
{
	...
	"secrets": {
		"name": "push-kb",
		"status": "UP",
		"details": {
			"mode": "INSECURE",
			"warning": "Primary Vault unreachable. Falling back to insecure local storage.",
			"vaultError": "Unexpected Vault failure"
		}
	}
}
```

- Vault is configured incorrectly, or not running, with PushKB in "secure mode"
	- This will not necessarily cause the module to fail at startup, but running operations requiring fetching of
		credentials will fail.
	- health response: 503
		- NOTE: The vault error may change depending on what happened. For more information check the
			logs from startup

```json
{
	...
	"secrets": {
		"name": "push-kb",
		"status": "DOWN",
		"details": {
			"error": "Unexpected Vault failure"
		}
	}
}
```

## Running workflows

Once the infrastructure setup steps above are complete, PushKB is ready to start accepting configuration data and to
pull and push data. To aid specific workflows extra documents are linked here documenting the process for specific data
flow paths.
The general pattern is:

- Create `Source`
	- This will set up a DutyCycleTask to schedule running ingests for the source data
- Create `Destination`
- Create a `PushTask` linking source to destination
	- This will set up a DutyCycleTask to schedule pushing the cached data from source ingest to the destination.

Specific workflow documentation:

- [GOKB -> FOLIO](./docs/gokb_to_folio_workflow.md)

# Developer information

## Cloning

At the moment, this module relies on an as-yet unreleased module "taskscheduler". This is included as a git submodule,
and so the options are _either_ to clone with submodules `git clone <this repo> --recurse-submodules` or after running
`git clone` to also run `git submodule init` followed by `git submodule update`.

## Bootstrapping

As a temporary measure, Sources, Destinations and PushTasks can be bootstrapped into the module by way of
`micronaut.config.files` (Or running with a custom profile yml)

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

To run the module as a developer, first navigate to `/infrastructure` and run `docker compose up`. This will configure a
testing DB with a postgres and a keycloak (WIP, kKC is not in use right now... to be worked on).

Postgres should start on port `6543` to avoid clashes with any local postgres instances for FOLIO or such.

Then use `./gradlew run` to start the module.

### Vault

The docker compose image includes a hashicorp vault. After a fresh docker compose up, the vault should be initialised
running in developer mode (ie unsealed and ready-to-use).

The vault by default should also be configured with a root access token, a KV(V1) secret engine and a user with a
developer policy attached, which should mirror the config present in the application-development.yml

When running tests, micronaut automatically spins up containers required for tests through the test-resources plugin,
these are configured within the application.yml under the hashicorp-vault property. Importantly, in order for micronaut
to know that a vault container is required, either the vault.client.uri/token need to be injected into the YAML.

## Migrations

Migrations in PushKB are handled by Flyway.

### Naming

Migration files are usually named in the following way `V(migration_number)__name_for_migration_file` as per Flyway
migration versioning, see https://documentation.red-gate.com/fd/migrations-184127470.html

### Strategy

The aim is to keep migration files relatively minimal. Flyway will handle the running in increasing order, we should aim
to keep migration files small so that they are easily parseable by a dev scanning through them, and easy to see what
each file is doing. Also allows easier use of "undo" migrations should they become necessary.

## Issues

### Stuck tasks

As PushKB is a significantly smaller external-to-folio module, with lower overheads and issues around longer running
tasks, there should be far fewer instances of the module completely dying. If the module shuts down gracefully, it will
release all DutyCycleTasks back into the pool for other instances of PushKB to pick up. However, there is a chance that
a task can get "stuck" if the module running it crashes suddenly. Currently this requires a manual reset of the
DutyCycleTask with a POST to `/dutycycletasks/<taskId>/reset`. WARNING: The recommendation is that all PushKB instances
are gracefully shut down, then restarted, and any that _remain_ `IN-PROCESS` having not run for a long time are the ones
that are safe then to perform this on. Performing a reset on a task which is legitimately in use can have strange
consequences.

This is equivalent to the "zombie jobs" issue in ERM, and
a [proper solution is on the roadmap](https://gitlab.com/knowledge-integration/platform/patterns/taskscheduler/-/issues/3),
but unlikely to make it into V1. This _shouldn't_ be too impactful, as again the theory is that these should crash far
less often than mod-agreements (or indeed any FOLIO module).

### Failing tasks

Sometimes a task can fail for reasons _outside_ of PushKB, such as GOKB API changes, or FOLIO being down etc. The task
scheduler inside PushKB will attempt a failing task 3 times, then set a "lastAttempted" field on the DutyCycleTask.
This will in turn prevent that task from being picked up by the scheduler for 10 minutes. These two values are not
currently configurable,
but [will be in a future update](https://gitlab.com/knowledge-integration/platform/patterns/taskscheduler/-/work_items/2).

This does mean that the `lastAttempted` only gets set currently after 3 failed attempts in a row. In addition there is a
`lastRun` which is set on successful completetion of a task. This behaviour is intended for logging purposes ONLY right
now, and is subject to change down the
line. well-thought-out metadata is
an [issue](https://gitlab.com/knowledge-integration/platform/patterns/taskscheduler/-/work_items/1) on the future
roadmap, but this will not be in place for V1.
