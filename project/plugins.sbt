addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.8")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.4")

scalacOptions ++= Seq("-deprecation", "-unchecked", "-language:_")

addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.12.2")

addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
