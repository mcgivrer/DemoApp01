# README

[![Java CI with build script](https://github.com/mcgivrer/DemoApp01/actions/workflows/main.yml/badge.svg)](https://github.com/mcgivrer/DemoApp01/actions/workflows/main.yml)

## Project DemoApp01 version 0.0.1

### Build JAR with sdkman and javac

```bash
sdk env use
chmod +x ./build
./build
```

### Run tests

```bash
./build test
```

### Execute project

```bash
./build run debug=2
```

>**Note** 
>You can pass as many argument after run as you need:
>e.g.: 
> - `debug`=[1 to 5], 
> - `mode`=[DEVELOPMENT,TESTING,PRODUCTION]
> - `timeout`=[milliseconds]

#### help on program

```bash
./build run help
```

#### run from java

```bash
java -jar target/build/DemoApp01-0.0.1.jar debug=2
```

Thanks,
Frédéric Delorme<frederic.delorme@merckgoup.com>.

