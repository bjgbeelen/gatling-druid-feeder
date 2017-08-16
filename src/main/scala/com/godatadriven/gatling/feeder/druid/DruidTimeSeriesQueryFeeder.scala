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

import ing.wbaa.druid.query.{DruidQuery, TimeSeriesResult}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global


class DruidTimeSeriesQueryFeeder[D, T] extends DruidQueryFeeder[TimeSeriesResult[D], D, T]
  with DruidTimeSeriesQueryFeedExecutor[D, T] {

  override def exec[P <: FeedElementBuilder[T]](query: DruidQuery[TimeSeriesResultFeedBuilder[P]])
                                               (
                                                 implicit mf: Manifest[List[TimeSeriesResultFeedBuilder[P]]]
                                               ): Seq[Map[String, T]] = {

    val druidResult = query.execute().map(_.map(_.result).map(_.toFeedElement))

    Await.result(druidResult, 30 seconds)
  }

  override def exec(query: DruidQuery[TimeSeriesResult[D]], transform: (D) => Map[String, T])
                   (implicit mf: Manifest[List[TimeSeriesResult[D]]]): Seq[Map[String, T]] = {

    val druidResult = query.execute().map(_.map(_.result).map(transform))

    Await.result(druidResult, 30 seconds)
  }

}

object DruidTimeSeriesQueryFeeder {

  def apply[D, T](query: DruidQuery[TimeSeriesResult[D]],
                  transform: (D) => Map[String, T]
                 )(implicit mf: Manifest[List[TimeSeriesResult[D]]]): Seq[Map[String, T]] = {
    new DruidTimeSeriesQueryFeeder[D, T]().exec(query, transform)(mf = mf)
  }

  def apply[D <: FeedElementBuilder[T], T](query: DruidQuery[TimeSeriesResult[D]])
                                          (implicit mf: Manifest[List[TimeSeriesResult[D]]]): Seq[Map[String, T]] = {
    new DruidTimeSeriesQueryFeeder[D, T]().exec(query)(mf = mf)
  }
}
