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

import ing.wbaa.druid.query.{DruidQuery, GroupByQueryResult}

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global


private[druid] class DruidGroupByQueryFeeder[D, T] extends DruidQueryFeeder[GroupByQueryResult[D], D, T]
  with DruidGroupByQueryFeedExecutor[D, T] {

  override def exec[P <: FeedElementBuilder[T]](query: DruidQuery[GroupByQueryResultFeedBuilder[P]])
                                               (
                                                 implicit mf: Manifest[List[GroupByQueryResultFeedBuilder[P]]]
                                               ): Seq[Map[String, T]] = {

    val druidResult = query.execute().map(_.map(_.event).map(_.toFeedElement))

    Await.result(druidResult, 30 seconds)
  }

  override def exec(query: DruidQuery[GroupByQueryResult[D]], transform: (D) => Map[String, T])
                   (implicit mf: Manifest[List[GroupByQueryResult[D]]]): Seq[Map[String, T]] = {

    val druidResult = query.execute().map(_.map(_.event).map(transform))

    Await.result(druidResult, 30 seconds)
  }

}

/**
  * DruidGroupByQueryFeeder companion object for easy DruidFeeder creation
  */
object DruidGroupByQueryFeeder {

  /**
    * DruidGroupByQueryFeeder creation method that can handle straight case classes and knows how to convert them into
    *   a Gatling feeder.Record[T] (also known as a Map[String, T])
    *
    * @param query a Druid GroupBy Query to request data
    * @param transform function to convert the D case class type to a Map[String, T]
    * @param mf implicit Manifest needed by the scruid library to translate the druid result to a case class
    * @tparam D case class type to which the druid query result can be converted to
    * @tparam T value type of the resulting Map elements
    * @return A Sequence of Map[String, T] that can be used in a Gatling feeder
    */
  def apply[D, T](query: DruidQuery[GroupByQueryResult[D]],
                  transform: (D) => Map[String, T]
                 )(implicit mf: Manifest[List[GroupByQueryResult[D]]]): Seq[Map[String, T]] = {
    new DruidGroupByQueryFeeder[D, T]().exec(query, transform)(mf = mf)
  }

  /**
    * DruidGroupByQueryFeeder creation method that can handle case classes that extend FeedElementBuilder[T] where the
    *   toFeedElement method is used to convert the class into a Gatling feeder.Record[T]
    *   (also known as a Map[String, T])
    *
    * @param query a Druid GroupBy Query to request data
    * @param mf implicit Manifest needed by the scruid library to translate the druid result to a case class
    * @tparam D case class type to which the druid query result can be converted to
    * @tparam T value type of the resulting Map elements
    * @return A Sequence of Map[String, T] that can be used in a Gatling feeder
    */
  def apply[D <: FeedElementBuilder[T], T](query: DruidQuery[GroupByQueryResult[D]])
                                          (implicit mf: Manifest[List[GroupByQueryResult[D]]]): Seq[Map[String, T]] = {
    new DruidGroupByQueryFeeder[D, T]().exec(query)(mf = mf)
  }
}
