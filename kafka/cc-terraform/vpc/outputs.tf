output "vpc" {
  value       = aws_vpc.vpc
  description = "VPC"
}

output "public-subnets" {
  value       = aws_subnet.public-subnets[*].id
  description = "Public Subnets"
}

output "private-subnets" {
  value       = aws_subnet.private-subnets[*].id
  description = "Private Subnets"
}

output "gateway-id" {
  value       = aws_internet_gateway.igw.id
  description = "Gateway ID"
}

output "rtb-public" {
  value       = aws_route_table.public-route-table.id
  description = "Gateway ID"
}

output "sg" {
  value = aws_security_group.sg.id
  description = "Security Group"
}

output "rtb-private" {
  value = aws_route_table.rt_private[*].id
  description = "Private Route Table"
}

output "vpce-s3" {
  value = aws_vpc_endpoint.s3.id
  description = "VPCE S3"
}

output "vpce-rtb-private" {
  value = aws_vpc_endpoint_route_table_association.rt_associate_private[*].id
  description = "VPCE RTB Private"
}
