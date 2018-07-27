package models

import play.api.libs.json._

 /*
   * Account is a data transfer object for looser coupling between the controller and data access logic
   * */
case class Account(id: String, customer_id: String, current_balance: Double)

object Account {
  implicit val accountFormat = Json.format[Account] 
  
  def reads(json: JsValue): JsResult[Account] = {
    val id = (json \ "id").as[String]
    val customer_id = (json \ "customer_id").as[String]
    val current_balance = (json \ "current_balance").as[Double]
    JsSuccess(Account(id, customer_id, current_balance))
  }
}

/* [ { "id":"1000", "customer_id":"1000", "current_balance":21050  } ] */
 