package services

import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfter
import org.scalatest.FunSuite
import com.google.inject.Injector
import com.google.inject.Guice
import org.scalatest.junit.JUnitRunner
import javax.inject.Inject
import controllers.RequestController
import play.api.test.Injecting
import org.scalatestplus.play.guice.GuiceOneAppPerTest

@RunWith(classOf[JUnitRunner])
class AccountServiceTester() extends FunSuite with GuiceOneAppPerTest with Injecting {

  
  /* run <host:port//xyzbank/debug> in the browser to get a sample of accounts 
     in the database for a valid account_id */
     
  
  
  test("getAccount: Valid account_id supplied") {
    val as = inject[AccountService]
    val returnedAcct = as.getAccount("1532627127911") match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(returnedAcct == true, s"getAccount should return account object")    
  }
  
  test("getBalance: Valid account_id supplied") {
    val as = inject[AccountService]
    val returnedBalance = as.getAccount("1532627127911") match {
      case Right(obj) => obj.current_balance
      case Left(msg)  => -1
    }
    assert(returnedBalance > -1, s"getBalance should return 0 or positive balance")    
  }

  test("getAccount: Invalid account_id supplied") {
    val as = inject[AccountService]
    val returnedAcct = as.getAccount("%&^%&^%") match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(returnedAcct == false, s"getAccount should return error message")
  }

  test("getAccount: No account_id supplied") {
    val as = inject[AccountService]
    val returnedAcct = as.getAccount("") match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(returnedAcct == false, s"getAccount should return error message")
  }
  
  /* this test can fail if daily frequency or daily max limits have been reached, 
     please use a different account_id every test run */
  test("deposit: valid amount supplied") {
    val as = inject[AccountService]
    
    val dep_op = as.deposit("1532627127911", 15) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(dep_op == true, s"deposit should return 1")
  }
  
   test("deposit: amount supplied over daily limit") {
    val as = inject[AccountService]
    
    val dep_op = as.deposit("1532627127911", 40001) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(dep_op == false, s"deposit should return error message")
  }
  
  test("deposit: 0 amount supplied") {
    val as = inject[AccountService]
    
    val dep_op = as.deposit("1532627127911", 0) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(dep_op == false, s"deposit should return error message")
  }
  
  test("deposit: Negative amount supplied") {
    val as = inject[AccountService]
    
    val dep_op = as.deposit("1532627127911", -234) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(dep_op == false, s"deposit should return error message")
  }
  
  /* this test can fail if daily frequency or daily max limits have been reached, 
     please use a different account_id every test run */
  test("withdrawal: valid amount supplied") {
    val as = inject[AccountService]
    
    val withd_op = as.withdraw("1532627127911", 15) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(withd_op == true, s"withdrawal should return 1")
  }
  
  test("withdrawal: Invalid account supplied") {
    val as = inject[AccountService]
    
    val withd_op = as.withdraw("_", 15) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(withd_op == false, s"withdrawal should return error message")
  }
  
  test("withdrawal: amount supplied over daily limit") {
    val as = inject[AccountService]
    
    val withd_op = as.withdraw("1532627127911", 20001) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(withd_op == false, s"deposit should return error message")
  }
  
  test("withdrawal: 0 amount supplied") {
    val as = inject[AccountService]
    
    val withd_op = as.withdraw("1532627127911", 0) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(withd_op == false, s"deposit should return error message")
  }
  
  test("withdrawal: Negative amount supplied") {
    val as = inject[AccountService]
    
    val withd_op = as.withdraw("1532627127911", -234) match {
      case Right(obj) => true
      case Left(msg)  => false
    }
    assert(withd_op == false, s"deposit should return error message")
  }
  
  /* returns a Seq of deposits for the account within a 24 hour interval */
  test("deposit transaction data: daily frequency limits, max daily deposit") {
    val as = inject[AccountService]
    
    val deposit_data = as.getAccountTxnData("1532627127911", "deposit") 
    val (txnCount, txnsSum) = (deposit_data.size, deposit_data.sum)    
    assert(txnCount <= 4, s"deposit frequency limit has been surpassed")
    assert(txnsSum <= 150000, s"deposit max daily limit has been surpassed")
  }
  
  /* returns a Seq of withdrawals for the account within a 24 hour interval */
  test("withdrawal transaction data: daily frequency limits, max daily withdrawal") {
    val as = inject[AccountService]
    
    val withd_data = as.getAccountTxnData("1532627127911", "withdrawal") 
    val (txnCount, txnsSum) = (withd_data.size, withd_data.sum)    
    assert(txnCount <= 3, s"withdrawal frequency limit has been surpassed")
    assert(txnsSum <= 50000, s"withdrawal max daily limit has been surpassed")
  }  

}
