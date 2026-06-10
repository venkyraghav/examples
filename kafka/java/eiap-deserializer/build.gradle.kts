import com.github.davidmc24.gradle.plugin.avro.GenerateAvroJavaTask

plugins {
    java

    // plugin for generating Java classes from .avsc files
    id ("com.github.davidmc24.gradle.plugin.avro") version "1.9.1"

    // plugin for generating Java classes from .proto files
    id("com.google.protobuf") version "0.9.4"
}

import groovy.json.JsonSlurper

group = "eiap.consumer"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

repositories {
    mavenCentral()

    // Confluent artifacts
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
}

// 3. Register the directory so your IDE and compiler see the fixed files
sourceSets {
    main {
        java {
            srcDir(layout.buildDirectory.dir("generated-main-avro-java"))
        }
    }
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.6.1")
    implementation("org.apache.avro:avro:1.11.3")
    implementation("org.slf4j:slf4j-simple:2.0.13")

    // Confluent serializers for Avro & Protobuf
    implementation("io.confluent:kafka-avro-serializer:7.6.1")
    implementation("io.confluent:kafka-protobuf-serializer:7.6.1")

    // Protobuf runtime
    implementation("com.google.protobuf:protobuf-java:3.25.3")

    // testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

avro {
    // optional avro plugin configuration
    fieldVisibility = "PRIVATE"
    stringType = "String"
}

// protobuf {
//     protoc {
//         artifact = "com.google.protobuf:protoc:3.25.3"
//     }

//     // Configure generated sources output directory
//     generateProtoTasks {
//         all().each { task ->
//             task.builtins {
//                 java {
//                     // Use default output directory
//                 }
//             }
//         }
//     }
// }

// Add generated protobuf sources to source sets
// sourceSets {
//     main {
//         java {
//             srcDir "build/generated/source/proto/main/java"
//         }
//     }
// }

// tasks.register("validateAvroSchemas") {
//     group = "verification"
//     description = "Validates Avro schemas for duplicate record full names before code generation."

//     inputs.files(fileTree("src/main/avro") {
//         include "**/*.avsc"
//     })

//     doLast {
//         def parser = new JsonSlurper()
//         def seenFullNames = [:]
//         def missingNamespace = []

//         fileTree("src/main/avro").matching {
//             include "**/*.avsc"
//         }.files.sort().each { File schemaFile ->
//             def schema = parser.parse(schemaFile)

//             if (schema.type != "record") {
//                 return
//             }

//             def namespace = schema.namespace ? schema.namespace.toString().trim() : ""
//             def name = schema.name?.toString()?.trim()
//             if (!name) {
//                 throw new GradleException("Schema ${schemaFile} is missing a record name")
//             }

//             def fullName = namespace ? "${namespace}.${name}" : name
//             if (!namespace) {
//                 missingNamespace << schemaFile.toString()
//             }

//             if (seenFullNames.containsKey(fullName)) {
//                 throw new GradleException(
//                     "Duplicate Avro record full name "${fullName}" found in ${schemaFile} and ${seenFullNames[fullName]}. " +
//                     "This will break SpecificRecord generation and RecordNameStrategy deserialization."
//                 )
//             }

//             seenFullNames[fullName] = schemaFile
//         }

//         if (!missingNamespace.isEmpty()) {
//             logger.warn("Avro schemas without namespace detected. This is allowed, but with RecordNameStrategy the record name must remain globally unique.")
//             missingNamespace.each { logger.warn(" - ${it}") }
//         }
//     }
// }

// tasks.named("generateAvroJava").configure {
//     dependsOn tasks.named("validateAvroSchemas")
// }

// tasks.withType(Test).configureEach {
//     useJUnitPlatform()
// }

tasks.withType<GenerateAvroJavaTask>().configureEach {
    // 1. Define where the files will be generated
    val genDir = layout.buildDirectory.dir("generated-main-avro-java").get().asFile
    setOutputDir(genDir)

    // 2. Brute Force: Insert the package declaration if it"s missing
    doLast {
        val targetPackage = "com.ericsson.eiap" // Change this!

        genDir.walkTopDown().filter { it.extension == "java" }.forEach { file ->
            val content = file.readText()
            if (!content.startsWith("package")) {
                file.writeText("package $targetPackage;\n\n$content")
                logger.lifecycle("Fixed missing namespace in: ${file.name}")
            }
        }
    }
}
