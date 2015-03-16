package net.fehmicansaglam.tepkin.api;

import akka.stream.ActorFlowMaterializer;
import akka.stream.FlowMaterializer;
import akka.stream.javadsl.Source;
import net.fehmicansaglam.bson.BsonDocument;
import net.fehmicansaglam.tepkin.api.options.AggregationOptions;
import net.fehmicansaglam.tepkin.protocol.result.CountResult;
import net.fehmicansaglam.tepkin.protocol.result.InsertResult;
import org.junit.*;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MongoCollectionTest {

    private static MongoClient mongoClient;
    private static MongoCollection collection;
    private static final FiniteDuration timeout = Duration.create(5, TimeUnit.SECONDS);
    private static final FiniteDuration longTimeout = Duration.create(20, TimeUnit.SECONDS);

    @BeforeClass
    public static void setUpClass() {
        mongoClient = MongoClient.create("mongodb://localhost");
        collection = mongoClient.db("tepkin").collection("mongo_collection_test");
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        mongoClient.shutdown();
        mongoClient = null;
    }

    @Before
    public void setUp() throws Exception {
        collection.drop(mongoClient.ec(), timeout).get();
    }

    @After
    public void tearDown() throws Exception {
        collection.drop(mongoClient.ec(), timeout).get();
    }

    @Test
    public void countCollection() throws Exception {
        final CompletableFuture<CountResult> cf = collection
                .count(mongoClient.ec(), timeout);
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
                .insert(document, mongoClient.ec(), timeout);
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
                .insert(document, mongoClient.ec(), timeout)
                .thenCompose(insert -> collection.findOne(mongoClient.ec(), timeout));
        final Optional<BsonDocument> actual = cf.get(5, TimeUnit.SECONDS);
        assertTrue(actual.isPresent());
        assertEquals("fehmi", actual.get().<String>getAs("name").get());
    }


    @Test
    public void insertAndFind100000Documents() throws Exception {
        List<List<BsonDocument>> documents = new ArrayList<>(100);
        for (int i = 0; i < 100; i++) {
            List<BsonDocument> row = new ArrayList<>(100);
            for (int j = 0; j < 1000; j++) {
                BsonDocumentBuilder builder = new BsonDocumentBuilder();
                builder.addString("name", "fehmi" + j);
                row.add(builder.build());
            }
            documents.add(row);
        }

        final FlowMaterializer mat = ActorFlowMaterializer.create(mongoClient.context());

        CompletableFuture<Integer> cf = JavaConverters.toCompletableFuture(
                collection
                        .insertFromSource(Source.from(documents), mongoClient.ec(), longTimeout)
                        .runForeach(result -> {
                        }, mat),
                mongoClient.ec())
                .thenCompose(result -> collection.find(BsonDocument.empty(), mongoClient.ec(), timeout))
                .thenCompose(source -> JavaConverters.toCompletableFuture(
                        source.runFold(0, (accu, docs) -> accu + docs.size(), mat),
                        mongoClient.ec()
                ));

        final int total = cf.get(20, TimeUnit.SECONDS);
        assertEquals(100000, total);
    }

    @Test
    public void groupByAndCalculateSum() throws Exception {
        final FlowMaterializer mat = ActorFlowMaterializer.create(mongoClient.context());

        List<BsonDocument> documents = Arrays.asList(
                new BsonDocumentBuilder()
                        .addInt("_id", 1)
                        .addString("cust_id", "abc1")
                        .addString("status", "A")
                        .addInt("amount", 50)
                        .build(),
                new BsonDocumentBuilder()
                        .addInt("_id", 2)
                        .addString("cust_id", "xyz1")
                        .addString("status", "A")
                        .addInt("amount", 100)
                        .build(),
                new BsonDocumentBuilder()
                        .addInt("_id", 3)
                        .addString("cust_id", "xyz1")
                        .addString("status", "D")
                        .addInt("amount", 25)
                        .build(),
                new BsonDocumentBuilder()
                        .addInt("_id", 4)
                        .addString("cust_id", "xyz1")
                        .addString("status", "D")
                        .addInt("amount", 125)
                        .build(),
                new BsonDocumentBuilder()
                        .addInt("_id", 5)
                        .addString("cust_id", "abc1")
                        .addString("status", "A")
                        .addInt("amount", 25)
                        .build()
        );

        List<BsonDocument> pipeline = Arrays.asList(
                new BsonDocumentBuilder()
                        .add("$match", new BsonDocumentBuilder().addString("status", "A").build())
                        .build(),
                new BsonDocumentBuilder()
                        .add("$group", new BsonDocumentBuilder()
                                .addString("_id", "$cust_id")
                                .add("total", new BsonDocumentBuilder().addString("$sum", "$amount").build())
                                .build())
                        .build(),
                new BsonDocumentBuilder()
                        .add("$sort", new BsonDocumentBuilder().addInt("total", -1).build())
                        .build()
        );

        CompletableFuture<List<BsonDocument>> cf = collection.insert(documents, mongoClient.ec(), timeout)
                .thenCompose(insert -> collection.aggregate(pipeline,
                        AggregationOptions.builder().allowDiskUse(true).build(),
                        mongoClient.ec(),
                        timeout))
                .thenCompose(aggregate -> {
                    List<BsonDocument> zero = new ArrayList<>(0);
                    Future<List<BsonDocument>> f = aggregate.runFold(zero, (accu, list) -> {
                        accu.addAll(list);
                        return accu;
                    }, mat);
                    return JavaConverters.toCompletableFuture(f, mongoClient.ec());
                });

        List<BsonDocument> results = cf.get(20, TimeUnit.SECONDS);
        assertEquals(2, results.size());
    }

}
