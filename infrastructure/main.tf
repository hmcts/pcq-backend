provider "azurerm" {
  features {}
}


provider "azurerm" {
  subscription_id            = var.aks_subscription_id
  skip_provider_registration = "true"
  features {}
  alias = "postgres_network"

}

locals {
  db_connection_options = "?sslmode=require"
  vault_name            = "${var.product}-${var.env}"
  asp_name              = "${var.product}-${var.env}"
  db_server_name        = "${var.product}-${var.component}-flexible"
}

data "azurerm_key_vault" "key_vault" {
  name                = local.vault_name
  resource_group_name = local.vault_name
}

module "pcq-db-flexible" {
  providers = {
    azurerm.postgres_network = azurerm.postgres_network
  }

  source = "git@github.com:hmcts/terraform-module-postgresql-flexible?ref=master"
  env    = var.env

  product       = var.product
  component     = var.component
  business_area = "cft"
  name          = local.db_server_name

  common_tags = var.common_tags

  pgsql_admin_username = "pcquser"
  pgsql_version        = "15"
  auto_grow_enabled    = true

  action_group_name           = join("-", [var.db_monitor_action_group_name, local.db_server_name, var.env])
  email_address_key           = var.db_alert_email_address_key
  email_address_key_vault_id  = data.azurerm_key_vault.key_vault.id

  # Setup Access Reader db user
  force_user_permissions_trigger = "1"

 enable_db_report_privileges = true

  pgsql_databases = [
    {
      name : "pcq"
      report_privilege_schema : "public"
      report_privilege_tables : ["protected_characteristics"]
    }
  ]

  pgsql_server_configuration = [
    {
      name  = "azure.extensions"
      value = "pg_stat_statements,pg_buffercache,pgcrypto"
    }
  ]

  admin_user_object_id = var.jenkins_AAD_objectId
}

////////////////////////////////
// Populate Vault with DB info
////////////////////////////////

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  key_vault_id = data.azurerm_key_vault.key_vault.id
  name         = "${var.component}-POSTGRES-USER"
  value        = module.pcq-db-flexible.username
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  key_vault_id = data.azurerm_key_vault.key_vault.id
  name         = "${var.component}-POSTGRES-PASS"
  value        = module.pcq-db-flexible.password
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  key_vault_id = data.azurerm_key_vault.key_vault.id
  name         = "${var.component}-POSTGRES-HOST"
  value        = module.pcq-db-flexible.fqdn
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  key_vault_id = data.azurerm_key_vault.key_vault.id
  name         = "${var.component}-POSTGRES-PORT"
  value        = var.postgresql_flexible_server_port
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  key_vault_id = data.azurerm_key_vault.key_vault.id
  name         = "${var.component}-POSTGRES-DATABASE"
  value        = var.pcq_db_name
}

# Copy postgres password for flyway migration
resource "azurerm_key_vault_secret" "flyway_password" {
  key_vault_id = data.azurerm_key_vault.key_vault.id
  name         = "flyway-password"
  value        = module.pcq-db-flexible.password
}
