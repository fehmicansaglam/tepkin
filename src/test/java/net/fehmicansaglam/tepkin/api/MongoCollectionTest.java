package net.fehmicansaglam.tepkin.api;

import akka.util.Timeout;
import net.fehmicansaglam.tepkin.MongoClient;
import net.fehmicansaglam.tepkin.bson.BsonDocument;
import net.fehmicansaglam.tepkin.protocol.result.CountResult;
import net.fehmicansaglam.tepkin.protocol.result.InsertResult;
import org.junit.*;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MongoCollectionTest {

    private static MongoClient mongoClient;
    private static MongoCollection collection;

    @BeforeClass
    public static void setUpClass() {
        mongoClient = MongoClient.create("localhost", 27017);
        collection = mongoClient.collection("tepkin", "test");
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        mongoClient.shutdown();
        mongoClient = null;
    }

    @Before
    public void setUp() {
        collection.drop(mongoClient.context().dispatcher(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
    }

    @After
    public void tearDown() {
        collection.drop(mongoClient.context().dispatcher(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
    }

    @Test
    public void countCollection() throws Exception {
        final CompletableFuture<CountResult> cf = collection
                .count(mongoClient.context().dispatcher(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
        final CountResult actual = cf.get(5, TimeUnit.SECONDS);
        assertTrue(actual.ok());
        assertEquals(0L, actual.n());
    }

    @Test
    public void insert1Document() throws Exception {
        final BsonDocumentBuilder builder = new BsonDocumentBuilder();
        builder.addString("name", "fehmi");
        final BsonDocument document = builder.build();

        final CompletableFuture<InsertResult> cf = collection
                .insert(document, mongoClient.context().dispatcher(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
        final InsertResult actual = cf.get(5, TimeUnit.SECONDS);
        assertTrue(actual.ok());
        assertEquals(1, actual.n());
    }
}