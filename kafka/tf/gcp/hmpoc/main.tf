module "bq" {
  source = "../bq"

  region     = var.region
  project_id = var.project_id
}

module "gcs" {
  source = "../gcs"

  region     = var.region
  project_id = var.project_id
}
