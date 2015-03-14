# Tepkin

[![Build Status](https://travis-ci.org/fehmicansaglam/tepkin.svg?branch=master)](https://travis-ci.org/fehmicansaglam/tepkin)
![Progress](http://progressed.io/bar/71?title=brewing)

Reactive MongoDB Driver for Scala 2.11 and Java 8 built on top of Akka IO and Akka Streams.

Only MongoDB 3.0+ is supported.

## Contributions
Tepkin is a young but very active project and absolutely needs your help. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation

## Quick Start

### Setting up dependencies

Current release is *0.1-SNAPSHOT*. So you need to add Sonatype Snapshots repository to your build.sbt:

```scala
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
```

For Scala developers, add the following dependency:
```scala
libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "tepkin" % "0.1-SNAPSHOT"
)
```
For Java developers, _net.fehmicansaglam.tepkin.api_ package is intended to be used from Java. To use the package, add the following dependency to build.sbt.

```scala
libraryDependencies ++= Seq(
  "net.fehmicansaglam" %% "tepkin-java" % "0.1-SNAPSHOT"
)
```

## Scala API

### Working with BsonDsl

To construct a Bson document, you can either create BsonElements and join them or create a document directly.

```scala
  import net.fehmicansaglam.bson.BsonDsl._
  import net.fehmicansaglam.bson.Implicits._
  import net.fehmicansaglam.bson.element.BsonObjectId
  import org.joda.time.DateTime

  // Construct a BsonDocument from BsonElements
  val element = "name" := "Johny"
  val document = element ~
    ("surname" := "Doe")
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

### Connect to database

To make a connection to MongoDB, use the <code>MongoClient</code> interface.

```scala
  import akka.util.Timeout
  import net.fehmicansaglam.tepkin.MongoClient

  import scala.concurrent.duration._

  // Connect to Mongo client
  val client = MongoClient("mongodb://localhost")

  // Obtain reference to database "tepkin" using client
  val db = client("tepkin")

  // Obtain reference to the collection "Example" using database
  val collection = db("example")

  import client.ec
  implicit val timeout: Timeout = 30.seconds
```

### Insert operations

Insert a single document

```scala
  import net.fehmicansaglam.bson.BsonDsl._
  import net.fehmicansaglam.bson.Implicits._

  val document = ("name" := "fehmi") ~ ("surname" := "saglam")
  collection.insert(document)
```
Insert a collection of documents

```scala
  import net.fehmicansaglam.bson.BsonDsl._
  import net.fehmicansaglam.bson.Implicits._

  val documents = (1 to 100).map(i => $document("name" := s"fehmi$i"))
  collection.insert(documents)
```
To insert large number of documents from source

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
### Simple queries

Find and update
```scala
  import net.fehmicansaglam.bson.BsonDsl._
  import net.fehmicansaglam.bson.Implicits._

  collection.findAndUpdate(
    query = Some("name" := "fehmi"),
    update = $set("name" := "fehmi can")
  )

  val newDocument = collection.findAndUpdate(
    query = Some("name" := "fehmi"),
    update = $set("name" := "fehmi can"),
    returnNew = true
  )
```

### Create index
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

  BsonDocumentBuilder builder = new BsonDocumentBuilder();
  builder.addString("name", "fehmi");
  BsonDocument document = builder.build();

  FiniteDuration timeout = Duration.create(5, TimeUnit.SECONDS);

  CompletableFuture<Optional<BsonDocument>> cf = collection
    .insert(document, mongoClient.ec(), timeout)
    .thenCompose(insert -> collection.findOne(mongoClient.ec(), timeout));
  Optional<BsonDocument> actual = cf.get(5, TimeUnit.SECONDS);
```
## Samples
Sample applications will be added as soon as possible to demonstrate Scala and Java API usage.
