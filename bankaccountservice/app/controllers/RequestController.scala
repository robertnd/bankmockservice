package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.libs.json._
import services.AccountService
import java.time.LocalDateTime
import java.time.ZoneOffset
import models.Transaction
import models.Response

@Singleton
class RequestController @Inject() (cc: ControllerComponents, as: AccountService) extends AbstractController(cc) {
  def getBalance(account_id: String) = Action {
    as.getAccount(account_id) match {
      case Left(msg)  => Ok(Json.toJson(Response("Error", msg)))
      case Right(account) => Ok(Json.toJson(Response("Ok", "current account balance is " + account.current_balance)))
    }   
  }
    
  /*
   * Deposit (To Remove): A method for quick prototyping via "GET" even though its a violation of REST verb principles 
   * */
  /*def deposit(account_id: String, amount: Double) = Action {
    as.deposit(account_id, amount) match {
      case Left(msg)  => Ok(Json.toJson(Response("Error", msg)))
      case Right(obj) => Ok(Json.toJson(Response("OK", "Deposit successful")))        
    }
  }*/
  
  /*
   * Deposit: Uses the idiomatic PUT verb 
   * */
  def depositPut = Action { request => 
    val json = request.body.asJson.get
    val deposit = json.as[Transaction]
    as.deposit(deposit.account_id, deposit.amount) match {
      case Left(msg)  => Ok(Json.toJson(Response("Error", msg)))
      case Right(obj) => Ok(Json.toJson(Response("OK", "Deposit successful")))
    }
  }

  /*def withdraw(account_id: String, amount: Double) = Action {
    as.withdraw(account_id, amount) match {
      case Left(msg)  => Ok(Json.toJson(Response("Error", msg)))
      case Right(obj) => Ok(Json.toJson(Response("OK", "Withdrawal successful")))
    }
  } */
  
  /*
   * Withdraw: Uses the idiomatic PUT verb 
   * */
  def withdrawPut = Action { request => 
    val json = request.body.asJson.get
    val withdraw = json.as[Transaction]
    as.withdraw(withdraw.account_id, withdraw.amount) match {
      case Left(msg)  => Ok(Json.toJson(Response("Error", msg)))
      case Right(obj) => Ok(Json.toJson(Response("OK", "Withdrawal successful")))
    }
  }

  /*
   * debug (for prototyping): Prints account and transaction objects in the database to the browser
   * */
  def debug = Action {
    val xx = as.debug    
    Ok(Json.toJson(xx))
  }    
    
  def insertTestRecord = Action {
    val createId = System.currentTimeMillis()
    as.deposit(createId.toString(), 50) match {
      case Left(msg)  => Ok(Json.toJson(Response("Error", msg)))
      case Right(obj) => Ok(Json.toJson(Response("OK", "Test Deposit successful")))        
    }
  }
  
  def catchall(path: String) = Action { request =>
    val redirectMsg = "No route found. Sample invocations <a href='../assets/usage.txt'>Sample Usage</a>"       
    Ok(Json.toJson(Response("Error", redirectMsg))).as("text/html")    
  }
}