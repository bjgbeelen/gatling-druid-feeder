package com.godatadriven.gatling.feeder.druid

import ing.wbaa.druid.query.{DruidQuery, GroupByQueryResult}
import io.gatling.core.feeder.Record

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class DruidGroupByQueryFeeder[D, T] extends DruidQueryFeeder[GroupByQueryResult[D], D, T] {

  override def exec(query: DruidQuery[GroupByQueryResult[D]], transform: (List[D]) => Vector[Record[T]])
                   (implicit mf: Manifest[List[GroupByQueryResult[D]]]): Vector[Record[T]] = {

    val druidResult = query.execute().map(_.map(_.event)).map(transform)

    Await.result(druidResult, 30 seconds)
  }

}

object DruidGroupByQueryFeeder {
  def apply[D, T](query: DruidQuery[GroupByQueryResult[D]],
                  transform: (List[D]) => Vector[Record[T]]
                 )(implicit mf: Manifest[List[GroupByQueryResult[D]]]): IndexedSeq[Record[T]] = {
    new DruidGroupByQueryFeeder[D, T]().exec(query, transform)(mf = mf)
  }
}
