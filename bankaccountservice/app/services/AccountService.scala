package services

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import javax.inject.Inject
import models.Account
import models.AccountHandler
import play.api.Configuration
import play.api.Logger


 /*
   * AccountService - to decouple the front controller and data access logic.
   * Also used for loading environment variables, validation, and error processing
   * */

class AccountService @Inject() (ac_handler: AccountHandler, playconf: Configuration) {
  
   /*
   * getAccount: returns a single account object
   * */
  def getAccount(account_id: String): Either[String, Account] = try {
    val acc = Await.result(ac_handler.getAccount(account_id), Duration.Inf) /* return eager. */

    if (acc.isEmpty) Left("No Account Found")
    else Right(acc.get)
  } catch {
    case ex: Exception => {
      Logger.error(s"Exception with getAccount: $account_id", ex)
      Left(ex.getMessage)
    }
  }
  
  /*
   * deposit: deposit operation with validation processing and error handling
   * */
  def deposit(account_id: String, amount: Double): Either[String, Int] = try {
    getAccount(account_id) match {
      case Left(msg) => {
        if (msg == "No Account Found") {                  
          val sid = System.currentTimeMillis.toString
          ac_handler.newaccount(sid, "c_" + sid, amount) /* a new account is created if none is found during a deposit operation */
          Right(1)
        } else Left(msg)
      }
      case Right(account) => {
        val txn_data = getAccountTxnData(account_id, "deposit") /* gets a Seq of this accounts transactions */
        val (txnCount, txnsSum) = (txn_data.size, txn_data.sum)

        val curr_bal = account.current_balance

        if (txnCount >= d_FrequencyMax.toInt) {
          Left(d_FrequencyMaxMsg)
        } else if (txnsSum >= d_DailyMax.toInt) {
          Left(d_DailyMaxMsg)
        } else if (amount >= d_TransactionMax.toInt) {
          Left(d_TransactionMaxMsg)
        } else if (amount < 0 || amount == 0) {
          Left("Deposit should be greater than 0")
        } else {
          val new_bal = curr_bal + amount
          Await.result(ac_handler.update(account_id, new_bal), Duration.Inf)
          ac_handler.transaction(account_id, amount, "deposit")
          Right(1)
        }
      }
    }
  } catch {
    case ex: Exception => {
      Logger.error(s"Exception with deposit: $account_id", ex)
      Left(ex.getMessage)
    }
  }

   /*
   * withdraw: withdraw operation with validation processing and error handling
   * */
  def withdraw(account_id: String, amount: Double): Either[String, Int] = try {
    getAccount(account_id) match {
      case Left(msg) => Left(msg)
      case Right(account) => {
        val txn_data = getAccountTxnData(account_id, "withdrawal") /* gets a Seq of this accounts transactions */
        val (txnCount, txnsSum) = (txn_data.size, txn_data.sum)
        val curr_bal = account.current_balance

        if (txnCount >= w_FrequencyMax.toInt) {
          Left(w_FrequencyMaxMsg)
        } else if (txnsSum >= w_DailyMax.toInt) {
          Left(w_DailyMaxMsg)
        } else if (amount >= w_TransactionMax.toInt) {
          Left(w_TransactionMaxMsg)
        } else if (amount >= curr_bal) {
          Left(w_OverdrawMsg)
        } else if (amount < 0 || amount == 0) {
          Left("Withdrawal should be greater than 0")
        } else {
          val new_bal = curr_bal + (-1 * amount)
          Await.result(ac_handler.update(account_id, new_bal), Duration.Inf)
          ac_handler.transaction(account_id, (-1 * amount), "withdrawal")
          Right(1)
        }
      }
    }
  } catch {
    case ex: Exception => {
      Logger.error(s"Exception with withdraw: $account_id", ex)
      Left(ex.getMessage)
    }
  }
  
  /*
   * getAccountTxnData: returns a Seq of transactions filtered by type (deposit|withdrawal).
   * A sum (total amount transacted) and size (no. of transactions) are computed against the collection for validation checks
   * */
  def getAccountTxnData(account_id: String, txn_type: String) = {
    Await.result(ac_handler.getIntervalTotal(account_id, txn_type), Duration.Inf)
  }

  def debug() = {
    //val txn_data = getAccountTxnData("2000", "deposit")
    //val (txnCount, txnsSum) = (txn_data.size, txn_data.sum)
    //(txnCount, txnsSum)
    val allaccounts = Await.result(ac_handler.getAll, Duration.Inf)
    val alltxns = Await.result(ac_handler.getAllTxns(), Duration.Inf)
    (allaccounts, alltxns)
  }

  /*
   * preload environment variables from application.conf
   * */
  val (d_DailyMax, d_TransactionMax, d_FrequencyMax, d_DailyMaxMsg, d_TransactionMaxMsg, d_FrequencyMaxMsg) = {
    (
      playconf.getString("Deposit_DailyMax").get,
      playconf.getString("Deposit_TransactionMax").get,
      playconf.getString("Deposit_FrequencyMax").get,
      playconf.getString("Deposit_DailyMaxMsg").get,
      playconf.getString("Deposit_TransactionMaxMsg").get,
      playconf.getString("Deposit_FrequencyMaxMsg").get)
  }

  /*
   * preload environment variables from application.conf
   * */
  val (w_DailyMax, w_TransactionMax, w_FrequencyMax, w_Overdraw, w_DailyMaxMsg, w_TransactionMaxMsg, w_FrequencyMaxMsg, w_OverdrawMsg) = {
    (
      playconf.getString("Withdraw_DailyMax").get,
      playconf.getString("Withdraw_TransactionMax").get,
      playconf.getString("Withdraw_FrequencyMax").get,
      playconf.getString("Withdraw_Overdraw").get,
      playconf.getString("Withdraw_DailyMaxMsg").get,
      playconf.getString("Withdraw_TransactionMaxMsg").get,
      playconf.getString("Withdraw_FrequencyMaxMsg").get,
      playconf.getString("Withdraw_OverdrawMsg").get)
  }

  val logLevel = playconf.getString("LogLevel").get
}

object AccountService {
}
