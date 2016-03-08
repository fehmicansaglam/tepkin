package com.github.jeroenr.tepkin.protocol.command

import com.github.jeroenr.bson.BsonDsl._
import com.github.jeroenr.bson.{BsonDocument}

/**
 * @param key Specifies the index’s fields. For each field, specify a key-value pair in which the key is the name
 *            of the field to index and the value is either the index direction or index type.
 *            If specifying direction, specify 1 for ascending or -1 for descending.
 * @param name A name that uniquely identifies the index.
 * @param background Builds the index in the background so that building an index does not block other database
 *                   activities. Specify true to build in the background. The default value is false.
 * @param unique Creates a unique index so that the collection will not accept insertion of documents where
 *               the index key or keys match an existing value in the index. Specify true to create a unique index.
 *               The default value is false.
 * @param sparse   If true, the index only references documents with the specified field. These indexes use less space
 *                 but behave differently in some situations (particularly sorts). The default value is false.
 * @param expireAfterSeconds Specifies a value, in seconds, as a TTL to control how long MongoDB retains documents in
 *                           this collection.
 * @param storageEngine Allows users to specify configuration to the storage engine on a per-index basis when creating
 *                      an index.
 * @param weights  For text indexes, a document that contains field and weight pairs. The weight is an integer ranging
 *                 from 1 to 99,999 and denotes the significance of the field relative to the other indexed fields
 *                 in terms of the score. You can specify weights for some or all the indexed fields.
 *                 The default value is 1.
 * @param defaultLanguage For text indexes, the language that determines the list of stop words and the rules
 *                        for the stemmer and tokenizer. The default value is english.
 * @param languageOverride For text indexes, the name of the field, in the collection’s documents, that contains
 *                         the override language for the document. The default value is language.
 * @param textIndexVersion  For text indexes, the text index version number. Version can be either 1 or 2.
 * @param _2dSphereIndexVersion For 2dsphere indexes, the 2dsphere index version number. Version can be either 1 or 2.
 * @param bits For 2d indexes, the number of precision of the stored geohash value of the location data.
 * @param min For 2d indexes, the lower inclusive boundary for the longitude and latitude values.
 *            The default value is -180.0.
 * @param max For 2d indexes, the upper inclusive boundary for the longitude and latitude values.
 *            The default value is 180.0.
 * @param bucketSize For geoHaystack indexes, specify the number of units within which to group the location values;
 *                   i.e. group in the same bucket those location values that are within the specified number of units
 *                   to each other. The value must be greater than 0.
 * @param ns The namespace (i.e. <database>.<collection>) of the collection for which to create the index.
 *           If you omit ns, MongoDB generates the namespace.
 */
case class Index(key: BsonDocument,
                 name: String,
                 background: Option[Boolean] = None,
                 unique: Option[Boolean] = None,
                 sparse: Option[Boolean] = None,
                 expireAfterSeconds: Option[Int] = None,
                 storageEngine: Option[BsonDocument] = None,
                 weights: Option[BsonDocument] = None,
                 defaultLanguage: Option[String] = None,
                 languageOverride: Option[String] = None,
                 textIndexVersion: Option[Int] = None,
                 _2dSphereIndexVersion: Option[Int] = None,
                 bits: Option[Int] = None,
                 min: Option[Double] = None,
                 max: Option[Double] = None,
                 bucketSize: Option[Int] = None,
                 ns: Option[String] = None) {
  val toDoc: BsonDocument = {
    ("key" := key) ~
      ("name" := name) ~
      ("background" := background) ~
      ("unique" := unique) ~
      ("sparse" := sparse) ~
      ("expireAfterSeconds" := expireAfterSeconds) ~
      ("storageEngine" := storageEngine) ~
      ("weights" := weights) ~
      ("default_language" := defaultLanguage) ~
      ("language_override" := languageOverride) ~
      ("textIndexVersion" := textIndexVersion) ~
      ("2dsphereIndexVersion" := _2dSphereIndexVersion) ~
      ("bits" := bits) ~
      ("min" := min) ~
      ("max" := max) ~
      ("bucketSize" := bucketSize) ~
      ("ns" := ns)
  }
}

object Index {
  def apply(document: BsonDocument): Index = {
    Index(
      key = document.getAs[BsonDocument]("key").get,
      name = document.getAs[String]("name").get,
      ns = document.getAs[String]("ns"))
  }
}

/**
 * Builds one or more indexes on a collection.
 * @param indexes Specifies the indexes to create. Each document in the array specifies a separate index.
 */
case class CreateIndexes(databaseName: String,
                         collectionName: String,
                         indexes: Index*) extends Command {
  override def command: BsonDocument = {
    ("createIndexes" := collectionName) ~
      ("indexes" := $array(indexes.map(_.toDoc): _*))
  }
}
