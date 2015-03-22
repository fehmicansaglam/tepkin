# Tepkin

[![Join the chat at https://gitter.im/fehmicansaglam/tepkin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/fehmicansaglam/tepkin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

[![Build Status](https://travis-ci.org/fehmicansaglam/tepkin.svg?branch=master)](https://travis-ci.org/fehmicansaglam/tepkin)
[![Codacy Badge](https://www.codacy.com/project/badge/d5039668605d44fea3adf2302e7e6c31)](https://www.codacy.com/public/fehmicansaglam/tepkin)
![Progress](http://progressed.io/bar/0?title=0.2)

Reactive MongoDB Driver for Scala 2.11 and Java 8 built on top of Akka IO and Akka Streams.

Only MongoDB 2.6+ is supported.

Don't hesitate to ask questions in the [Tepkin Google Group](https://groups.google.com/forum/#!forum/tepkin)

## Contributions
Tepkin is a young but very active project and absolutely needs your help. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation

Please read our Scala Guide first: https://github.com/fehmicansaglam/tepkin/wiki/Scala-Guide

## Quick Start

### Setting up dependencies

Latest stable Tepkin release is **0.1** and is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ctepkin).

Scala developers, add the following dependency:

```scala
libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "tepkin" % "0.1"
)
```

Java developers, `net.fehmicansaglam.tepkin.api` package is intended to be used from Java. To use the package, add the following dependency to your pom.xml:

```xml
<dependency>
  <groupId>net.fehmicansaglam</groupId>
  <artifactId>tepkin-java_2.11</artifactId>
  <version>0.1</version>
</dependency>
```

Or if you want to be on the bleeding edge using snapshots, latest snapshot release is **0.2-SNAPSHOT**.

Scala developers, add the following repository and dependency:
```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "tepkin" % "0.2-SNAPSHOT"
)
```

Java developers add the following repository and dependency to your pom.xml:

```xml
<repository>
  <id>Sonatype Snapshots</id>
  <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
</repository>

<dependency>
	<groupId>net.fehmicansaglam</groupId>
	<artifactId>tepkin-java_2.11</artifactId>
	<version>0.2-SNAPSHOT</version>
</dependency>
```

## Scala API

### Working with BSON DSL

To construct a Bson document, you can either create BsonElements and join them with `~` or create a document directly.

```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.bson.element.BsonObjectId
import org.joda.time.DateTime

// Construct a BsonDocument from BsonElements
val element = "name" := "Johny"
val document = element ~
  ("surname" := "Doe") ~
  ("age" := 28) ~
  ("months" := $array(1, 2, 3))

// Construct a BsonDocument
val document = $document(
  "_id" := BsonObjectId.generate,
  "name" := "Johny",
  "surname" := "Doe",
  "age" := 28,
  "months" := $array(1, 2, 3),
  "details" := $document(
    "salary" := 455.5,
    "inventory" := $array("a", 3.5, 1L, true),
    "birthday" := new DateTime(1987, 3, 5, 0, 0)
  )
)
```

There is an implicit conversion from any `BsonElement` to `BsonDocument` for convenience.

```scala
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.element.BsonElement
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

val element: BsonElement = "name" := "fehmi"
val document: BsonDocument = "name" := "fehmi"
```

### Connecting to MongoDB

To make a connection to MongoDB, use the `MongoClient` interface.

```scala
import net.fehmicansaglam.tepkin.MongoClient

// Connect to a MongoDB node.
val client = MongoClient("mongodb://localhost")
```

`MongoClient` manages multiple connection pools to MongoDB instances and therefore is a heavy class. Most of the time you will need only one `MongoClient` instance per application.

Use `MongoDatabase` and `MongoCollection` in order to obtain a reference to a database and a collection.

```scala
// Obtain a reference to the "tepkin" database
val db = client("tepkin")

// Obtain a reference to the "example" collection in "tepkin" database.
val collection = db("example")
```

`MongoDatabase` and `MongoCollection` are lightweight classes and may be instantiated more than once if needed. However they are both immutable and reusable.

All methods in the `MongoCollection` class need an implicit `scala.concurrent.ExecutionContext` and an `akka.util.Timeout`. You can define a default timeout and use the client's execution context as shown below:

```scala
import akka.util.Timeout
import scala.concurrent.duration._

// val client = ...

import client.ec
implicit val timeout: Timeout = 5.seconds
```

### Find documents

```scala
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

val query: BsonDocument = "name" := "fehmi"

val source = collection.find(query)
```

All find methods in Tepkin return an `akka.stream.scaladsl.Source[List[BsonDocument], ActorRef]`. Then you can use any method in Akka Streams to process the returned stream.

### Insert operations

#### Insert a single document

```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

val document = ("name" := "fehmi") ~ ("surname" := "saglam")
collection.insert(document)
```
#### Insert a collection of documents

```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

val documents = (1 to 100).map(i => $document("name" := s"fehmi$i"))
collection.insert(documents)
```
#### Insert a large number of documents from a stream

```scala
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

import scala.collection.immutable.Iterable

implicit val mat = ActorFlowMaterializer()(client.context)

val documents: Source[List[BsonDocument], Unit] = Source {
  Iterable.tabulate(100) { _ =>
    (1 to 1000).map(i => $document("name" := s"fehmi$i")).toList
  }
}

collection.insertFromSource(documents).runForeach(_ => ())
```
### Other queries

#### Update

```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

import scala.concurrent.Future

val document = ("name" := "fehmi") ~ ("surname" := "saglam")

val result: Future[UpdateResult] = for {
  insert <- collection.insert(document)
  update <- collection.update(
    query = "name" := "fehmi",
    update = $set("name" := "fehmi can")
  )
} yield update
```

#### Find and update

Update and return the old document.

```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

collection.findAndUpdate(
  query = Some("name" := "fehmi"),
  update = $set("name" := "fehmi can")
)
```

Update and return the updated document.

```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._

collection.findAndUpdate(
  query = Some("name" := "fehmi"),
  update = $set("name" := "fehmi can"),
  returnNew = true
)
```

#### Create index
```scala
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.bson.Implicits._
import net.fehmicansaglam.tepkin.protocol.command.Index

collection.createIndexes(Index(name = "name_surname", key = ("name" := 1) ~ ("surname" := 1)))
```

## Java API

```java
import net.fehmicansaglam.tepkin.api.*;

MongoClient mongoClient = MongoClient.create("mongodb://localhost");
MongoCollection collection = mongoClient.db("tepkin").collection("test");

BsonDocument document = BsonDocumentBuilder.create().addString("name", "fehmi").build();

FiniteDuration timeout = Duration.create(5, TimeUnit.SECONDS);

CompletableFuture<Optional<BsonDocument>> cf = collection
  .insert(document, mongoClient.ec(), timeout)
  .thenCompose(insert -> collection.findOne(mongoClient.ec(), timeout));
Optional<BsonDocument> actual = cf.get(5, TimeUnit.SECONDS);
```
