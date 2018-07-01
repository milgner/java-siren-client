# Java Siren Hypermedia Client

[![Build Status](https://travis-ci.org/milgner/java-siren-client.svg?branch=master)](https://travis-ci.org/milgner/java-siren-client)

### What?

This is a generic client to access Siren-based Hypermedia APIs.
It currently only supports JSON Siren and makes a couple of other assumptions:

  - every action will respond with a Siren entity
  - requests are encoded in UTF-8
  - everything is well-formed and according to the [specification](https://github.com/kevinswiber/siren). No error handling yet.

### Why?

This is just an experiment to familiarize myself with the current state of Java development.
After having only sporadic contact with the Java ecosystem for a couple of years I decided to familiarize myself with how things are done in 2018.

## Contributions

As this is primarily an educational project for the time being, I welcome any suggestions for improvement and pull requests.

## TODO

  - action fields currently only support string values
