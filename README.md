# Tepkin

[![Build Status](https://travis-ci.org/fehmicansaglam/tepkin.svg?branch=master)](https://travis-ci.org/fehmicansaglam/tepkin)

Reactive MongoDB Driver for Scala 2.11 and Java 8 built on top of Akka IO and Akka Streams.

Only MongoDB 3.0+ is supported.

## Contributions
Tepkin is a relatively young project and absolutely needs your help. Good ways to contribute include:

* Raising bugs and feature requests
* Fixing bugs
* Improving the performance
* Adding to the documentation

## Scala example

```scala
val client = MongoClient("mongodb://localhost")
val db = client("tepkin")
val collection = db("mongo_collection_spec")
val document = ("name" := "fehmi") ~ ("surname" := "saglam")

implicit val timeout: Timeout = 5.seconds

val future = for {
  insert <- collection.insert(Seq(document))
  update <- collection.update(
    query = ("name" := "fehmi"),
    update = $set("name" := "fehmi can")
  )
} yield update
```

## Java example

```java
MongoClient mongoClient = MongoClient.create("mongodb://localhost");
MongoCollection collection = mongoClient.db("tepkin").collection("test");

BsonDocumentBuilder builder = new BsonDocumentBuilder();
builder.addString("name", "fehmi");
BsonDocument document = builder.build();

CompletableFuture<Optional<BsonDocument>> cf = collection
  .insert(document, mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS)))
  .thenCompose(insert ->
                collection.findOne(mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS))));
Optional<BsonDocument> actual = cf.get(5, TimeUnit.SECONDS);
```
