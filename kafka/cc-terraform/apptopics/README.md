# Notes

Create topics and associated resources

## Dependencies

Run `cckafka` module to create your CC Cluster

## Steps

* Follow the general steps in parent `README.md`
* To output APIKey:Secret

```shell
terraform output -raw test_sa
terraform output -raw test_sa_complex
```
