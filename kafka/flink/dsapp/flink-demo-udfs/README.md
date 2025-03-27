# README

## Package

```shell
mvn clean package
```

## CC Flink Jar Setup

* Upload ./target/flink-demo-udf*.jar to CC Flink to the region where compute pool is provisioned
* To create the UDF, execute the following statement in `confluent flink` CLI or Confluent Cloud UI Flink Workspace
```sql
USE CATALOG venky;

USE venky_cluster;

CREATE FUNCTION JSONJOLT2 AS 'io.confluent.flink.demo.JsonTransformation'
USING JAR 'confluent-artifact://cfa-rrgw21';

-- Uncomment below to create `XML2JSON` UDF
-- CREATE FUNCTION XML2JSON AS 'io.confluent.flink.demo.XML2JSON'
-- USING JAR 'confluent-artifact://cfa-rrgw21';
```
* To upload a new version of the UDF
  * drop the function
  ```sql
  USE CATALOG venky;

  USE venky_cluster;
  
  drop function `JSONJOLT`;
    
  -- drop function `XML2JSON`;
  ```
  * Upload the latest jar
  * Create the UDF function following steps outline above


