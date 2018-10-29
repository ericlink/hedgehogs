# Hedgehogs!

Who doesnt like hedgehogs? They running and sniffing and impaling things.. 
--


Simple service playground to play with localstack setup of aws services , **NOT using any Spring**.

1) install localstack-cli ( https://github.com/localstack/awscli-local )

2) fire up docker container by 
    docker run  --name localstack -p 8080:8080 -p 4567-4582:4567-4582  localstack/localstack
    (if you need to pull it do docker pull first)

3) WHEN THEY FINISH IMPLEMENTING CLOUDFORMATION 
    fire up stack by:
    awslocal cloudformation create-stack --template-body file://src/main/resources/test-formation.json --stack-name simple-stack-with-params --parameters ParameterKey=Prefix,ParameterValue=local

3) UNTIL THEN

    awslocal dynamodb create-table --table-name hedgehogs  --attribute-definitions \
            AttributeName=id,AttributeType=S \
             --key-schema AttributeName=id,KeyType=HASH \
             --provisioned-throughput \
             ReadCapacityUnits=5,WriteCapacityUnits=5
    
    awslocal sns create-topic --name HedgehogsLifecycleEventsTopic
    awslocal sqs create-queue --queue-name HedgehogsLifecycleEventsQueue.fifo
    awslocal sns subscribe --topic-arn arn:aws:sns:us-east-1:123456789012:HedgehogsLifecycleEventsTopic --protocol sqs --notification-endpoint arn:aws:sqs:us-east-1:123456789012:HedgehogsLifecycleEventsQueue.fifo


    (notice fifo here in name - this guarantees FIFO order)

4) then you can fire up HedgehogServicesImpl.


Useful stuff
--

if you change cloud formation do this command to validate template

awslocal cloudformation validate-template --template-body file://src/main/resources/test-formation.json

if no errors found

awslocal cloudformation update-stack --template-body file://src/main/resources/test-formation.json --stack-name simple-stack-with-params --parameters ParameterKey=Prefix,ParameterValue=local


Services to be used
--
_Cloudformation,
DynamoDb_

Idea of project is that we are in forest, populated by hedgehogs. 
We controlling their lifecycle, update it through service runs and see how it goes

Next bits will be
_SQS/SNS,
IoT,
Lambda,
API Gateway_