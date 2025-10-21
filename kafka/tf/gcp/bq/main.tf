# ---------------------------
# BigQuery Dataset
# ---------------------------
resource "google_bigquery_dataset" "analytics" {
  dataset_id                 = "venky_highmark_analytics_dataset"
  friendly_name              = "Venky HighMark Analytics Dataset"
  description                = "Venky HighMark Dataset for analytics and reporting"
  location                   = var.region
  delete_contents_on_destroy = true
}
