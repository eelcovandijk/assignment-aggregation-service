

## Step 1) Figure out the domain:

![Domain](domain.jpg)


## Step 2) Architecture


### Deployment

it should be easy to run the service. I run it as a Docker image.
Docker is widely used and can run anywhere from locally to somewhere in the cloud.

### Development platform/framework

I've a lot of experience with Spring-boot but this service is implemented using Quarkus.
This is a great opportunity to familiarize myself with the quarkus framework and learn something new.
I'm very impressed with the native capabilities of compiling applications to native images.
Quarkus makes it easy for the developer to create native images, although Spring is also working to get it compiled to native images.
They spin up in no time and have a minimal memory footprint, and the dev-mode makes it easy to implement functionality.
This is a great opportunity for me to learn about native images.

### Build tooling

Maven compiles the code and delivers a deployable package or image.
Quarkus did help me here as well to get up and running in no time.

### Async communication

Async communication is a big part of this program.
For production applications I would choose Queue implementations like RabbitMQ.
Queueing makes it easy for applications to scale, but harder to test.
Because this is a demo application and scaling is less important than showing the programming skills I've chosen to do the queueing in memory.

Same is for the scheduling part. Scheduling is done in memory.
I would choose Quartz schedulers when running in the cloud with multiple instances.
This makes it possible to 'share' the scheduler and run the task once, and not once per instance.

## 3) Testing

I've implemented unit tests and a component test with the JUnit 5 framework.
The component test is able to test against running services and by default it mocks out the services with WireMock.

For this simple service I did not find it necessary to implement tests in a separate project with Cucumber for instance.
I like the Gherkin language to describe how the system should behave to create a common understanding of the system (stakeholders/team members).
I found BDD very rewarding but also very time-consuming. That is why I did not use it in this application.

 