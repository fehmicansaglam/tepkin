package com.github.jeroenr.tepkin

import com.github.simplyscala.{MongodProps, MongoEmbedDatabase}
import de.flapdoodle.embed.mongo.distribution.Version
import org.scalatest.{BeforeAndAfterAll, Suites}

/**
 * Created by jero on 23/02/16.
 */
class MongoIntegrationTests extends Suites(new MongoDatabaseSpec, new MongoCollectionSpec, new GridFsSpec) with BeforeAndAfterAll with MongoEmbedDatabase {
  var mongoProps: MongodProps = null

  override def beforeAll() = {
    mongoProps = mongoStart(version = Version.V3_2_0)
  }

  override def afterAll() = {
    mongoStop(mongoProps)
  }
}
