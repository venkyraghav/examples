terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = ">= 5.0"
    }
  }
}
# providers for module reverse_proxy
provider "google" {
  project = var.project_id
  region  = var.region

  default_labels = {
    cflt_managed_id  = "vnarayanan"
    cflt_managed_by  = "user"
    cflt_environment = "dev"
    cflt_service     = "highmarkdemo"
    cflt_keep_until  = "2025-12-31"
  }
}
