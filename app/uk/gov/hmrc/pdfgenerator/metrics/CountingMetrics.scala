/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.pdfgenerator.metrics

/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.File
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{Gauge, MetricRegistry}
import play.api.Logger
import uk.gov.hmrc.play.graphite.MicroserviceMetrics

sealed protected trait Timer {
  this: MicroserviceMetrics =>

  val prefix : String

  private def time(diff: Long, unit: TimeUnit) =
    metrics.defaultRegistry.timer(s"$prefix-timer").update(diff, unit)

  def startTimer() : Long = System.currentTimeMillis()

  def endTimer(start: Long) = {
    val end = System.currentTimeMillis() - start
    time(end, TimeUnit.MILLISECONDS)
  }
}

sealed protected trait Connector {
  this: MicroserviceMetrics =>

  val prefix : String

  def status(code: Int) : Unit = metrics.defaultRegistry.counter(s"$prefix-connector-status-$code").inc()
}

sealed trait PDFMetrics extends MicroserviceMetrics with Timer with Connector {
  Logger.info(s"[${super.getClass}][constructor] Initialising metrics interface")

  val prefix : String
}



/**
  * CountingMetrics
  * @param name name of the counter
  */

sealed abstract class BasePdfGeneratorMetric(name : String) extends PDFMetrics {

  override val prefix = name

  def count() = metrics.defaultRegistry.counter(s"$prefix-count").inc()


}

object PdfGeneratorMetric extends BasePdfGeneratorMetric("pdf-generator-service") {


  lazy val guage = new DiskSpaceGuage()

  metrics.defaultRegistry.register[DiskSpaceGuage](s"${prefix}-free-disk-space-guage",
    guage)


  class DiskSpaceGuage() extends Gauge[Int] {

    private val ONE_MILLION = 1000000
    val measureFile = new File("spaceTest.txt")
    measureFile.createNewFile()

    override def getValue: Int = {
      try {

        val freeSpace = Math.toIntExact(measureFile.getFreeSpace / ONE_MILLION)
        Logger.debug(s"Getting free diskspace ${freeSpace}Mb")
        freeSpace
      } catch {
        case e => {
          Logger.error(s"DiskSpaceGuage: Bad Disk Space value ${e.getMessage}" )
          -1
        }
      }
    }
  }
}
