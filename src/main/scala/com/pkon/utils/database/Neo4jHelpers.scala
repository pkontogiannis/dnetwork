package com.pkon.utils.database

import org.neo4j.driver.v1.Value

object Neo4jHelpers {

  def getOptString(opt: Value): Option[String] = {
    if (!opt.isNull) Some(opt.asString()) else None
  }
  def getOptDouble(opt: Value): Option[Double] = {
    if (!opt.isNull) Some(opt.asString().toDouble) else None
  }

}
