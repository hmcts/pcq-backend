variable "product" {
  default = "pcq"
}

variable "component" {
  default = "backend"
}

variable "location" {
  default = "UK South"
}

variable "location_db" {
  default = "UK South"
}

variable "env" {}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "subscription" {}

variable "common_tags" {
  type = map(string)
}

variable "appinsights_location" {
  default     = "West Europe"
  description = "Location for Application Insights"
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default     = ""
}

variable "postgresql_flexible_server_port" {
  default = "5432"
}

variable "pcq_db_name" {
  description = "Name of database to use"
  default     = "pcq"
}

variable "aks_subscription_id" {}


variable "db_monitor_action_group_name" {
  description = "The name of the Action Group to create."
  type        = string
  default     = "db_monitor_ag"
}

variable "db_alert_email_address_key" {
  description = "Email address key in azure Key Vault. If empty, no action group will be created."
  type        = string
  default     = "db-monitoring-alert-email"
}

variable "cpu_threshold" {
  default     = 1
  type        = number
  description = "Average CPU utilisation threshold"
}

variable "memory_threshold" {
  default     = 5
  type        = number
  description = "Average memory utilisation threshold"
}

variable "storage_threshold" {
  default     = 0.5
  type        = number
  description = "Average storage utilisation threshold"
}
