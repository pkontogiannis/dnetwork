package com.pkon.persistence

import akka.actor.ActorSystem
import com.pkon.utils.database.DBAccess
import slick.jdbc.JdbcProfile

trait SlickJdbcProfile {
  val actorSystem: ActorSystem

  lazy val dbAccess: DBAccess = DBAccess(actorSystem)

  lazy val profile: JdbcProfile = slick.jdbc.PostgresProfile

}
