output "instance_hosts_fqdn_ip" {
  value = [
    for instance in aws_instance.instance_host : {
      id          = instance.id
      name        = instance.tags.Name
      public_ip   = instance.public_ip
      public_dns  = instance.public_dns
      private_dns = instance.private_dns
    }
  ]
}
