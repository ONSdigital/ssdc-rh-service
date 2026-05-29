#!/bin/sh

# Wait for pubsub-emulator to come up
bash -c 'while [[ "$(curl -s -o /dev/null -w ''%{http_code}'' '"$PUBSUB_SETUP_HOST"')" != "200" ]]; do sleep 1; done'

curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/topics/event_uac-update
curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/subscriptions/event_uac-update_rh -H 'Content-Type: application/json' -d '{"topic": "projects/our-project/topics/event_uac-update"}'

curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/topics/event_case-update
curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/subscriptions/event_case-update_rh -H 'Content-Type: application/json' -d '{"topic": "projects/our-project/topics/event_case-update"}'

curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/topics/event_collection-exercise-update
curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/subscriptions/event_collection-exercise-update_rh -H 'Content-Type: application/json' -d '{"topic": "projects/our-project/topics/event_collection-exercise-update"}'

curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/topics/event_survey-update

curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/topics/event_eq-launch
curl -X PUT http://"$PUBSUB_SETUP_HOST"/v1/projects/our-project/subscriptions/event_eq-launch -H 'Content-Type: application/json' -d '{"topic": "projects/our-project/topics/event_eq-launch"}'
