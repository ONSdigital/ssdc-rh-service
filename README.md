# ssdc-rh-service

## Introduction 
  This service stores Case & UAC update messages from PubSub into Firestore.  It has an endpoint that 
  takes a UAC hash and provides an EQLaunch Token.

  The firestore allows it to be 'super fast' at reading data out for fast launching

## Running

  Running locally, you need Pubsub emulator and Firebase emulator ENV variables set

    SPRING_CLOUD_GCP_PUBSUB_EMULATOR_HOST=localhost:8538
    FIRESTORE_EMULATOR_HOST="localhost:8542"

You can do this with an export, set them in your .bash_profile/.zshrc or in your IntelliJ run config.  
  