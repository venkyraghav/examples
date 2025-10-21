output "bucket_name" {
  description = "The name of the created GCS bucket"
  value       = google_storage_bucket.data_bucket.name
}
