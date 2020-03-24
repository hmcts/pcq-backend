provider "azurerm" {
  version = "=1.33.1"
}

locals {
  db_connection_options = "?sslmode=require"
}

module "pcq-db" {
  source             = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product            = "${var.product}-${var.component}"
  location           = "${var.location_db}"
  env                = "${var.env}"
  database_name      = "pcq"
  postgresql_user    = "pcquser@pcq-backend-${var.env}"
  postgresql_version = "11"
  postgresql_listen_port = "5432"
  sku_name           = "GP_Gen5_2"
  sku_tier           = "GeneralPurpose"
  common_tags        = "${var.common_tags}"
  subscription       = "${var.subscription}"
}

module "pcq" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  env                             = "${var.env}"
  java_container_version          = "11.0"
  product                         = "${var.product}-${var.component}"
  subscription                    = "${var.subscription}"
  common_tags                     = "${var.common_tags}"

  app_settings = {
    // db
    PCQ_DB_PORT         = "${module.pcq-db.postgresql_listen_port}"
    PCQ_DB_USERNAME     = "${module.pcq-db.postgresql_user}"
    PCQ_DB_PASSWORD     = "${module.pcq-db.postgresql_password}"
    PCQ_DB_NAME         = "${module.pcq-db.database_name}"
    PCQ_DB_CONN_OPTIONS = "${local.db_connection_options}"
    FLYWAY_URL                    = "jdbc:postgresql://${module.pcq-db.host_name}:${module.pcq-db.postgresql_listen_port}/${module.pcq-db.postgresql_database}${local.db_connection_options}"
    FLYWAY_USER                   = "${module.pcq-db.postgresql_user}"
    FLYWAY_PASSWORD               = "${module.pcq-db.postgresql_password}"
    FLYWAY_NOOP_STRATEGY          = "true"
  }
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "${var.component}-POSTGRES-PASS"
  value        = "${module.pcq-db.postgresql_password}"
}

# Copy postgres password for flyway migration
resource "azurerm_key_vault_secret" "flyway_password" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "flyway-password"
  value        = "${module.pcq-db.postgresql_password}"
}
