terraform {
  backend "azurerm" {}

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.45.1"
    }
    azuread = {
      source  = "hashicorp/azuread"
      version = "3.6.0"
    }
  }
}
