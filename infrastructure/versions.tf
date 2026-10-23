terraform {
  backend "azurerm" {}

  required_version = ">= 1.1.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "5.11.0"
    }

    azuread = {
      source  = "hashicorp/azuread"
      version = "3.10.0"
    }
  }
}
