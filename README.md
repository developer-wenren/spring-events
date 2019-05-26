# 掌握 Spring 之事件处理

![头图](http://ww1.sinaimg.cn/large/006tNc79ly1g3eyykzk1lj30p00dwq4p.jpg)

## 1 前言

本次我们来学习 Spring 的事件处理，源于实际工作中遇到的项目需求：在一个支付的下单场景中，当用户真正支付成功，服务器收到回调后就需要及时更新订单数据状态来保证数据一致。通常做法就是在回调方法里直接使用订单服务更新数据， 然而这样实现上两个模块出现了紧密耦合，如果订单更新的操作需要进行调整，那么在支付回调的代码块中也需要被修改。

为了避免这样情况发生，我采用了 Spring 事件发布与订阅的方式来实现接受支付回调，发布通知更新订单状态的这个功能，让订单服务更新数据的操作只依赖特定的事件，而不用关心具体的触发对象，也能达到代码复用的目的。

本文主要内容涉及如下：

- Spring 标准事件的处理
- Spring 中自定义事件扩展实现
- Spring Boot 的事件与侦听

> 示例项目：
>
> - spring-events：https://github.com/wrcj12138aaa/spring-events
>
> 环境支持：
>
> - JDK 8
> - SpringBoot 2.1.4
> - Maven 3.6.0

## 2.1 Spring 标准事件处理

Spring 程序启动过程中会有不同的事件通知，内置标准的事件有 5 种：

| 事件                    | 说明                                                                                                                               |
| ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------- |
| `ContextRefreshedEvent` | 当 Spring 容器处于初始化或者刷新阶段时就会触发，事实是`ApplicationContext#refresh()`方法被调用时，此时容器已经初始化完毕。         |
| `ContextStartedEvent`   | 当调用 `ConfigurableApplicationContext`接口下的 `start()` 方法时触发，表示 Spring 容器启动；通常用于 Spring 容器显式关闭后的启动。 |
| `ContextStoppedEvent`   | 当调用 `ConfigurableApplicationContext` 接口下的 `stop()`方法时触发，表示 Spring 容器停止，此时能通过其 `start()`方法重启容器。    |
| `ContextClosedEvent`    | 当 Spring 容器调用 `ApplicationContext#close()` 方法时触发，此时 Spring 的 beans 都已经被销毁，并且不会重新启动和刷新。            |
| `RequestHandledEvent`   | 只在 Web 应用下存在，当接受到 HTTP 请求并处理后就会触发，实际传递的默认实现类 `ServletRequestHandledEvent`                         |

通常情况下，Spring 程序都会接收到 `ContextRefreshedEvent`, `ContextClosedEvent` 事件的通知。

知道了 Spring 自带的事件有哪些后，我们就可以针对一些场景利用事件机制来实现需求，比如说在 Spring 启动后初始化资源，加载缓存数据到内存中等等。代码实现也很简单，如下：

```java
@Component
public class InitalizeListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        System.out.println("Spring 容器启动  获取到 Application Context 对象 " + applicationContext);
        //TODO 初始化资源，加载缓存数据到内存
    }
}

// 启动 Spring 程序后，控制台出现如下日志：
// Spring 容器启动  获取到 Application Context 对象 org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext@6950ed69, started on Sun May 26 12:19:33 CST 2019
```

我们可以从 `ContextRefreshedEvent` 事件中获取到 `ApplicationContext` 对象，从而获取 Spring 容器中任何已经装载的 Bean 进行自定义的操作。

### 2.1.1 注解驱动的事件侦听

#### 引入 @EventListener

从 Spring 4.2 开始，Spring 又提供了更灵活的，注解驱动的事件侦听处理方式。主要使用 `@EventListener` 注解来标记需要监听程序事件的方法，底层由 `EventListenerMethodProcessor` 对象将标注的方法转为成 `ApplicationListener` 实例。

为什么说这个注解方式侦听事件更加灵活呢，我们可以先看下 `@EventListener` 注解的源码。

```java
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EventListener {

    @AliasFor("classes")
    Class<?>[] value() default {};

    @AliasFor("value")
    Class<?>[] classes() default {};

    String condition() default "";
}
```

`EventListener` 注解主要有两个属性：`classes` 和 `condition`。 `classes` 表示所需要侦听的事件类型，是个数组，所以允许在单个方法里进行多个不同事件的侦听，以此做到复用的效果；`condition` 顾名思义就是用来定义所侦听事件是否处理的前置条件，这里需要注意的是使用 [Spring Expression Language][spring expression language] （SpEL）定义条件，比如 `#root.event` 表示了具体的 `ApplicationEvent`对象, 使用方式可以参考下方示例代码：

```java
@Component
public class AnnotationListener {

    @EventListener(value = {ContextRefreshedEvent.class, ContextStartedEvent.class, ContextStoppedEvent.class, ContextClosedEvent.class, RequestHandledEvent.class}, condition = "#root.event != null")
    public void listener(ApplicationEvent event) {
        System.out.println(Thread.currentThread() + " 接收到 Spring 事件：" + event);
    }
}
```

这里需要注意的是，注解 `@EventListener`标记的方法参数类型不再限制必须是 `ApplicationEvent`的子类，没有实现 `ApplicationListener` 接口方法的约束，也让事件变得更加灵活。

#### 事件的传递

另外，使用 `@EventListener` 还支持事件的传递，将当前事件处理好的结果封装后发布一个新的事件，实现的方式就是让侦听方法返回非 `null` 值时，就视为事件继续传播,如下面的示例代码：

```java
@Component
@Order(2)
public class CustomEventListener {
    @EventListener
    public SecondCustomEvent listener(CustomEvent event) {
        System.out.println(Thread.currentThread() + "CustomEventListener接受到自定义事件：" + event);
        return new SecondCustomEvent(this, event.toString());
    }
}
```

### 2.1.2 侦听器优先级

当我们对单个事件存在多个侦听器时，可能会由于需求想要指定侦听器的执行顺序，这一点 Spring 也为我们考虑到了，只要使用 `@Order`注解声明监听类或者监听方法即可，根据 `@Order` 的 `value` 大小来确定执行顺序，越小越优先执行。

```java
@EventListener
@Order(42)
public void processEvent(Event event) {
}
```

## 2.2 自定义事件

在了解如何侦听 Spring 事件后，我们再来看下如何实现自定义的事件发布和侦听处理。首先就要介绍 Spring 中事件机制的三类对象：

- `Event` ：所需要触发的具体事件对象，通常扩展 `ApplicationEvent` 实现。
- `Publisher`：触发事件发布的对象，Spring 提供了 `ApplicationEventPublisher` 对象供我们使用，使用它的`publishEvent()` 方法就可以发布该事件。
- `Listener`：侦听事件发生的对象，也就是接受回调进行处理的地方，可以通过 实现 `ApplicationListener`接口，或者使用前面提到的 `@EventListener`注解声明为事件的侦听器。

接下来就简单看下，一个自定义事件从声明到发布订阅的代码示例。

### 2.2.1 自定义 **Application** Event

```java
public class CustomEvent extends ApplicationEvent {
    private String data;

    public CustomEvent(Object source, String data) {
        super(source);
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CustomEvent{" +
                "data='" + data + '\'' +
                ", source=" + source +
                '}';
    }
}
```

### 2.2.2 自定义 Publisher

```java
@Component
public class CustomeEventPublisher implements ApplicationEventPublisherAware {
    private ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(String message) {
        System.out.println("开始发布事件 " + message);
        applicationEventPublisher.publishEvent(new CustomEvent(this, message));
    }
}
```

创建事件发布者有两种方式，一种是使用 `@Autowire`注解，通过 Spring 容器的依赖注入功能，直接注入 `ApplicationEventPublisher`对象，或者实现 `ApplicationEventPublisherAware`接口，在 Spring 容器启动时由 Spring 设置。

### 2.2.3 自定义 Listener

```java
@Component
public class CustomEventListener implements ApplicationListener<CustomEvent> {
    @Override
    public void onApplicationEvent(CustomEvent event) {
        System.out.println(Thread.currentThread()+"CustomEventListener接受到自定义事件：" + event);
    }
}
```

定义事件侦听器时，我们通过实现 `ApplicationListener` 接口，指定了事件类型，这样在处理事件时就不避免了事件类型判断和转换。

关于事件侦听器还需要注意的一点是：Spring 事件处理默认是同步的，这一点在 Spring 官方文档所有提及，我们先解读下官方描述：

> You can register as many event listeners as you wish, but note that, by default, event listeners receive events synchronously. This means that the `publishEvent()` method blocks until all listeners have finished processing the event. One advantage of this synchronous and single-threaded approach is that, when a listener receives an event, it operates inside the transaction context of the publisher if a transaction context is available. If another strategy for event publication becomes necessary, See the javadoc for Spring’s [`ApplicationEventMulticaster`](https://docs.spring.io/spring-framework/docs/5.1.6.RELEASE/javadoc-api/org/springframework/context/event/ApplicationEventMulticaster.html) interface.

当发布者执行了 `publishEvent()` 方法，默认情况下方法所在的当前线程就会阻塞，直到所有该事件相关的侦听器将事件处理完成。而这样采用单线程同步方式处理的好处主要是可以保证让事件处理与发布者处于同一个事务环境里，如果多个侦听方法涉及到数据库操作时保证了事务的存在。

### 2.2.4 异步事件处理

当然 Spring 也提供了异步侦听事件的方式，这里主要依赖 `ApplicationEventMulticaster`接口,可以理解为广播方式，为了便于使用，Spring 提供一个简易的实现类 `SimpleApplicationEventMulticaster` 供我们直接使用,只需要将这个对象注册到 Spring 容器即可。

```java
@Configuration
public class AsynchronousSpringEventsConfig {
    @Bean(name = "applicationEventMulticaster")
    public ApplicationEventMulticaster simpleApplicationEventMulticaster() {
        SimpleApplicationEventMulticaster eventMulticaster
                = new SimpleApplicationEventMulticaster();
        eventMulticaster.setTaskExecutor(new SimpleAsyncTaskExecutor());
        return eventMulticaster;
    }
}
```

这里 `ApplicationEventMulticaster`Bean 需要一个 `java.util.concurrent.Executor`对象作为事件处理的线程池，我们直接使用 Spring 提供的 `SimpleAsyncTaskExecutor` 对象，每次事件处理都会有创建新的线程。

> 注意：注册 `ApplicationEventMulticaster` Bean 后所有的事件侦听处理都会变成的异步形式，如果需要针对特定的事件侦听采用异步方式的话：可以使用 `@EventListener` 和`@Async` 组合来实现。(前提是 Spring 程序启用 `@EnableAsync` 注解)

这里再提下使用异步方式处理事件的利弊，好处在于让我们程序在处理事件更加有效率，而缺点就在针对异常发生的处理更加复杂，需要借助 `AsyncUncaughtExceptionHandler`接口实现。

## 2.3 Spring Boot 事件与侦听

学习了那么多 Spring Framework 的事件处理相关的内容后，我们现在再来看看在 Spring Boot 里事件处理有什么需要额外学习的地方。还是一样，我们先从 Spring Boot 官方文档下手，在 Spring Boot

Doc 的 [23.5 Application Events and Listeners][23.5 application events and listeners] 一节中提到了事件处理:

> - In addition to the usual Spring Framework events, such as [`ContextRefreshedEvent`](https://docs.spring.io/spring/docs/5.1.6.RELEASE/javadoc-api/org/springframework/context/event/ContextRefreshedEvent.html), a `SpringApplication` sends some additional application events.
>
> - Application events are sent by using Spring Framework’s event publishing mechanism.

可以看出 Spring Boot 仍是基于 Spring Framework 的事件发布机制去处理事件，只是在此基础了新增了几个 `SpringApplication` 相关的事件：

- `ApplicationStartingEvent` ：程序启动时发生。
- `ApplicationEnvironmentPreparedEvent` ：程序中`Environment` 对象就绪时发生。
- `ApplicationPreparedEvent` ：程序启动后但还未刷新时发生。
- `ApplicationStartedEvent`：程序启动刷新后发生。
- `ApplicationReadyEvent`：程序启动完毕，等待请求时发生。
- `ApplicationFailedEvent` ：程序启动过程中出现异常时发生。

并且它们的执行顺序也是列举书顺序依次触发的。

另外，需要注意的是，当需要触发的事件是在 `ApplicationContext` 创建之前发生时，用 `@Bean` 方式注册的侦听器就不会执行，而 Spring Boot 为此提供了三种方式来处理这种情况：

1. 使用 `SpringApplication.addListeners(…)` 方法注册侦听器

   ```java
   SpringApplication springApplication = new SpringApplication(SpringEventsApplication.class);
   springApplication.addListeners(new NormalCustomEventListener());
   springApplication.run(args);
   ```

2. 使用 `SpringApplicationBuilder.listeners(…)`方法注册侦听器

   ```java
   SpringApplicationBuilder springApplicationBuilder = new SpringApplicationBuilder(SpringEventsApplication.class);
   springApplicationBuilder.listeners(new NormalCustomEventListener()).run(args);
   ```

3. 在应用资源文件夹新建文件 `META-INF/spring.factories`，并将 `org.springframework.context.ApplicationListener` 作为键，指定需要注册的侦听器类，如:

   ```properties
   org.springframework.context.ApplicationListener=\
   com.one.learn.spring.springevents.listener.NormalSecondCutomEventListener
   ```

## 3 结语

到这里我们学习 Spring 事件相关的内容就结束了，了解 Spring 的事件机制，并适当应用，可以为我们完成程序的某个功能时提供一个更加解耦，灵活的实现方式。

如果读完觉得有收获的话，欢迎点【好看】，点击文章头图，扫码关注【闻人的技术博客】😄😄😄。

## 4 参考

Spring context-functionality-events: https://docs.spring.io/spring/docs/5.1.6.RELEASE/spring-framework-reference/core.html#context-functionality-events

Spring boot-features-application-events-and-listeners：https://docs.spring.io/spring-boot/docs/2.1.4.RELEASE/reference/htmlsingle/#boot-features-application-events-and-listeners

Spring Expression Language: https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html

SpringEvents: https://www.baeldung.com/spring-events

Better application events in Spring Framework 4.2: https://spring.io/blog/2015/02/11/better-application-events-in-spring-framework-4-2

[23.5 application events and listeners]: https://docs.spring.io/spring-boot/docs/2.1.4.RELEASE/reference/htmlsingle/#boot-features-application-events-and-listeners
[spring expression language]: https://docs.spring.io/spring/docs/4.3.10.RELEASE/spring-framework-reference/html/expressions.html

