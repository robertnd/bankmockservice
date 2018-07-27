CREATE SCHEMA IF NOT EXISTS acctservice_db;

CREATE TABLE IF NOT EXISTS account (
  id varchar(20) NOT NULL,
  customer_id varchar(20) NOT NULL,
  current_balance double NOT NULL DEFAULT '0',
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS transaction (
  id varchar(20) NOT NULL,
  account_id varchar(20) DEFAULT NULL,
  amount double DEFAULT NULL,
  transaction_type varchar(15) DEFAULT NULL,
  event_time bigint(8) DEFAULT NULL,
  PRIMARY KEY (id)
);
