output "confluent_network" {
  value       = format("%s-%s", confluent_network.tgw.resource_name, confluent_network.tgw.id)
  description = "Confluent Network"
}

output "confluent_network_vpc" {
  value       = confluent_network.tgw.aws[0].vpc
  description = "Confluent Network VPC"
}

output "confluent_network_account" {
  value       = confluent_network.tgw.aws[0].account
  description = "Confluent Network Account"
}

output "confluent_network_cidr" {
  value       = var.confluent_cidr
  description = "Confluent Network CIDR"
}

output "aws_tgw_id" {
    value = aws_ec2_transit_gateway.tgw.id
    description = "AWS TGW Id"
}

output "aws_tgw_prop_rtb" {
    value = aws_ec2_transit_gateway.tgw.propagation_default_route_table_id
    description = "AWS TGW Default Propagation Route Table"
}

output "aws_ram_id" {
    value = aws_ram_resource_share.confluent.id
    description = "AWS RAM Id"
}

output "aws_ram_arn" {
    value = aws_ram_resource_share.confluent.arn
    description = "AWS RAM ARN"
}

output "confluent_tgw_attach_id" {
    value = confluent_transit_gateway_attachment.cflt_tgw_attach.id
    description = "Confluent TGW attachment Id"
}

output "confluent_tgw_attach_aws_id" {
    value = confluent_transit_gateway_attachment.cflt_tgw_attach.aws[0].transit_gateway_attachment_id
    description = "Confluent TGW attachment AWS Id"
}

output "aws_vpc_tgw_attach_id" {
    value = aws_ec2_transit_gateway_vpc_attachment.attachment.id
    description = "AWS VPC TGW attachment Id"
}