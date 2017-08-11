package com.godatadriven.gatling.feeder.druid

import ing.wbaa.druid.query.{DruidQuery, TopNResult}
import io.gatling.core.feeder.Record

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class DruidTopNQueryFeeder[D, T] extends DruidQueryFeeder[TopNResult[D], D, T] {

  override def exec(query: DruidQuery[TopNResult[D]], transform: (List[D]) => Vector[Record[T]])
                   (implicit mf: Manifest[List[TopNResult[D]]]): Vector[Record[T]] = {

    val druidResult = query.execute().map(_.flatMap(_.result)).map(transform)

    Await.result(druidResult, 30 seconds)
  }

}

object DruidTopNQueryFeeder {
  def apply[D, T](query: DruidQuery[TopNResult[D]],
                  transform: (List[D]) => Vector[Record[T]]
                 )(implicit mf: Manifest[List[TopNResult[D]]]): IndexedSeq[Record[T]] = {
    new DruidTopNQueryFeeder[D, T]().exec(query, transform)(mf = mf)
  }
}
