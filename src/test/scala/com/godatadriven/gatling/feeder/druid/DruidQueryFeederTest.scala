/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.godatadriven.gatling.feeder.druid

import ing.wbaa.druid.definitions.{Aggregation, Dimension}
import ing.wbaa.druid.query.{GroupByQuery, TimeSeriesQuery, TopNQuery}
import org.scalatest.{FunSuiteLike, Inside, OptionValues}

case class TimeseriesCount(count: Int)

case class TimeseriesCountWithFeedElementBuilder(count: Int) extends FeedElementBuilder[Int] {
  override def toFeedElement = Map("counter" -> count)
}

case class GroupByIsAnonymous(isAnonymous: Boolean, count: Int)

case class GroupByIsAnonymousWithFeedElementBuilder(isAnonymous: Boolean, count: Int) extends FeedElementBuilder[Any] {
  override def toFeedElement = Map("isAnonymous" -> isAnonymous, "counter" -> count)
}

case class TopCountry(count: Int, countryName: String = null)

case class TopCountryWithFeedElementBuilder(count: Int, countryName: String = null) extends FeedElementBuilder[Any] {
  override def toFeedElement = Map("name" -> countryName, "counter" -> count)
}

class DruidQueryFeederTest extends FunSuiteLike with Inside with OptionValues {

  private val totalNumberOfEntries = 39244

  test("Create a Feeder Based on a TimeSeriesQuery") {

    val feeder = DruidTimeSeriesQueryFeeder[TimeseriesCountWithFeedElementBuilder, Int](
      DruidQueryFeederTest.timeSeriesQuery[TimeseriesCountWithFeedElementBuilder]
    )

    assertTimeSeriesResult(feeder)
  }

  test("Create a Feeder Based on a TimeSeriesQuery with converter method") {

    def convert(p: TimeseriesCount): Map[String, Int] = {
      Map("counter" -> p.count)
    }

    val feeder = DruidTimeSeriesQueryFeeder[TimeseriesCount, Int](
      DruidQueryFeederTest.timeSeriesQuery[TimeseriesCount], convert _
    )

    assertTimeSeriesResult(feeder)
  }

  test("Create a Feeder Based on a TimeSeriesQuery with converter method on class with feed element builder") {

    def convert(p: TimeseriesCountWithFeedElementBuilder): Map[String, Int] = {
      Map("counter" -> p.count)
    }

    val feeder = DruidTimeSeriesQueryFeeder[TimeseriesCountWithFeedElementBuilder, Int](
      DruidQueryFeederTest.timeSeriesQuery[TimeseriesCountWithFeedElementBuilder], convert _
    )

    assertTimeSeriesResult(feeder)
  }

  private def assertTimeSeriesResult(feeder: Seq[Map[String, Int]]) = {
    //  [{"timestamp":"2015-09-12T23:00:00.000Z","result":{"count":1482}},
    //   {"timestamp":"2015-09-12T22:00:00.000Z","result":{"count":1590}},
    //   ...
    //   {"timestamp":"2015-09-12T01:00:00.000Z","result":{"count":1144}},
    //   {"timestamp":"2015-09-12T00:00:00.000Z","result":{"count":268}}
    //  ]

    // The number of entries in the data set
    assert(feeder.map { x: Map[String, Int] => x.getOrElse("counter", -1) }.sum == totalNumberOfEntries)
  }

  test("Create a Feeder Based on a GroupByQuery") {

    val feeder = DruidGroupByQueryFeeder[GroupByIsAnonymousWithFeedElementBuilder, Any](
      DruidQueryFeederTest.groupByQuery[GroupByIsAnonymousWithFeedElementBuilder]
    )

    assertGroupByResult(feeder)
  }


  test("Create a Feeder Based on a GroupByQuery with converter method") {

    def convert(p: GroupByIsAnonymous): Map[String, Any] = {
      Map[String, Any]("isAnonymous" -> p.isAnonymous, "counter" -> p.count)
    }

    val feeder = DruidGroupByQueryFeeder[GroupByIsAnonymous, Any](
      DruidQueryFeederTest.groupByQuery[GroupByIsAnonymous], convert _
    )

    assertGroupByResult(feeder)
  }

  test("Create a Feeder Based on a GroupByQuery with converter method on class with feed element builder") {

    def convert(p: GroupByIsAnonymousWithFeedElementBuilder): Map[String, Any] = {
      Map[String, Any]("isAnonymous" -> p.isAnonymous, "counter" -> p.count)
    }

    val feeder = DruidGroupByQueryFeeder[GroupByIsAnonymousWithFeedElementBuilder, Any](
      DruidQueryFeederTest.groupByQuery[GroupByIsAnonymousWithFeedElementBuilder], convert _
    )

    assertGroupByResult(feeder)
  }


