terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.80.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      cflt_managed_id  = "vnarayanan"
      cflt_managed_by  = "user"
      cflt_environment = "dev"
      cflt_service     = "Demo CP OAuth"
      cflt_keep_until  = "2025-12-31"
    }
  }
}
