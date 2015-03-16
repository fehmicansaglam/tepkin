package net.fehmicansaglam.tepkin.api.options

import java.lang.{Boolean => JavaBoolean}

import net.fehmicansaglam.bson.BsonDocument

case class AggregationOptions(explain: Option[Boolean],
                              allowDiskUse: Option[Boolean],
                              cursor: Option[BsonDocument])

object AggregationOptions {

  class Builder {
    private var explain: Option[Boolean] = None
    private var allowDiskUse: Option[Boolean] = None
    private var cursor: Option[BsonDocument] = None

    def explain(explain: JavaBoolean): Builder = {
      this.explain = Some(explain)
      this
    }

    def allowDiskUse(allowDiskUse: JavaBoolean): Builder = {
      this.allowDiskUse = Some(allowDiskUse)
      this
    }

    def cursor(cursor: BsonDocument): Builder = {
      this.cursor = Some(cursor)
      this
    }

    def build(): AggregationOptions = AggregationOptions(explain, allowDiskUse, cursor)

  }

  def builder(): Builder = new Builder()

}

