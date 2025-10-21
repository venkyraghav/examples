module "ec2" {
  source = "../ec2"

  aws_region             = "us-east-2"
  vpc_id                 = "vpc-07c6db6034b62d13e"
  aws_security_group_ids = ["sg-077a33f6b9cacde15"]
  instance_count         = 1
  instance_type          = "t2.medium"
  instance_ami           = "ami-0cfde0ea8edd312d4"
  instance_keypair       = "venkyawsohio"
  aws_subnet_id          = "subnet-08ce3a594a1278bcd"
  user_data              = templatefile("../jumpbox/setup-user-data.tpl", {})
  lifecycle {
    prevent_destroy = true
  }
}
