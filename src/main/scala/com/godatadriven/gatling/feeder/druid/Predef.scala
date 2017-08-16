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

import io.gatling.core.feeder.RecordSeqFeederBuilder

object Predef {

  /**
    *
    * @param feeder The Sequence of Map[String, T] elements that needs to be turned into a RecordSeqFeederBuilder[T]
    * @tparam T type of the elements that the feeder considers the values
    * @return
    */
  def druidFeeder[T](feeder: Seq[Map[String, T]]) : RecordSeqFeederBuilder[T] =
    RecordSeqFeederBuilder[T](feeder.toIndexedSeq)
}


