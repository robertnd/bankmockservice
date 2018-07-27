package models

import play.api.libs.json._


 /*
   * Transaction is a data transfer object for looser coupling between the controller and data access logic
   * */

case class Transaction(id: String, txn_type: String, account_id: String, amount: Double, event_time: Long)
case class Response(Status: String, Message: String)

object Transaction {
  implicit val transactionFormat = Json.format[Transaction]  
  
  def reads(json: JsValue): JsResult[Transaction] = {
    val id = (json \ "id").as[String]
    val txn_type = (json \ "txn_type").as[String]
    val account_id = (json \ "account_id").as[String]
    val amount = (json \ "amount").as[Double]
    val event_time = System.currentTimeMillis
    JsSuccess(Transaction(id, txn_type, account_id, amount, event_time))
  }
}

object Response {
  implicit val respFormat = Json.format[Response]
}

/*[ { "id":"1532560028767", "txn_type":"deposit", "account_id":"1000", "amount":30000, "event_time":1532560028767  } ]*/