package routes

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._
import com.pkon.service.auth.{AuthRoutes, AuthServiceDefault}
import com.pkon.service.user.UserModel.{UserCreate, UserDto, UserLogin, UserLoginDto}
import com.pkon.service.user.persistence.UserPersistenceSQL
import com.pkon.utils.database.DBAccess
import com.typesafe.config.{Config, ConfigFactory}
import io.circe.generic.auto._
import io.circe.syntax._
import routes.helpers.ServiceSuite

import scala.collection.JavaConverters._

class AuthRoutesIT extends ServiceSuite {

  private val roles: List[String] = config.getStringList("authentication.roles").asScala.toList
  val user = UserCreate("pkont4@gmail.com", "Petros", "Kontogiannis", "password", roles.head)
  val expectedUser = UserDto(UUID.randomUUID().toString, "pkont4@gmail.com", "Petros", "Kontogiannis", roles.head)
  val userLogin = UserLogin(user.email, user.password)

  trait Fixture {
    val dbAccess = DBAccess(system)
    val userPersistence = new UserPersistenceSQL(dbAccess)
    userPersistence.deleteAllUsers
    val authService = new AuthServiceDefault(userPersistence)
    val authRoutes: Route = new AuthRoutes(authService).authRoutes
  }

  "Auth Routes" should {

    "successfully register a user" in new Fixture {
      Post("/api/v01/auth/register", user) ~> authRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val resultUser = responseAs[UserDto]
        assert(
          resultUser.email === expectedUser.email
        )
      }
    }

    "successfully login user" in new Fixture {
      val resultUser: UserDto = Post("/api/v01/auth/register", user) ~> authRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        responseAs[UserDto]
      }

      Post("/api/v01/auth/login", userLogin) ~> authRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val resultUser = responseAs[UserLoginDto]
        assert(
          resultUser.email === expectedUser.email
        )
      }
    }
  }

}
