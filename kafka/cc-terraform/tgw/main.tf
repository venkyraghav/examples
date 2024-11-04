terraform {
  required_version = ">= 0.14.0"
  required_providers {
    confluent = {
      source  = "confluentinc/confluent"
      version = "2.3.0"
    }
    aws = {
      source  = "hashicorp/aws"
      version = ">= 5.17.0"
    }
  }
}

provider "confluent" {
  cloud_api_key    = var.confluent_cloud_api_key
  cloud_api_secret = var.confluent_cloud_api_secret
}

data "confluent_environment" "env" {
  id = var.confluent_cloud_env
}

data "confluent_network" "network" {
  id = var.confluent_cloud_network

  environment {
    id = data.confluent_environment.env.id
  }
}

resource "confluent_network" "tgw" {
  display_name     = format("CFLT TGW %s by %s for %s", var.tag_env, var.tag_owner_email, var.tag_purpose)
  cloud            = "AWS"
  region           = var.region
  cidr             = var.confluent_cidr
  connection_types = ["TRANSITGATEWAY"]

  environment {
    id = data.confluent_environment.env.id
  }
#   lifecycle {
#     prevent_destroy = true
#   }
}

# Create a Transit Gateway Connection to Confluent Cloud on AWS
provider "aws" {
  region = var.region
}


# Find the routing table
data "aws_route_tables" "rts" {
  vpc_id = var.vpc_id
}

data "aws_subnets" "private" {
  filter {
    name   = "tag:Name"
    values = ["venky-test-subnet-private*"]
  }
}

# TODO use VPC Name instead of vpc_id
resource "aws_ec2_transit_gateway" "tgw" {
  description                      = format("%s-tgw", var.vpc_id)
  auto_accept_shared_attachments = "enable"

  tags = {
    Name = format("%s-tgw", var.vpc_id)
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

# Sharing Transit Gateway with Confluent via Resource Access Manager (RAM) Resource Share
resource "aws_ram_resource_share" "confluent" {
  name                      = format("%s-ram", var.vpc_id)
  allow_external_principals = true
  tags = {
    Name = format("%s-ram", var.vpc_id)
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_ram_principal_association" "confluent" {
  principal          = confluent_network.tgw.aws[0].account # Replaced with var.aws_account_id
  resource_share_arn = aws_ram_resource_share.confluent.arn
}

resource "aws_ram_resource_association" "confluent" {
  resource_arn       = aws_ec2_transit_gateway.tgw.arn
  resource_share_arn = aws_ram_resource_share.confluent.arn
}

resource "confluent_transit_gateway_attachment" "cflt_tgw_attach" {
  display_name = format("CFLT TGW Attachment %s by %s for %s", var.tag_env, var.tag_owner_email, var.tag_purpose)
  aws {
    ram_resource_share_arn = aws_ram_resource_share.confluent.arn
    transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
    routes                 = var.aws_vpc_cidr
  }
  environment {
    id = data.confluent_environment.env.id
  }
  network {
    id = confluent_network.tgw.id
  }
#   lifecycle {
#     prevent_destroy = true
#   }
}

# Create Transit Gateway Attachment for the user's VPC
resource "aws_ec2_transit_gateway_vpc_attachment" "attachment" {
  subnet_ids         = data.aws_subnets.private.ids
  transit_gateway_id = aws_ec2_transit_gateway.tgw.id
  vpc_id             = var.vpc_id

  tags = {
    name = format("tgw_attach_%s", var.vpc_id)
    env = var.tag_env
    owner_email = var.tag_owner_email
    purpose = var.tag_purpose
  }
}

resource "aws_route" "r" {
  for_each               = toset(data.aws_route_tables.rts.ids)
  route_table_id         = each.key
  destination_cidr_block = confluent_network.tgw.cidr
  transit_gateway_id     = aws_ec2_transit_gateway.tgw.id
}