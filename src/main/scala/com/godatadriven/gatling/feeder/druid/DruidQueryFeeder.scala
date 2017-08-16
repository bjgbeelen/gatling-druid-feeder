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

import ing.wbaa.druid.query._

trait FeedElementBuilder[T] {

  def toFeedElement : Map[String, T]
}

trait DruidQueryFeeder[Q, D, T] {

  def exec(query: DruidQuery[Q], transform: (D) => Map[String, T])
          (implicit mf: Manifest[List[Q]]): Seq[Map[String, T]]

}

trait DruidTimeSeriesQueryFeedExecutor[D, T] {
  type TimeSeriesResultFeedBuilder[B <: FeedElementBuilder[T]] = TimeSeriesResult[B]

  def exec[P <: FeedElementBuilder[T]](query: DruidQuery[TimeSeriesResultFeedBuilder[P]])
                                      (implicit mf: Manifest[List[TimeSeriesResultFeedBuilder[P]]]): Seq[Map[String, T]]

}

trait DruidTopNQueryFeedExecutor[D, T] {
  type TopNResultFeedBuilder[B <: FeedElementBuilder[T]] = TopNResult[B]

  def exec[P <: FeedElementBuilder[T]](query: DruidQuery[TopNResultFeedBuilder[P]])
                                      (implicit mf: Manifest[List[TopNResultFeedBuilder[P]]]): Seq[Map[String, T]]

}

trait DruidGroupByQueryFeedExecutor[D, T] {
  type GroupByQueryResultFeedBuilder[B <: FeedElementBuilder[T]] = GroupByQueryResult[B]

  def exec[P <: FeedElementBuilder[T]](query: DruidQuery[GroupByQueryResultFeedBuilder[P]])
                                      (
                                        implicit mf: Manifest[List[GroupByQueryResultFeedBuilder[P]]]
                                      ): Seq[Map[String, T]]

}
