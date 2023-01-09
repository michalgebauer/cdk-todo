package com.myorg;

import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.services.apigateway.Cors;
import software.amazon.awscdk.services.apigateway.CorsOptions;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.apigateway.StageOptions;
import software.amazon.awscdk.services.cloudfront.Behavior;
import software.amazon.awscdk.services.cloudfront.CfnDistribution;
import software.amazon.awscdk.services.cloudfront.CloudFrontWebDistribution;
import software.amazon.awscdk.services.cloudfront.OriginAccessIdentity;
import software.amazon.awscdk.services.cloudfront.PriceClass;
import software.amazon.awscdk.services.cloudfront.S3OriginConfig;
import software.amazon.awscdk.services.cloudfront.SourceConfiguration;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketEncryption;
import software.amazon.awscdk.services.s3.deployment.BucketDeployment;
import software.amazon.awscdk.services.s3.deployment.Source;
import software.constructs.Construct;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;

import java.util.List;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;

public class CdkTodoStack extends Stack {
    public CdkTodoStack(final Construct scope, final String id) {
        this(scope, id, null);
    }

    public CdkTodoStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Function cdkTodoLambda = Function.Builder.create(this, "CdkTodoLambda")
                .runtime(Runtime.JAVA_11)
                .code(Code.fromAsset("/Users/michalgebauer/Documents/workspace/temp/cdk-todo/todo-lamda/target/todo-lamda-0.0.1-SNAPSHOT-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .memorySize(256)
                .build();

        RestApi restApi = RestApi.Builder.create(this, "CdkTodoRestApi")
                .defaultCorsPreflightOptions(CorsOptions.builder()
                        .allowMethods(Cors.ALL_METHODS)
                        .allowOrigins(Cors.ALL_ORIGINS)
                        .build())
                .deployOptions(StageOptions.builder()
                        .stageName("api")
                        .build())
                .build();


        restApi.getRoot().addResource("todos")
                .addMethod("POST", LambdaIntegration.Builder.create(cdkTodoLambda).build());

        // FE
        Bucket cdkTodoWebBucket = Bucket.Builder.create(this, "CdkTodoWebBucket")
                .websiteIndexDocument("index.html")
                .websiteErrorDocument("index.html")
                .versioned(true)
                .encryption(BucketEncryption.S3_MANAGED)
                .enforceSsl(true)
                .autoDeleteObjects(true)
                .removalPolicy(RemovalPolicy.DESTROY)
                .build();

        OriginAccessIdentity cdkAccessidentity = OriginAccessIdentity.Builder.create(this, "CdkAccessidentity")
                .build();

        cdkTodoWebBucket.grantRead(cdkAccessidentity);

        CloudFrontWebDistribution cloudFrontWebDistribution = CloudFrontWebDistribution.Builder.create(this, "CdkTodo")
                .originConfigs(List.of(SourceConfiguration.builder()
                        .s3OriginSource(S3OriginConfig.builder().s3BucketSource(cdkTodoWebBucket)
                                .originAccessIdentity(cdkAccessidentity)
                                .build())
                        .behaviors(List.of(Behavior.builder().isDefaultBehavior(true).build()))
                        .build()))
                .errorConfigurations(List.of(CfnDistribution.CustomErrorResponseProperty
                        .builder()
                        .errorCode(404)
                        .responseCode(200)
                        .responsePagePath("/index.html")
                        .errorCachingMinTtl(300)
                        .build()))
                .priceClass(PriceClass.PRICE_CLASS_100)
                .build();

        BucketDeployment.Builder.create(this, "CdkDeployment")
                .sources(List.of(Source.asset("/Users/michalgebauer/Documents/workspace/temp/cdk-todo/todo-website/build")))
                .destinationBucket(cdkTodoWebBucket)
                .distribution(cloudFrontWebDistribution)
                .distributionPaths(List.of("/*"))
                .memoryLimit(256)
                .build();
    }
}
