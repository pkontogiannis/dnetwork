package com.pkon.service.user

import java.util.UUID

import scala.language.{higherKinds, implicitConversions}

object UserModel {

  case class Usr[F[_]](id: Option[Int],
                       userId: F[UUID],
                       email: F[String],
                       firstName: F[String],
                       lastName: F[String],
                       balance: F[BigDecimal])

  case class User(id: Option[Int] = None,
                  userId: String,
                  email: String,
                  password: String,
                  firstName: String,
                  lastName: String,
                  role: String)

  def updateUserToUser(userId: String, updateUser: UpdateUser): User =
    User(
      userId = updateUser.userId.getOrElse(userId),
      email = updateUser.email.getOrElse(""),
      password = updateUser.password.getOrElse(""),
      firstName = updateUser.firstName.getOrElse(""),
      lastName = updateUser.lastName.getOrElse(""),
      role = updateUser.role.getOrElse("")
    )

  def updateUserRow(old: User, update: UpdateUser): User =
    old.copy(
      email = update.email.getOrElse(old.email),
      firstName = update.firstName.getOrElse(old.firstName),
      lastName = update.lastName.getOrElse(old.lastName),
      password = update.password.getOrElse(old.password),
      role = update.role.getOrElse(old.role)
    )

  case class UpdateUser(
                         userId: Option[String],
                         password: Option[String],
                         email: Option[String],
                         firstName: Option[String],
                         lastName: Option[String],
                         role: Option[String]
                       )

  case class UserDto(userId: String,
                     email: String,
                     firstName: String,
                     lastName: String,
                     role: String)

  implicit def userToUserDto(user: User): UserDto = {
    UserDto(user.userId, user.email, user.firstName, user.lastName, user.role)
  }

  case class UserLogin(email: String, password: String)

  case class UserLoginDto(email: String, accessToken: Token, refreshToken: Token,
                          role: String, tokenType: String)

  case class UserCreate(email: String,
                        firstName: String,
                        lastName: String,
                        password: String,
                        role: String
                       )

  case class Token(token: String, expiresIn: Int)

}
