resource "google_storage_bucket" "data_bucket" {
  name                        = "${var.project_id}-venky-highmark-data-bucket"
  location                    = var.region
  force_destroy               = true # WARNING: allows deletion even if bucket not empty
  uniform_bucket_level_access = true

  versioning {
    enabled = true
  }

  lifecycle_rule {
    action {
      type = "Delete"
    }
    condition {
      age = 30
    }
  }
}
