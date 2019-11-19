# Assumptions:
1. Not exist client with the same Client Identifier
2. All messages have correct structure
3. Files can't be empty

### Server
## Build
```bash
./gradlew :server:fatJar
```

## Start
```bash
java -jar server/build/libs/server-all-1.0-SNAPSHOT.jar
```

### Client
## Build
```bash
./gradlew :client:fatJar
```

## Start
```bash
java -jar client/build/libs/client-all-1.0-SNAPSHOT.jar
```
