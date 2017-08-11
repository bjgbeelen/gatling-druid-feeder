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
import io.gatling.core.feeder.Record
import org.scalatest.{FunSuiteLike, Inside, OptionValues}

case class TimeseriesCount(count: Int)

case class GroupByIsAnonymous(isAnonymous: Boolean, count: Int)

case class TopCountry(count: Int, countryName: String = null)

class DruidQueryFeederTest extends FunSuiteLike with Inside with OptionValues {

  private val totalNumberOfEntries = 39244

  test("Create a Feeder Based on a TimeSeriesQuery") {

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

    val tsQuery = TimeSeriesQuery[TimeseriesCount](
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

    def convert(p: List[TimeseriesCount]): Vector[Record[Int]] = {
      p.map { x: TimeseriesCount =>
        Map("counter" -> x.count)
      }.toVector
    }

    val feeder = DruidTimeSeriesQueryFeeder[TimeseriesCount, Int](tsQuery, convert)

    //  [{"timestamp":"2015-09-12T23:00:00.000Z","result":{"count":1482}},
    //   {"timestamp":"2015-09-12T22:00:00.000Z","result":{"count":1590}},
    //   ...
    //   {"timestamp":"2015-09-12T01:00:00.000Z","result":{"count":1144}},
    //   {"timestamp":"2015-09-12T00:00:00.000Z","result":{"count":268}}
    //  ]

    // The number of entries in the data set
    assert(feeder.map {x: Map[String, Int] => x.getOrElse("counter", -1)}.sum == totalNumberOfEntries)
  }

  test("Create a Feeder Based on a GroupByQuery") {

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

    val gbQuery = GroupByQuery[GroupByIsAnonymous](
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

    def convert(p: List[GroupByIsAnonymous]): Vector[Record[Any]] = {
      p.map { x: GroupByIsAnonymous =>
        Map[String, Any]("isAnonymous" -> x.isAnonymous, "counter" -> x.count)
      }.toVector
    }

    val feeder = DruidGroupByQueryFeeder[GroupByIsAnonymous, Any](gbQuery, convert)

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
    assert(feeder.map {x: Map[String, Any] => x.getOrElse("isAnonymous", "unknown").toString}.distinct.length === 2)

    // True count == 3799
    val trueCount: Int = feeder.flatMap {
      case x: Map[String, Any] if x("isAnonymous").asInstanceOf[Boolean] =>
        Some(x.getOrElse("counter", -1).asInstanceOf[Int])
      case _ => None
    }.head
    assert( trueCount === 3799)
  }


  test("Create a Feeder Based on a TopN Query") {

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

    val threshold = 5

    val tnQuery = TopNQuery[TopCountry](
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

    def convert(p: List[TopCountry]): Vector[Record[Any]] = {
      p.map { x: TopCountry =>
        Map[String, Any]("name" -> x.countryName, "counter" -> x.count)
      }.toVector
    }

    val feeder = DruidTopNQueryFeeder[TopCountry, Any](tnQuery, convert)

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

    assert(feeder.length == threshold)
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