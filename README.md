# gatling-druid-feeder
Custom Gatling Feeder voor connecting to a Druid cluster to get info

## How to use

There are 2 different ways you can use this feeder

- With standard case class combined with a conversion method to output the contents of the case class as a Map
- With a case class which extends the FeedElementBuilder

### Example

#### bare case class with conversion method

```scala
import com.godatadriven.gatling.feeder.druid.DruidTimeSeriesQueryFeeder
import ing.wbaa.druid.definitions.{Aggregation, Dimension}
import ing.wbaa.druid.query.TimeSeriesQuery

case class TimeseriesCount(count: Int)

val timeSeriesQuery = TimeSeriesQuery[TimeseriesCount](
    aggregations = List(
      Aggregation(
        kind = "count",
        name = "count",
        fieldName = "count"
      )
    ),
    granularity = "hour",
    intervals = List("2011-06-01/2017-06-01")
  )

def convert(p: TimeseriesCount): Map[String, Int] = {
  Map("counter" -> p.count)
}

val feeder: Seq[Map[String, Int]] = DruidTimeSeriesQueryFeeder[TimeseriesCount, Int](
  timeSeriesQuery, convert _
)

import io.gatling.core.Predef._
import com.godatadriven.gatling.feeder.druid.Predef._

feed(druidFeeder[Int](feeder).circular)

```


#### case class extending the FeedElementBuilder
```scala
import com.godatadriven.gatling.feeder.druid.FeedElementBuilder
import com.godatadriven.gatling.feeder.druid.DruidTimeSeriesQueryFeeder
import ing.wbaa.druid.definitions.{Aggregation, Dimension}
import ing.wbaa.druid.query.TimeSeriesQuery

case class TimeseriesCountWithFeedElementBuilder(count: Int) 
  extends FeedElementBuilder[Int] {
    override def toFeedElement = Map("counter" -> count)
}

val timeSeriesQuery = TimeSeriesQuery[TimeseriesCountWithFeedElementBuilder](
    aggregations = List(
      Aggregation(
        kind = "count",
        name = "count",
        fieldName = "count"
      )
    ),
    granularity = "hour",
    intervals = List("2011-06-01/2017-06-01")
  )

val feeder: Seq[Map[String, Int]] = 
DruidTimeSeriesQueryFeeder[TimeseriesCountWithFeedElementBuilder, Int](
  timeSeriesQuery
)

import io.gatling.core.Predef._
import com.godatadriven.gatling.feeder.druid.Predef._

feed(druidFeeder[Int](feeder).circular)

```