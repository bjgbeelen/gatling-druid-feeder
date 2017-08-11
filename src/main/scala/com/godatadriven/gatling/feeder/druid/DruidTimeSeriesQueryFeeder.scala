package com.godatadriven.gatling.feeder.druid

import ing.wbaa.druid.query.{DruidQuery, TimeSeriesResult}
import io.gatling.core.feeder.Record

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global


class DruidTimeSeriesQueryFeeder[D, T] extends DruidQueryFeeder[TimeSeriesResult[D], D, T] {

  override def exec(query: DruidQuery[TimeSeriesResult[D]], transform: (List[D]) => Vector[Record[T]])
                   (implicit mf: Manifest[List[TimeSeriesResult[D]]]): Vector[Record[T]] = {

    val druidResult = query.execute().map(_.map(_.result)).map(transform)

    Await.result(druidResult, 30 seconds)
  }

}

object DruidTimeSeriesQueryFeeder {
  def apply[D, T](query: DruidQuery[TimeSeriesResult[D]],
                  transform: (List[D]) => Vector[Record[T]]
                 )(implicit mf: Manifest[List[TimeSeriesResult[D]]]): IndexedSeq[Record[T]] = {
    new DruidTimeSeriesQueryFeeder[D, T]().exec(query, transform)(mf = mf)
  }
}
