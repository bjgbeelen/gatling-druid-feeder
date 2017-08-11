package com.godatadriven.gatling.feeder.druid

import ing.wbaa.druid.query._
import io.gatling.core.feeder.Record

trait DruidQueryFeeder[Q, D, T] {

  def exec(query: DruidQuery[Q], transform: (List[D]) => Vector[Record[T]])
          (implicit mf: Manifest[List[Q]]): Vector[Record[T]]

}
