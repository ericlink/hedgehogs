# hedgehogs

Simple service playground to play with localstack setup of aws services , **NOT using any Spring**.

1) clone project, 
2) fire up stack by:
   awslocal cloudformation create-stack --template-body file://src/main/resources/test-formation.json --stack-name simple-stack-with-params --parameters ParameterKey=Prefix,ParameterValue=local
3) then you can fire up HedgehogServicesImpl.



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