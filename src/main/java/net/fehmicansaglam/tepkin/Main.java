package net.fehmicansaglam.tepkin;

import akka.util.Timeout;
import net.fehmicansaglam.tepkin.api.BsonDocumentBuilder;
import net.fehmicansaglam.tepkin.api.MongoCollection;
import net.fehmicansaglam.tepkin.bson.BsonDocument;
import net.fehmicansaglam.tepkin.bson.Implicits;
import net.fehmicansaglam.tepkin.bson.element.BsonString;
import net.fehmicansaglam.tepkin.protocol.result.DeleteResult;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        final MongoClient mongoClient = MongoClient.create("localhost", 27017);
        final MongoCollection collection = mongoClient.collection("colossus", "abuzer");
        final CompletionStage<Optional<BsonDocument>> cs = collection.findOne(
                BsonDocument.empty(),
                mongoClient.context().dispatcher(),
                new Timeout(Duration.create(5, TimeUnit.SECONDS)));
        cs.thenAccept(System.out::println);


        BsonDocumentBuilder builder = new BsonDocumentBuilder();
        builder.addElement(new BsonString("ali", new Implicits.BsonValueString("veli")));

        final CompletionStage<DeleteResult> dcs = mongoClient.collection("colossus", "abuzer")
                .delete(new BsonDocument[]{builder.build()}, mongoClient.context().dispatcher(),
                        new Timeout(Duration.create(5, TimeUnit.SECONDS)));
        dcs.thenAccept(System.out::println);

        Thread.sleep(1000);
        mongoClient.shutdown();
    }
}
