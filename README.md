# nats-java2
NATS Java Client

Working on the new Java client. 

Goal is to get a basic client up and going ASAP. 

Then I will add reconnect logic, 
* TLS, 
* mTLS, 
* JWT, 

But first the basics. The goal is a clean interface and rigourous testing. 

We abstracted the network transport so we can implement it with NIO, Netty, or Vert.x or OIO. 

This will make it very portable to Android, yet still very fast for server-side. 

The base jar will come with OIO baked in. 

You will be able to add blocking client or non-blocking client (ease of use) on top of the base client. 
The async can be callback or future based as well as streams.
It should also be easy to add support for RxJava, Vert.x, 

* Basics of reactive movement https://www.reactivemanifesto.org/
* Base streams API https://www.reactive-streams.org/
* RxJava https://github.com/ReactiveX/RxJava
* Vert.x https://vertx.io/
* Java 9+ Flow https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html 
* Spring 5 reactor https://projectreactor.io/ 

