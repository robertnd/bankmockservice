package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.test._
import play.api.test.Helpers._
import services.AccountService
import models.AccountHandler
import javax.inject.Inject
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import models.Transaction
import play.api.libs.json._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RequestControllerTester extends PlaySpec with GuiceOneAppPerTest with Injecting {

  "RequestController GET" should {   
    
    "return account balance " in {
      val controller = inject[RequestController]
      val balance_request = controller.getBalance("1532627127911").apply(FakeRequest(GET, "/xyzbank/account/balance/1532627127911"))
      status(balance_request) mustBe OK
      contentType(balance_request) mustBe Some("application/json")  
      contentAsString(balance_request) must include ("current account balance")
    }
    
    "deposit into account " in {
      val controller = inject[RequestController]
      val deposit = Transaction("provided", "deposit", "1532627127911", 50, System.currentTimeMillis)
      val jsonReq = Json.toJson(deposit)
      val fakeRequest = FakeRequest(PUT, "/xyzbank/account/deposit/depositPut").withJsonBody(jsonReq);
      
      val deposit_req = controller.depositPut.apply(fakeRequest)      
      status(deposit_req) mustBe OK
      contentType(deposit_req) mustBe Some("application/json")  
      contentAsString(deposit_req) must include ("Deposit successful")
    }
    
    "withdraw from account " in {
      val controller = inject[RequestController]
      val withdraw = Transaction("provided", "withdrawal", "1532627127911", 50, System.currentTimeMillis)
      val jsonReq = Json.toJson(withdraw)
      val fakeRequest = FakeRequest(PUT, "/xyzbank/account/withdraw/withdrawPut").withJsonBody(jsonReq);
      
      val withd_req = controller.withdrawPut.apply(fakeRequest)      
      status(withd_req) mustBe OK
      contentType(withd_req) mustBe Some("application/json")  
      contentAsString(withd_req) must include ("Withdrawal successful")
    }
    
    "return debug info balance " in {
      val controller = inject[RequestController]
      val dbg = controller.debug.apply(FakeRequest(GET, "/xyzbank/debug"))
      status(dbg) mustBe OK
      contentType(dbg) mustBe Some("application/json")      
    }
    
    
  }   
  

}

/*"RequestController GET" should {

    "return list of accounts " in {
      val controller = new RequestController(stubControllerComponents(), as)
      val home = controller.getBalance("1000").apply(FakeRequest(GET, "/xyzbank/debug"))
      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      //contentAsString(home) must include ("Welcome to Play")
    }
  }*/
