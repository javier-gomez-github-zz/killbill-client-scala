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
