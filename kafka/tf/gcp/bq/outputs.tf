output "bigquery_dataset_id" {
  description = "The ID of the created BigQuery dataset"
  value       = google_bigquery_dataset.analytics.dataset_id
}
