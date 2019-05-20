package com.pkon.persistence

import com.pkon.service.user.persistence.UserTableDef

trait Schema extends SlickJdbcProfile
  with UserTableDef {

  import profile.api._

  implicit lazy val Users: TableQuery[UserTable] = TableQuery[UserTable]

  lazy val Schema: profile.DDL =
    Users.schema
}

