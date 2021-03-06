package com.pkon.service.auth

import com.pkon.service.errors.DatabaseError
import com.pkon.service.errors.ServiceError.AuthenticationError
import com.pkon.service.user.UserModel.{Token, UserCreate, UserDto, UserLogin, UserLoginDto}

import scala.concurrent.Future


trait AuthService {

  def loginUser(userLogin: UserLogin): Future[Either[AuthenticationError, UserLoginDto]]

  def registerUser(userRegister: UserCreate): Future[Either[DatabaseError, UserDto]]

  def getAccessToken(userId: String, role: String): Future[Either[AuthenticationError, Token]]

  def getRefreshToken(userId: String, role: String): Future[Either[AuthenticationError, Token]]

}
