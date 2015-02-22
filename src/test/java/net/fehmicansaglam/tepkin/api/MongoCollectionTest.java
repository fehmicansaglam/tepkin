package net.fehmicansaglam.tepkin.api;

import akka.util.Timeout;
import net.fehmicansaglam.tepkin.MongoClient;
import net.fehmicansaglam.tepkin.protocol.result.CountResult;
import org.junit.*;
import scala.concurrent.duration.Duration;

import java.io.IOException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
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
    public void tearDown() throws IOException {
        collection.drop(mongoClient.context().dispatcher(), new Timeout(Duration.create(5, TimeUnit.SECONDS)));
    }

    @Test
    public void countCollection() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CompletionStage<CountResult> cs = collection
                .count(mongoClient.context().dispatcher(), new Timeout(Duration.create(5, TimeUnit.SECONDS)))
                .thenApply(result -> {
                    latch.countDown();
                    return result;
                });
        latch.await(5, TimeUnit.SECONDS);
        final CountResult actual = cs.toCompletableFuture().get();
        assertTrue(actual.ok());
        assertEquals(0L, actual.n());
    }
}
