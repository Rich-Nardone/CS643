# CS643 Project 1

The follwoing prject was created using Java, Apache Maven, AWS Rekognition, AWS SQS, AWS S3 and AWS EC2

![image](https://user-images.githubusercontent.com/64598006/195741055-66505477-b5db-4d81-a3cb-1e16137b885d.png)

AWS Setup

--IAM Setup
  Navigate to "IAM" -> "Access Management" -> "Policies"
    Select these three:
      -AmazonRekognitionFullAccess
      -AmazonS3FullAccess
      -AmazonSQSFullAccess

--Create EC2 instances
Navigate to "Services" -> "EC2"
Navigate to "Launch Instances" -> "Launch Instancess" 
Select the "Amazon Linux"  
Select the "t2.micro type (Free tier eligible)" 
Create New Key Pair and use for both instances
Click "Next: Add Storage"
Click "Next: Add Tags"
	Insert "EC2_A" under "Key" and "Value"
	
Click "Next: Configure Security Group"
	Click "Edit"
	Select all three SSH, HTTP, HTTPS
	Under 'Source' drop down for each rule, select 'My IP'
	
Click "Review and Launch"
	Click on "Launch" after reviewing your information
	Hit "Launch Instances"
	Hit "View Instances"

Set correct permissions for .pem file:

	$ chmod 400 <file_name>.pem

Connect to EC2 instance
  Run this command in terminal of local machine:
  
	$ ssh -i "~/<file_name>.pem" ec2-user@<YOUR_EC2_INSTANCE_PUBLIC_IPV4_ADDRESS>
  
  !Change pem and IPV4 Address for each instance!
  
After you have connected, run the following commands to update Java from 1.7 to 1.8 on the EC2:

	$ sudo yum install java-1.8.0-devel
	$ sudo /usr/sbin/alternatives --config java
	
Enter number corresponding to: 
	java-1.8.0-openjdk.x86_64 (/usr/lib/jvm/java-1.8.0-openjdk-1.8.0.322.b06-2.el8_5.x86_64/jre/bin/java)
	
--Credentials Setup [Access and Secret Keys]
	Navigate to "IAM"
	Select "Users"
	Click "Security credentials" -> "Create access key"
	To view access key, select "Show". 
    
		Access key ID: <insert_key_here>
		Secret access key: <insert_secret_here>

--SSH into both EC2's

  Create a .aws folder and files credentials and config
  
	$mkdir .aws
	$touch .aws/credentials
	$vi .aws/credentials
	
Paste copied credentials into credential file for each EC2, format as so:

	[default]
	aws_access_key_id=<insert_key_here>
	aws_secret_access_key=<insert_secret_here>
  
Next create the config file as follows:

	$touch .aws/config
	$vi .aws/config

Paste this into each config file:

	[default]
	region=us-east-1
	output=json             

Run the application
ssh into each ec2 and run the command

	$ git clone https://github.com/Rich-Nardone/CS643.git
 

SSH into EC2-B and run the following command:

	$ java -jar CS643/CS643-B/target/recogB-0.0.1-SNAPSHOT.jar

SSH into EC2-A and run the following command:

	$ java -jar CS643/CS643/target/recog-0.0.1-SNAPSHOT.jar
	
You will begin to see both files outputting.

When you see output >> output.txt on the EC2_B server the process has completed and the output is in output.txt

run command 

	$vi output.txt 


