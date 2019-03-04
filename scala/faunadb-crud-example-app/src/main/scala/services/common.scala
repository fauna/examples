package services

import scala.concurrent.ExecutionContext

/**
  * Base trait for implementing Domain Services.
  */
trait Service {
  /*
   * Note: using Scala's global ExecutionContext for keeping
   * the example with a simple design. Evaluate the use of a
   * dedicated ExecutionContext for the service layer
   * (bulkheading) in a prod environment.
   */
  implicit protected val ec: ExecutionContext = ExecutionContext.global
}
