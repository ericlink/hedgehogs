# hedgehogs

Simple service playground to play with localstack setup of aws services , **NOT using any Spring**.

1) install localstack-cli ( https://github.com/localstack/awscli-local )
2) fire up docker container by 
   docker run  --name localstack -p 8080:8080 -p 4567-4582:4567-4582  localstack/localstack
   (if you need to pull it do docker pull first)
3) fire up stack by:
   awslocal cloudformation create-stack --template-body file://src/main/resources/test-formation.json --stack-name simple-stack-with-params --parameters ParameterKey=Prefix,ParameterValue=local
4) then you can fire up HedgehogServicesImpl.



Services to be used

_Cloudformation,
DynamoDb_

Idea of project is that we are in forest, populated by hedgehogs. 
We controlling their lifecycle, update it through service runs and see how it goes

Next bits will be
_SQS/SNS,
IoT,
Lambda,
API Gateway_