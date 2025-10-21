resource "aws_instance" "instance_host" {
  count                       = var.instance_count
  ami                         = var.instance_ami
  instance_type               = var.instance_type
  subnet_id                   = var.aws_subnet_id
  vpc_security_group_ids      = var.aws_security_group_ids
  key_name                    = var.instance_keypair
  associate_public_ip_address = true
  user_data                   = var.user_data

  ebs_block_device {
    device_name           = "/dev/sdb" # Device name for the additional volume
    volume_size           = 50         # Size in GiB
    volume_type           = "gp2"
    delete_on_termination = true
    encrypted             = true # Enable encryption
  }

  tags = {
    Name = "CPDemoOAuthInstance-${count.index}"
  }
}
