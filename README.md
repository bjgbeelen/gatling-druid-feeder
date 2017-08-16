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

## How to build

### Testing

Test need a druid docker container running. The command for that is:
```bash
docker run --rm -i -p 8082:8082 -p 8081:8081 fokkodriesprong/docker-druid
```
This is the same docker image used for testing the scruid library

Testing can be run with:
```bash
sbt test
```

### Publishing

#### Requirements

##### Sonatype login
For publishing a sonatype login is required. 
Credential details can be put in a file called `~/.sbt/0.13/sonatype.sbt` in the format
```text
credentials += Credentials("Sonatype Nexus Repository Manager",
        "oss.sonatype.org",
        "<username>",
        "<password>")
```

##### GPG key
The sbt-gpg plugin uses the gpg commandline tool. On a Mac this can be installed and configured with the following commands:
```bash
brew install gnupg gnupg2
gpg --gen-key
gpg --list-keys
gpg --keyserver hkp://pgp.mit.edu --send-keys <KEY-UUID>
```

#### Publishing a SNAPSHOT version

```bash
export GPG_TTY=$(tty)
sbt publishSigned
```


#### Publishing a RELEASE version

After changing the version in build.sbt to a non snapshot version number

```bash
export GPG_TTY=$(tty)
sbt publishSigned
sbt sonatypeRelease
```