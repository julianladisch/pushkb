/* keycloak needs to match $KEYCLOAK_SCHEMA
 * from .env, can't work out how to run init.sh or
 * init.sql with those rn
 */
CREATE SCHEMA IF NOT EXISTS keycloak;