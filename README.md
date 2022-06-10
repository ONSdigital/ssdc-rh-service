# ssdc-rh-service

## Introduction 
  This service stores Case & UAC update messages from PubSub into Firestore.  It has an endpoint that 
  takes a UAC hash and provides an EQLaunch Token.

  The firestore allows it to be 'super fast' at reading data out for fast launching
