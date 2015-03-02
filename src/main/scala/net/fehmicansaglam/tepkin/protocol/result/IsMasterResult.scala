package net.fehmicansaglam.tepkin.protocol.result

import net.fehmicansaglam.tepkin.bson.BsonDocument
import org.joda.time.DateTime

case class ReplicaSet(setName: String,
                      setVersion: Int,
                      me: String,
                      primary: String,
                      hosts: List[String],
                      passives: Option[List[String]],
                      arbiters: Option[List[String]],
                      isSecondary: Boolean, // `secondary`
                      isArbiterOnly: Option[Boolean], // `arbiterOnly`
                      isPassive: Option[Boolean], // `passive`
                      isHidden: Option[Boolean], // `hidden`
                      tags: Option[BsonDocument])


case class IsMasterResult(isMaster: Boolean, // `ismaster`
                          maxBsonObjectSize: Int = 16 * 1024 * 1024,
                          maxMessageSizeBytes: Int = 48000000, // mongod >= 2.4
                          maxWriteBatchSize: Int = 1000, // mongod >= 2.6
                          localTime: DateTime, // mongod >= 2.2
                          minWireVersion: Int, // mongod >= 2.6
                          maxWireVersion: Int, // mongod >= 2.6
                          replicaSet: Option[ReplicaSet]) extends Result

object IsMasterResult {
  def apply(document: BsonDocument): IsMasterResult = {
    val replicaSet = document.getAs[String]("setName") map { setName =>
      ReplicaSet(
        setName = setName,
        setVersion = document.getAs[Int]("setVersion").get,
        me = document.getAs[String]("me").get,
        primary = document.getAs[String]("primary").get,
        hosts = document.getAsList[String]("hosts").get,
        passives = document.getAsList[String]("passives"),
        arbiters = document.getAsList[String]("arbiters"),
        isSecondary = document.getAs[Boolean]("secondary").get,
        isArbiterOnly = document.getAs[Boolean]("arbiterOnly"),
        isPassive = document.getAs[Boolean]("passive"),
        isHidden = document.getAs[Boolean]("hidden"),
        tags = document.getAs[BsonDocument]("tags")
      )
    }

    IsMasterResult(
      isMaster = document.getAs[Boolean]("ismaster").get,
      maxBsonObjectSize = document.getAs[Int]("maxBsonObjectSize").get,
      maxMessageSizeBytes = document.getAs[Int]("maxMessageSizeBytes").get,
      maxWriteBatchSize = document.getAs[Int]("maxWriteBatchSize").get,
      localTime = document.getAs[DateTime]("localTime").get,
      minWireVersion = document.getAs[Int]("minWireVersion").get,
      maxWireVersion = document.getAs[Int]("maxWireVersion").get,
      replicaSet = replicaSet
    )
  }
}
