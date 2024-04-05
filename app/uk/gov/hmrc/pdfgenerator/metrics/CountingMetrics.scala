/*
 * Copyright 2024 HM Revenue & Customs
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

import java.io.File
import java.util.concurrent.TimeUnit
import com.codahale.metrics.{Gauge, MetricRegistry}

import javax.inject.{Inject, Singleton}
import play.api.Logging

sealed protected trait Timer {

  val prefix: String
  val registry: MetricRegistry

  private def time(diff: Long, unit: TimeUnit): Unit =
    registry.timer(s"$prefix-timer").update(diff, unit)

  def startTimer(): Long = System.currentTimeMillis()

  def endTimer(start: Long): Unit = {
    val end = System.currentTimeMillis() - start
    time(end, TimeUnit.MILLISECONDS)
  }
}

sealed protected trait HealthCheckTimer {
  val prefix: String
  val registry: MetricRegistry

  private def time(diff: Long, unit: TimeUnit): Unit =
    registry.timer(s"$prefix-health-check-timer").update(diff, unit)

  def startHealthCheckTimer(): Long = System.currentTimeMillis()

  def endHealthCheckTimer(start: Long): Unit = {
    val end = System.currentTimeMillis() - start
    time(end, TimeUnit.MILLISECONDS)
  }
}

sealed protected trait Connector {

  val prefix: String
  val registry: MetricRegistry

  def status(code: Int): Unit = registry.counter(s"$prefix-connector-status-$code").inc()
}

sealed trait PDFMetrics extends Timer with Connector with HealthCheckTimer with Logging {
  logger.info(s"[${super.getClass}][constructor] Initialising metrics interface")
  val prefix: String
}

/**
  * CountingMetrics
  * @param name name of the counter
  */
sealed abstract class BasePdfGeneratorMetric(name: String) extends PDFMetrics {

  override val prefix: String = name

  def count(): Unit = registry.counter(s"$prefix-count").inc()

  def successCount(): Unit = registry.counter(s"$prefix-success-count").inc()

  def failureCount(): Unit = registry.counter(s"$prefix-failure-count").inc()
}

@Singleton
class PdfGeneratorMetric @Inject()(val metrics: MetricRegistry)
    extends BasePdfGeneratorMetric("pdf-generator-service") with Logging {

  lazy val gauge = new DiskSpaceGuage()
  override val registry: MetricRegistry = metrics

  registry.register[DiskSpaceGuage](s"$prefix-free-disk-space-gauge", gauge)

  class DiskSpaceGuage() extends Gauge[Int] {

    private val ONE_MILLION = 1000000
    val measureFile = new File("spaceTest.txt")
    measureFile.createNewFile()

    override def getValue: Int =
      try {
        val freeSpace = Math.toIntExact(measureFile.getFreeSpace / ONE_MILLION)
        logger.info(s"Getting free diskspace ${freeSpace}Mb")
        freeSpace
      } catch {
        case e: Throwable =>
          logger.error(s"DiskSpaceGuage: Bad Disk Space value ${e.getMessage}")
          -1
      }
  }
}
