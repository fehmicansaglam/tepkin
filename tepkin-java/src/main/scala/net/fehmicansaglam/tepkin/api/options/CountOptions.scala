package net.fehmicansaglam.tepkin.api.options

case class CountOptions(limit: Option[Int],
                        skip: Option[Int])

object CountOptions {

  class Builder {
    private var limit: Option[Int] = None
    private var skip: Option[Int] = None

    def limit(limit: Integer): Builder = {
      this.limit = Some(limit)
      this
    }

    def skip(skip: Integer): Builder = {
      this.skip = Some(skip)
      this
    }

    def build(): CountOptions = CountOptions(limit, skip)
  }

  def builder(): Builder = new Builder()

}
