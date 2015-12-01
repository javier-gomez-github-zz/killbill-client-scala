[![Build Status](https://travis-ci.org/jgomez-vp/killbill-client-scala.svg?branch=master)](https://travis-ci.org/jgomez-vp/killbill-client-scala)
[![Coverage Status](https://coveralls.io/repos/jgomez-vp/killbill-client-scala/badge.svg?branch=master&service=github)](https://coveralls.io/github/jgomez-vp/killbill-client-scala?branch=master)
[![License](http://img.shields.io/:license-Apache%202-red.svg)](http://www.apache.org/licenses/LICENSE-2.0.txt)

killbill-client-scala
====================

Scala client library for Kill Bill developed using Scala native code and the Spray / Akka frameworks

Usage
-----

1- Clone this repository.

2- SBT must be installed before proceeding.

3- Run "sbt publish-local" and wait until all the necessary files are generated.

4- In the application that will use this Client, edit the "build.sbt" file and add the following dependency:
    libraryDependencies ++= {
      Seq(
        "org.killbill" %% "killbill-api-client-scala" % "0.1"
      )
    }

5- Import the KillBillClient.scala library and Declare the new client by passing your killBillUrl and the necessary headers (examples following):

    import org.killbill.billing.client.actor.KillBillClient
    val killBillUrl = "http://localhost:8080/1.0/kb"
    val headers = List(
        HttpHeaders.Authorization.apply(BasicHttpCredentials.apply("user", "password")),
        HttpHeaders.RawHeader.apply("X-Killbill-CreatedBy", "user"),
        HttpHeaders.RawHeader.apply("X-Killbill-ApiKey", "tenantApiKey"),
        HttpHeaders.RawHeader.apply("X-Killbill-ApiSecret", "tenantApiSecret")
      )

    val client: KillBillClient = new KillBillClient(killBillUrl, headers)

6- Use the declared client to call all the methods that will access the KillBill Core (example following):

    println("Accounts: " + client.getAccounts())
