package org.killbill.billing.client.model

import spray.json.{DefaultJsonProtocol, JsonFormat}

case class ResponseUri(
  uri: Option[String]
)

case class ResponseUriResult[T](
  uri: Option[String]
)

object ResponseUriJsonProtocol extends DefaultJsonProtocol {
  implicit val responseUriFormat = jsonFormat1(ResponseUri)
  implicit def responseUriResultFormat[T :JsonFormat] = jsonFormat1(ResponseUriResult.apply[T])
}
