package com.example.crickets.service;

import com.example.crickets.data.*;
import com.example.crickets.repository.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.support.converter.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.stereotype.*;
import reactor.core.publisher.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

import static com.example.crickets.configuration.RabbitPostConfig.*;
import static com.example.crickets.configuration.RabbitReplyConfig.*;
import static java.util.stream.Collectors.*;
import static org.springframework.amqp.support.AmqpHeaders.*;

@Service
public class TimelineService {

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    private final PostReactiveRepository postReactiveRepository;

    private final PostUpdateRepository postUpdateRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final RabbitTemplate rabbitTemplate;

    private final Map<String, FluxSink<Post>> fluxMap = new HashMap<>();

    @Value("#{replyBinding.routingKey}")
    private String replyRoutingKey;

    public TimelineService(UserRepository userRepository, PostRepository postRepository, PostReactiveRepository postReactiveRepository, PostUpdateRepository postUpdateRepository, SubscriptionRepository subscriptionRepository, RabbitTemplate rabbitTemplate) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.postReactiveRepository = postReactiveRepository;
        this.postUpdateRepository = postUpdateRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    private Flux<Post> streamPostsRequest(String server, List<String> creators) {
        return Flux.create(sink -> {
            String correlationId = UUID.randomUUID().toString();
            fluxMap.put(correlationId, sink);
            sink.onCancel(() -> {
                // Cleanup if the subscriber cancels
                // rabbitTemplate.convertAndSend("post.exchange", "post." + server + ".routing.key.cancel", creators);
                System.out.println("cancelled!");
            });

            MessageProperties properties = MessagePropertiesBuilder.newInstance()
                    .setReplyTo(replyRoutingKey)
                    .setCorrelationId(correlationId)
                    .build();

            MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
            Message message = messageConverter.toMessage(creators, properties);

            rabbitTemplate.send(POST_EXCHANGE, postRoutingKey(server), message);
        });
    }

    @RabbitListener(queues = "#{replyQueue.name}")
    public void handleReplyQueue(@Header(CORRELATION_ID) String correlationId, @Payload Post post) {
        FluxSink<Post> sink = fluxMap.get(correlationId);
        if (sink != null) {
            sink.next(post);
        } else {
            System.out.println("unknown correlationId: " + correlationId + "post = " + post);
        }
    }

    @RabbitListener(queues = "#{postQueue.name}")
    public void handlePostQueue(@Header(REPLY_TO) String replyTo, @Header(CORRELATION_ID) String correlationId, List<String> creatorNames) {
        System.out.println("handlePostsRequest: replyTo = " + replyTo + ", correlationId = " + correlationId + ", creatorNames = " + creatorNames);
        List<User> creators = userRepository.findByUsernameIn(creatorNames);
        postUpdateRepository.streamByCreatorIn(creators)
                .subscribe(post -> {
                    System.out.println("post = " + post);

                    MessageProperties properties = MessagePropertiesBuilder.newInstance()
                            .setCorrelationId(correlationId)
                            .build();

                    MessageConverter messageConverter = rabbitTemplate.getMessageConverter();

                    Message message = messageConverter.toMessage(post, properties);
                    rabbitTemplate.send(REPLY_EXCHANGE, replyTo, message);
                });
    }

    public Flux<Post> getTimelinePostUpdates(String subscriberName) {
        User subscriber = getUser(null, subscriberName);
        Stream<User> userStream = subscriptionRepository.findBySubscriber(subscriber).stream()
                .map(Subscription::getCreator);

        Map<String, List<User>> creatorMap = Stream.concat(Stream.of(subscriber), userStream)
                .collect(groupingBy(user -> Optional.ofNullable(user.getServer()).orElse("")));

        return Flux.merge(
                creatorMap.entrySet().stream().map(entry -> {
                    String server = entry.getKey();
                    List<User> creators = entry.getValue();
                    System.out.println("getTimelinePostUpdates: server = " + server + ", creators = " + creators);
                    if (server.isEmpty()) {
                        return postUpdateRepository.streamByCreatorIn(creators);
                    } else {
                        List<String> creatorNames = creators.stream().map(User::getUsername).toList();
                        return streamPostsRequest(server, creatorNames);
                    }
                }).toList()
        );
    }

    public void createPost(String creatorName, String content) {
        User creator = getUser(null, creatorName);
        Date now = new Date();
        Post post = new Post(content, creator, now);
        postRepository.save(post);
    }

    private User getUser(String server, String username) {
        User user = userRepository.findByServerAndUsername(server, username);

        if (user == null) {
            user = new User(server, username);
            userRepository.save(user);
        }

        return user;
    }

    public Flux<Post> searchPosts(String query) {
        return postReactiveRepository.findByContentContains(query)
                .delayElements(Duration.ofMillis(500));
    }

}
