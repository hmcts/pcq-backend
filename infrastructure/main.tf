provider "azurerm" {
  version = "=1.33.1"
}

locals {
  is_preview               = "${(var.env == "preview" || var.env == "spreview")}"
  preview_vault_name       = "${var.product}-aat"
  non_preview_vault_name   = "${var.product}-${var.env}"
  vault_name               = "${local.is_preview ? local.preview_vault_name : local.non_preview_vault_name}"
  asp_name                 = "${var.product}-${var.env}"

  db_connection_options    = "?sslmode=require"
}

module "pcq-db" {
  source                 = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product                = "${var.product}-${var.component}"
  location               = "${var.location_db}"
  env                    = "${var.env}"
  database_name          = "pcq"
  postgresql_user        = "pcquser"
  postgresql_version     = "11"
  postgresql_listen_port = "5432"
  sku_name               = "GP_Gen5_2"
  sku_tier               = "GeneralPurpose"
  common_tags            = "${var.common_tags}"
  subscription           = "${var.subscription}"
}

data "azurerm_key_vault" "key_vault" {
  name                = "${local.vault_name}"
  resource_group_name = "${local.vault_name}"
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "${var.component}-POSTGRES-USER"
  value        = "${module.pcq-db.user_name}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "${var.component}-POSTGRES-PASS"
  value        = "${module.pcq-db.postgresql_password}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "${var.component}-POSTGRES-HOST"
  value        = "${module.pcq-db.host_name}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "${var.component}-POSTGRES-PORT"
  value        = "${module.pcq-db.postgresql_listen_port}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = "${module.pcq-db.postgresql_database}"
}

# Copy postgres password for flyway migration
resource "azurerm_key_vault_secret" "flyway_password" {
  key_vault_id = "${data.azurerm_key_vault.key_vault.id}"
  name         = "flyway-password"
  value        = "${module.pcq-db.postgresql_password}"
}
