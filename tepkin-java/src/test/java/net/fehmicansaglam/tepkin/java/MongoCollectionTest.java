package net.fehmicansaglam.tepkin.java;

import akka.util.Timeout;
import net.fehmicansaglam.bson.BsonDocument;
import net.fehmicansaglam.tepkin.protocol.result.CountResult;
import net.fehmicansaglam.tepkin.protocol.result.InsertResult;
import org.junit.*;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MongoCollectionTest {

    private static MongoClient mongoClient;
    private static MongoCollection collection;

    @BeforeClass
    public static void setUpClass() {
        mongoClient = MongoClient.create(new InetSocketAddress("localhost", 27017));
        collection = mongoClient.db("tepkin").collection("test");
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        mongoClient.shutdown();
        mongoClient = null;
    }

    @Before
    public void setUp() throws Exception {
        collection.drop(mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS))).get();
    }

    @After
    public void tearDown() throws Exception {
        collection.drop(mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS))).get();
    }

    @Test
    public void countCollection() throws Exception {
        final CompletableFuture<CountResult> cf = collection
                .count(mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
        final CountResult actual = cf.get(5, TimeUnit.SECONDS);
        assertTrue(actual.ok());
        assertEquals(0L, actual.n());
    }

    @Test
    public void insertOneDocument() throws Exception {
        final BsonDocumentBuilder builder = new BsonDocumentBuilder();
        builder.addString("name", "fehmi");
        final BsonDocument document = builder.build();

        final CompletableFuture<InsertResult> cf = collection
                .insert(document, mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
        final InsertResult actual = cf.get(5, TimeUnit.SECONDS);
        assertTrue(actual.ok());
        assertEquals(1, actual.n());
    }

    @Test
    public void insertOneDocumentAndFindOne() throws Exception {
        final BsonDocumentBuilder builder = new BsonDocumentBuilder();
        builder.addString("name", "fehmi");
        final BsonDocument document = builder.build();

        final CompletableFuture<Optional<BsonDocument>> cf = collection
                .insert(document, mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS)))
                .thenCompose(insert ->
                        collection.findOne(mongoClient.ec(), new Timeout(Duration.create(5, TimeUnit.SECONDS))));
        final Optional<BsonDocument> actual = cf.get(5, TimeUnit.SECONDS);
        assertTrue(actual.isPresent());
        assertEquals("fehmi", actual.get().<String>getAs("name").get());
    }
}
