/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.pdfgenerator.utils

/**
  * Describes a command line option
  */
trait Parameter[T] {

  /**
    * The underlying value of this option
    */
  private var underlying: Option[T] = None

  /**
    * The commandline name for this parameter
    */
  val name: String

  /**
    * The optional default value for this parameter
    */
  val default: Option[T] = None

  /**
    * Sets a new value for this parameter
    */
  def :=(newValue: T): Unit = underlying = Some(newValue)

  /**
    * Converts this option to a sequence of strings to be appended to the
    * command line
    */
  def toParameter(implicit shower: ParamShow[T]): Iterable[String] = value match {
    case Some(v) => shower.show(name, v)
    case _       => Iterable.empty
  }

  /**
    * Provides the current value for the option
    */
  private def value: Option[T] = underlying orElse default

}

object Parameter {

  /**
    * Creates a new CommandOption with the specified name and default value
    */
  def apply[T: ParamShow](commandName: String, defaultValue: T): Parameter[T] =
    new Parameter[T] {
      override val name: String = commandName
      override val default: Option[T] = Some(defaultValue)
    }

  /**
    * Creates a new CommandOption with the specified name
    */
  def apply[T: ParamShow](commandName: String): Parameter[T] =
    new Parameter[T] {
      override val name: String = commandName
      override val default: Option[T] = None
    }

}
