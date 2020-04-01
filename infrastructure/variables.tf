variable "product" {
  default = "pcq"
}

variable "raw_product" {
  default = "pcq" // jenkins-library overrides product for PRs and adds e.g. pr-55-pcq
}

variable "component" {
  default = "backend"
}

variable "location" {
  default = "UK South"
}

variable "location_db" {
  type    = "string"
  default = "UK South"
}

variable "deployment_namespace" {}

variable "env" {}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type        = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "subscription" {}

variable "common_tags" {
  type = "map"
}

variable "managed_identity_object_id" {
  default = ""
}

variable "appinsights_location" {
  type        = "string"
  default     = "West Europe"
  description = "Location for Application Insights"
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default     = ""
}
