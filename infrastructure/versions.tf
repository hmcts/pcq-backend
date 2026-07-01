terraform {
  backend "azurerm" {}

  required_version = ">= 1.1.0"

  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.79.0"
    }

    azuread = {
      source  = "hashicorp/azuread"
      version = "3.9.0"
    }
  }
}
