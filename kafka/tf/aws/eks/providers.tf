provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      cflt_managed_id  = "vnarayanan"
      cflt_managed_by  = "user"
      cflt_environment = "dev"
      cflt_service     = "ATT Demo CPC Gateway"
      cflt_keep_until  = "2025-12-31"
    }
  }
}
