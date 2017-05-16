# tiny-ioc [![Build Status](https://travis-ci.org/uklance/tiny-ioc.svg?branch=master)](https://travis-ci.org/uklance/tiny-ioc) [![Coverage Status](https://coveralls.io/repos/uklance/tiny-ioc/badge.svg?branch=master)](https://coveralls.io/r/uklance/tiny-ioc?branch=master) [![Download](https://api.bintray.com/packages/uklance/maven/tiny-ioc/images/download.svg) ](https://bintray.com/uklance/maven/tiny-ioc/_latestVersion)

A lightweight IOC container with minimal dependencies inspired by [google-guice](https://github.com/google/guice) and [tapestry-ioc](https://tapestry.apache.org/ioc.html) 

## Building and Registering Services With the IOC Container

```java
import com.lazan.tinyioc.annotations.*
public class MyModule {
    @Bind
    public void bind(ServiceBinder binder) {
           binder.bind(ServiceOne.class, ServiceOneImpl.class);
    }
    
    @Service
    public ServiceTwo serviceTwo() {
        return new ServiceTwoImpl("xxx");
    }
}
```

```java
import com.lazan.tinyioc.*;
public class MyMain {
    public static void main(String[] args) {
        ServiceRegistry registry = new ServiceRegistryBuilder()
            .withModuleType(MyModule.class)
            .build();
        
        ServiceOne s1 = registry.getService("serviceOne");
        ServiceTwo s2 = registry.getService(ServiceTwo.class);
    }
}
```
