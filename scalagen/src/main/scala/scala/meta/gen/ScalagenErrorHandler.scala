package scala.meta.gen

import com.typesafe.scalalogging.Logger

case class FatalGenerationException(message: String) extends RuntimeException(message)

trait ScalagenErrorHandler {

  private val logger = Logger(classOf[ScalagenErrorHandler])

  def abort(message: String): Nothing =
    throw FatalGenerationException(message)

  def error(message: String): Unit =
    logger.error(message)

  def warn(message: String): Unit =
    logger.warn(message)

  def info(message: String): Unit =
    logger.info(message)

  def debug(message: String): Unit =
    logger.debug(message)
}

object ScalagenErrorHandler extends ScalagenErrorHandler