  private def assertGroupByResult(feeder: Seq[Map[String, Any]]) = {
    //  [{
    //    "version": "v1",
    //    "timestamp": "2011-06-01T00:00:00.000Z",
    //    "event": {
    //    "count": 35445,
    //    "isAnonymous": "false"
    //  }
    //  }, {
    //    "version": "v1",
    //    "timestamp": "2011-06-01T00:00:00.000Z",
    //    "event": {
    //    "count": 3799,
    //    "isAnonymous": "true"
    //  }
    //  }]

    // True and False are available in the feeder
    assert(feeder.map { x: Map[String, Any] => x.getOrElse("isAnonymous", "unknown").toString }.distinct.length === 2)

    // True count == 3799
    val trueCount: Int = feeder.flatMap {
      case x: Map[String, Any] if x("isAnonymous").asInstanceOf[Boolean] =>
        Some(x.getOrElse("counter", -1).asInstanceOf[Int])
      case _ => None
    }.head
    assert(trueCount === 3799)
  }

  test("Create a Feeder Based on a TopN Query") {

    val feeder = DruidTopNQueryFeeder[TopCountryWithFeedElementBuilder, Any](
      DruidQueryFeederTest.topNQuery[TopCountryWithFeedElementBuilder]
    )

    assertTopNResult(feeder)
  }

  test("Create a Feeder Based on a TopN Query with converter method") {

    def convert(p: TopCountry): Map[String, Any] = {
      Map[String, Any]("name" -> p.countryName, "counter" -> p.count)
    }

    val feeder = DruidTopNQueryFeeder[TopCountry, Any](DruidQueryFeederTest.topNQuery[TopCountry], convert _)

    assertTopNResult(feeder)
  }

  test("Create a Feeder Based on a TopN Query with converter method on class with feed element builder") {

    def convert(p: TopCountryWithFeedElementBuilder): Map[String, Any] = {
      Map[String, Any]("name" -> p.countryName, "counter" -> p.count)
    }

    val feeder = DruidTopNQueryFeeder[TopCountryWithFeedElementBuilder, Any](
      DruidQueryFeederTest.topNQuery[TopCountryWithFeedElementBuilder], convert _
    )

    assertTopNResult(feeder)
  }

  private def assertTopNResult(feeder: Seq[Map[String, Any]]) = {
    //  [{
    //    "timestamp": "2015-09-12T00:46:58.771Z",
    //    "result": [{
    //    "count": 35445,
    //    "countryName": null
    //  }, {
    //    "count": 528,
    //    "countryName": "United States"
    //  }, {
    //    "count": 256,
    //    "countryName": "Italy"
    //  }, {
    //    "count": 234,
    //    "countryName": "United Kingdom"
    //  }, {
    //    "count": 205,
    //    "countryName": "France"
    //  }]
    //  }]


    assert(feeder.length == DruidQueryFeederTest.threshold)
    assert(feeder.head.getOrElse("counter", -1) === 35445)

    assert(feeder(1).getOrElse("counter", -1) === 528)
    assert(feeder(1).getOrElse("name", "") === "United States")

    assert(feeder(2).getOrElse("counter", -1) === 256)
    assert(feeder(2).getOrElse("name", "") === "Italy")

    assert(feeder(3).getOrElse("counter", -1) === 234)
    assert(feeder(3).getOrElse("name", "") === "United Kingdom")

    assert(feeder(4).getOrElse("counter", -1) === 205)
    assert(feeder(4).getOrElse("name", "") === "France")
  }


}

object DruidQueryFeederTest {
  val threshold = 5

  //  {
  //    "queryType": "topN",
  //    "dimension": {
  //      "dimension": "countryName",
  //      "outputName": null,
  //      "outputType": null,
  //      "dimensionType": "default"
  //    },
  //    "threshold": 5,
  //    "filter": null,
  //    "metric": "count",
  //    "granularity": "all",
  //    "aggregations": [{
  //      "type": "count",
  //      "name": "count",
  //      "fieldName": "count/"
  //    }],
  //    "intervals": ["2011-06-01/2017-06-01"],
  //    "dataSource": "wikiticker"
  //  }
  def topNQuery[T] = TopNQuery[T](
    dimension = Dimension(
      dimension = "countryName"
    ),
    threshold = threshold,
    metric = "count",
    aggregations = List(
      Aggregation(
        kind = "count",
        name = "count",
        fieldName = "count"
      )
    ),
    intervals = List("2011-06-01/2017-06-01")
  )

  //  {
  //    "queryType": "groupBy",
  //    "dimensions": [],
  //    "granularity": "all",
  //    "aggregations": [{
  //      "type": "count",
  //      "name": "count",
  //      "fieldName": "count"
  //    }],
  //    "dimension": ["isAnonymous"],
  //    "intervals": ["2011-06-01/2017-06-01"],
  //    "dataSource": "wikiticker"
  //  }
  def groupByQuery[T] = GroupByQuery[T](
    aggregations = List(
      Aggregation(
        kind = "count",
        name = "count",
        fieldName = "count"
      )
    ),
    dimensions = List(Dimension(dimension = "isAnonymous")),
    intervals = List("2011-06-01/2017-06-01")
  )


  //  {
  //    "queryType": "timeseries",
  //    "granularity": "week",
  //    "descending": "true",
  //    "filter": null,
  //    "aggregations": [{
  //      "type": "count",
  //      "name": "count",
  //      "fieldName": "count"
  //    }],
  //    "dataSource": "wikiticker",
  //    "intervals": ["2016-06-01/2017-06-01"]
  //  }
  def timeSeriesQuery[T] = TimeSeriesQuery[T](
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

}