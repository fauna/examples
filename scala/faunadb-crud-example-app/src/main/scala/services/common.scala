package services

import scala.concurrent.ExecutionContext

/**
  * Base trait for implementing Domain Services
  */
trait Service {
  // Note: using Scala's global ExecutionContext for now.
  // Evaluate the use of a dedicated ExecutionContext for
  // the services layer (bulkheading) in a prod environment.
  implicit protected val ec: ExecutionContext = ExecutionContext.global
}
