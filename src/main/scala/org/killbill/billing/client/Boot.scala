package org.killbill.billing.client

import akka.actor.ActorDSL._
import akka.actor.{ActorSystem, Props}
import akka.event.Logging
import akka.io.IO
import akka.io.Tcp.{Bound, CommandFailed}
import spray.can.Http

/**
 * Created by jgomez on 20/10/2015.
 */
object Boot {

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("killbill-api-scala-service")
  val log = Logging(system, getClass)

  val callbackActor = actor(new Act {
    become {
      case b @ Bound(connection) => log.info(b.toString)
      case cf @ CommandFailed(command) => log.error(cf.toString)
      case all => log.debug("KillBillClient Api Received a message from Akka.IO: " + all.toString)
    }
  })

  // create and start our service actor
  val service = system.actorOf(Props[KillBillClientServiceActor], "killbill-service")

  // start a new HTTP server on port 8888 with our service actor as the handler
  IO(Http).tell(Http.Bind(service, "localhost", 8888), callbackActor)
}
