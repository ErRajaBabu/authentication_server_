# Contribution guide

## Installing the testing framework

For testing you will need FrisbyJs and Jasmine:

    npm install -g frisby
    npm install -g jasmine-node
    npm install pow-mongodb-fixtures -g
    npm install -g underscore

Note: Underscore is required because of an issue in the dependencies of mongodb-fixtures.

## Running the tests

First, start the server in testing mode:

    NODE_ENV=testing-config node app.js
    
(Re)populate the testing Mongodb database:

    mongo test --eval \"db.dropDatabase()\"
    mongofixtures test fixtures
    
Then the tests:

    NODE_ENV=testing-config jasmine-node test
    
All tests must pass. You have to repeat the steps above for all environments:

* testing-config
* testing-mongodb
* testing-fallback
    
## Misc dev notes

Environment variables:

    MJOLNIR_VERBOSE: boolean, set true to verbose mode
    NODE_ENV: environment in which the application should run, the default is development; can be: production, testing