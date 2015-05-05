package net.fehmicansaglam.pide.examples

import java.util.UUID

import akka.util.Timeout
import net.fehmicansaglam.bson.BsonDocument
import net.fehmicansaglam.bson.BsonDsl._
import net.fehmicansaglam.pide.{Dao, Entity, StringPide}
import net.fehmicansaglam.tepkin.{MongoClient, MongoCollection}

import scala.concurrent.Await
import scala.concurrent.duration._


object Example2 extends App {

  val client = MongoClient("mongodb://localhost")
  val db = client("tepkin")

  case class Person(id: String,
                    name: String,
                    surname: String,
                    age: Int) extends Entity[String]

  object PersonDao extends Dao[String, Person] {
    override val collection: MongoCollection = db("person")
  }

  implicit object PersonPide extends StringPide[Person] {

    override def read(document: BsonDocument): Person = {
      Person(
        id = document.getAs[String]("_id").get,
        name = document.getAs[String]("name").get,
        surname = document.getAs[String]("surname").get,
        age = document.getAs[Int]("age").get
      )
    }

    override def write(person: Person): BsonDocument = {
      ("_id" := person.id) ~
        ("name" := person.name) ~
        ("surname" := person.surname) ~
        ("age" := person.age)
    }
  }

  val person1 = Person(UUID.randomUUID().toString, "name1", "surname1", 16)
  val person2 = Person(UUID.randomUUID().toString, "name2", "surname2", 32)

  import client.ec

  implicit val timeout: Timeout = 5.seconds

  val result = for {
    insert1 <- PersonDao.insert(person1)
    insert2 <- PersonDao.insert(person2)
    drop <- PersonDao.collection.drop()
  } yield drop

  Await.ready(result, 30.seconds)

  client.shutdown()
}
