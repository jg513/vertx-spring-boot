## Vert.x mail client example

This example demonstrates how to use a Vert.x mail client integration with WebFlux.

This application requires a running SMTP server. You can provide the server credentials in an application.properties file or just pass them on a start up as follows:

```
> java -jar target/vertx-spring-boot-sample-mail.jar --vertx.mail.host=${SMTP_HOST} --vertx.mail.username=${SMTP_USERNAME} --vertx.mail.password=${SMTP_PASSWORD}
```

Once the application is running, you can access it with your browser at http://localhost:8080/index.html.
