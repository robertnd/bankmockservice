package models

import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import java.time.temporal.ChronoField
import java.time.ZoneId

/*
 * Repository handler (CRUD operations)
 * */
@Singleton
class AccountHandler @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  private val format = "dd/MM/yyyy HH:mm:ss"

  import dbConfig._
  import profile.api._

  /*
   * Table object for account
   * */
  private class AccountTable(tag: Tag) extends Table[Account](tag, "account") {
    def id = column[String]("id", O.PrimaryKey) // This is the primary key column
    def customer_id = column[String]("customer_id")
    def current_balance = column[Double]("current_balance")

    override def * = (id, customer_id, current_balance) <> ((Account.apply _).tupled, Account.unapply)
  }

  /*
   * Table object for transaction
   * */
  private class TransactionsTable(tag: Tag) extends Table[Transaction](tag, "transaction") {
    def id = column[String]("id", O.PrimaryKey) // This is the primary key column
    def txn_type = column[String]("transaction_type")
    def account_id = column[String]("account_id")
    def amount = column[Double]("amount")
    def event_time = column[Long]("event_time")

    override def * = (id, txn_type, account_id, amount, event_time) <> ((Transaction.apply _).tupled, Transaction.unapply)
  }

  private val accounts = TableQuery[AccountTable]
  private val txns = TableQuery[TransactionsTable]
  
  def preprocess() = {
    
  }

  /*
   * List all accounts in the database
   * */
  def getAll(): Future[Seq[Account]] = db.run {
    accounts.result
  }

  /*
   * List all transactions in the database
   * */
  def getAllTxns(): Future[Seq[Transaction]] = db.run {
    txns.result
  }

  /*
   * Get a single account
   * */
  def getAccount(id: String): Future[Option[Account]] = {
    db.run(accounts.filter(_.id === id).result.headOption)
  }

  /*
   * Create an account. This is an implicit operation called when a deposit is attempted and the account does not exist
   * */
  def newaccount(id: String, customer_id: String, current_balance: Double) = db.run {
    DBIO.seq(accounts += Account(id, customer_id, current_balance)).transactionally
  }

   /* 
   * Transaction is a generic method for inserting deposits and withdrawals  
   * */
  def transaction(account_id: String, amount: Double, txn_type: String) = db.run {
    DBIO.seq(txns += Transaction(System.currentTimeMillis.toString(), txn_type, account_id, amount, getCurrentTime)).transactionally
  }

   /* 
   * Updates the running balance on the account object 
   * */
  def update(account_id: String, new_amount: Double) = db.run {
    accounts.filter(_.id === account_id).map(_.current_balance).update(new_amount).transactionally
  }

   /* 
   * Gets transactions for an account for a period of 24 hours 
   * */
  def getIntervalTotal(account_id: String, txn_type: String) = db.run {
    val marker1 = getMidnights()._1
    val marker2 = getMidnights()._2
    // :-( Yikes! There is probably a better way of doing this ...a for comprehension or something ...
    txns.filter(_.account_id === account_id).filter(_.txn_type === txn_type).filter(_.event_time > marker1).filter(_.event_time < marker2).map(_.amount).result
  }

   /* 
   * Returns a timestamp in UTC millisecond format. These are used as ids for various objects 
   * */
  def getCurrentTime(): Long = {
    System.currentTimeMillis()
  }
  
  /*
   * Calculate start of day, end of day timestamps in millisecs, use that to retrieve transactions falling between the two
   * */
  def getMidnights(): (Long, Long) = {
    val last_midnight = LocalDateTime.now().toLocalDate().atStartOfDay()
    val last_millis = last_midnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val to_midnight = last_midnight.plusSeconds(86399) //86399
    val to_millis = to_midnight.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    (last_millis, to_millis)
  }

}