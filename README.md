# Tepkin

Reactive MongoDB Driver for Scala built on top of Akka IO and Akka Streams.

[![Join the chat at https://gitter.im/jeroenr/tepkin](https://badges.gitter.im/jeroenr/tepkin.svg)](https://gitter.im/jeroenr/tepkin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/jeroenr/tepkin.svg?branch=master)](https://travis-ci.org/jeroenr/tepkin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.jeroenr/tepkin_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.jeroenr/tepkin_2.11)
[![License](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

Only MongoDB 2.6+, Scala 2.11+ is supported. **Java support has been dropped. See details here: https://github.com/fehmicansaglam/tepkin/issues/22**

## Contributions
Tepkin is a young but very active project and absolutely needs your help. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation

Please read our Scala Guide first: https://github.com/fehmicansaglam/tepkin/wiki/Scala-Guide

## Quick Start

### Setting up dependencies

Latest stable Tepkin release is **0.6** and is available on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ctepkin). Just add the following dependency:

```scala
libraryDependencies ++= Seq(
  "com.github.jeroenr" %% "tepkin" % "0.6"
)
```

Or if you want to be on the bleeding edge using snapshots, latest snapshot release is **0.7-SNAPSHOT**. Add the following repository and dependency:
```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= Seq(
  "com.github.jeroenr" %% "tepkin" % "0.7-SNAPSHOT"
)
```

## Scala API

### Working with BSON DSL

To construct a Bson document, you can either create BsonElements and join them with `~` or create a document directly.

```scala
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._
import com.github.jeroenr.bson.element.BsonObjectId
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
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.bson.element.BsonElement
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

val element: BsonElement = "name" := "fehmi"
val document: BsonDocument = "name" := "fehmi"
```

### Connecting to MongoDB

To make a connection to MongoDB, use the `MongoClient` interface.

```scala
import com.github.jeroenr.tepkin.MongoClient

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
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

val query: BsonDocument = "name" := "fehmi"

val source = collection.find(query)
```

All find methods in Tepkin return an `akka.stream.scaladsl.Source[List[BsonDocument], ActorRef]`. Then you can use any method in Akka Streams to process the returned stream.

### Insert operations

#### Insert a single document

```scala
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

val document = ("name" := "fehmi") ~ ("surname" := "saglam")
collection.insert(document)
```
#### Insert a collection of documents

```scala
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

val documents = (1 to 100).map(i => $document("name" := s"fehmi$i"))
collection.insert(documents)
```
#### Insert a large number of documents from a stream

```scala
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl.Source
import com.github.jeroenr.bson.BsonDocument
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

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
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

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
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

collection.findAndUpdate(
  query = Some("name" := "fehmi"),
  update = $set("name" := "fehmi can")
)
```

Update and return the updated document.

```scala
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._

collection.findAndUpdate(
  query = Some("name" := "fehmi"),
  update = $set("name" := "fehmi can"),
  returnNew = true
)
```

#### Create index
```scala
import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.Implicits._
import com.github.jeroenr.tepkin.protocol.command.Index

collection.createIndexes(Index(name = "name_surname", key = ("name" := 1) ~ ("surname" := 1)))
```
