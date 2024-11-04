terraform {
  required_version = ">= 0.14.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.17.0"
    }
  }
}

provider "aws" {
  region = var.region
}

resource "aws_vpc" "vpc" {
  cidr_block = var.cidr

  tags = {
    Name = var.vpc_name
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_subnet" "private-subnets" {
  count = length(var.cidr_private_subnets)
  vpc_id     = aws_vpc.vpc.id
  cidr_block = var.cidr_private_subnets[count.index]
  availability_zone = var.azs[count.index]

  depends_on = [
    aws_vpc.vpc
  ]

  tags = {
    Name = format("%s-subnet-private%d-%s", var.vpc_name, count.index, var.azs[count.index])
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_subnet" "public-subnets" {
  count = length(var.cidr_public_subnets)

  vpc_id     = aws_vpc.vpc.id
  cidr_block = var.cidr_public_subnets[count.index]
  availability_zone = var.azs[count.index]

  depends_on = [
    aws_vpc.vpc
  ]

  tags = {
    Name = format("%s-subnet-public%d-%s", var.vpc_name, count.index, var.azs[count.index])
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id

  depends_on = [
    aws_vpc.vpc
  ]

  tags = {
    Name = format("%s-igw", var.vpc_name)
    name = format("%s-igw", var.vpc_name)
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_route_table" "public-route-table" {
  vpc_id = aws_vpc.vpc.id

  depends_on = [
    aws_vpc.vpc
  ]

  tags = {
    Name = format("%s-rtb-public", var.vpc_name)
    name = format("%s-rtb-public", var.vpc_name)
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_route" "public-route" {
  route_table_id         = aws_route_table.public-route-table.id
  destination_cidr_block = "0.0.0.0/0"
  gateway_id             = aws_internet_gateway.igw.id

  depends_on = [
    aws_vpc.vpc,
    aws_internet_gateway.igw,
    aws_route_table.public-route-table
  ]
}

resource "aws_route_table_association" "public-subnet-associations" {
  count = length(aws_subnet.public-subnets)
  subnet_id      = aws_subnet.public-subnets[count.index].id
  route_table_id = aws_route_table.public-route-table.id

  depends_on = [
    aws_vpc.vpc,
    aws_subnet.public-subnets,
    aws_route_table.public-route-table
  ]
}

# Creating Security Group 
resource "aws_security_group" "sg" {
  vpc_id      = "${aws_vpc.vpc.id}"
# Inbound Rules
# HTTP access from anywhere
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
# HTTPS access from anywhere
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
# SSH access from anywhere
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
# Outbound Rules
  # Internet access to anywhere
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
  depends_on = [
    aws_vpc.vpc
  ]
}

resource "aws_route_table" "rt_private" {
  vpc_id = aws_vpc.vpc.id
  count = length(var.azs)
  
  depends_on = [
    aws_vpc.vpc
  ]
 
  tags = {
    Name = format("%s-rtb-private%d-%s", var.vpc_name, count.index, var.azs[count.index])
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_route_table_association" "rt_associate_private" {
  count = length(aws_subnet.private-subnets)
  subnet_id      = aws_subnet.private-subnets[count.index].id
  route_table_id = aws_route_table.rt_private[count.index].id
  depends_on = [
    aws_vpc.vpc,
    aws_subnet.private-subnets,
    aws_route_table.aws_route_table.rt_private
  ]
}

resource "aws_vpc_endpoint" "s3" {
  vpc_id       = aws_vpc.vpc.id
  service_name = format("com.amazonaws.%s.s3", var.region)
  depends_on = [
    aws_vpc.vpc
  ]
  
  tags = {
    Name = format("%s-vpce-s3-%s", var.vpc_name, var.region)
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

# associate route table with VPC endpoint
resource "aws_vpc_endpoint_route_table_association" "rt_associate_private" {
  count = length(aws_route_table.rt_private)
  route_table_id = aws_route_table.rt_private[count.index].id
  vpc_endpoint_id = aws_vpc_endpoint.s3.id
  depends_on = [
    aws_vpc.vpc,
    aws_vpc_endpoint.s3,
    aws_route_table.aws_route_table.rt_private
  ]
}
