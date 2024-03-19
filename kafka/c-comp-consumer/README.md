# C Kafka Client

## Setup

* Change `.vscode/c_cpp_properties.json` to reflect your local setup

```json
                "/opt/homebrew/Cellar/librdkafka/2.3.0/include",
                "/opt/homebrew/Cellar/glib/2.78.4/include/glib-2.0",
                "/opt/homebrew/Cellar/glib/2.78.4/lib/glib-2.0/include"
```

* `make`

## Run options

* Static membership

```shell
./rdkafka_complex_consumer_example -A -i 5 -d generic,metadata,consumer,conf <topic>:<partition>
```

* Dynamic membership

```shell
./rdkafka_complex_consumer_example -A -d generic,metadata,consumer,conf <topic>
```
