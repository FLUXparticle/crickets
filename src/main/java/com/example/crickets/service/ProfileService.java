package com.example.crickets.service;

import com.example.crickets.data.*;
import com.example.crickets.repository.*;
import org.slf4j.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.*;
import org.springframework.amqp.support.converter.*;
import org.springframework.stereotype.*;

import java.util.*;

import static com.example.crickets.configuration.RabbitSubscribeConfig.*;

@Service
public class ProfileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProfileService.class);

    private final UserRepository userRepository;

    private final SubscriptionRepository subscriptionRepository;

    private final RabbitTemplate rabbitTemplate;

    private final String hostname;

    public ProfileService(UserRepository userRepository, SubscriptionRepository subscriptionRepository, RabbitTemplate rabbitTemplate, String hostname) {
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.hostname = hostname;
    }

    public User getUser(String server, String username) {
        User user = userRepository.findByServerAndUsername(server, username);

        if (user == null) {
            user = new User(server, username);
            userRepository.save(user);
        }

        return user;
    }

    public long getSubscriberCount(String userId) {
        return subscriptionRepository.countByCreatorId(userId);
    }

    public boolean subscribe(String creatorServer, String creatorName, String subscriberName) {
        Subscription subscription;
        if (creatorServer.isEmpty()) {
            User creator = getUser(null, creatorName);
            User subscriber = getUser(null, subscriberName);
            subscription = new Subscription(creator, subscriber);
        } else if (sendSubscribe(creatorServer, creatorName, subscriberName)) {
            User creator = getUser(creatorServer, creatorName);
            User subscriber = getUser(null, subscriberName);
            subscription = new Subscription(creator, subscriber);
        } else {
            subscription = null;
        }

        if (subscription != null) {
            subscriptionRepository.save(subscription);
            return true;
        }

        return false;
    }

    private Subscription sendSubscribeByHand(String creatorServer, String creatorName, String subscriberName) {
        // Erstellen der Reply Queue
        String replyQueueName = rabbitTemplate.execute(channel -> {
            return channel.queueDeclare().getQueue();
        });

        LOGGER.info("replyQueueName = {}", replyQueueName);

        User creator = new User(null, creatorName);
        User subscriber = new User(hostname, subscriberName);
        Subscription subscription = new Subscription(creator, subscriber);

        // Senden der Anfrage
        MessageProperties properties = MessagePropertiesBuilder.newInstance()
                .setReplyTo(replyQueueName)
                .setCorrelationId(UUID.randomUUID().toString())
                .build();

        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();
        Message requestMessage = messageConverter.toMessage(subscription, properties);
        rabbitTemplate.send(SUBSCRIBE_EXCHANGE, subscribeRoutingKey(creatorServer), requestMessage);

        // Empfangen der Antwort
        Message responseMessage = rabbitTemplate.receive(replyQueueName, 5000); // Timeout von 5 Sekunden
        if (responseMessage != null && responseMessage.getBody() != null) {
            return (Subscription) messageConverter.fromMessage(requestMessage);
        }

        return null;
    }

//    @RabbitListener(queues = "#{subscribeQueue.name}")
    public void handleSubscribeRequestByHand(Message requestMessage) {
        MessageProperties properties = requestMessage.getMessageProperties();
        MessageConverter messageConverter = rabbitTemplate.getMessageConverter();

        // Extrahieren des Benutzers aus der Anfrage
        Subscription subscription = (Subscription) messageConverter.fromMessage(requestMessage);

        User creator = subscription.getCreator();
        User subscriber = subscription.getSubscriber();

        boolean reply;
        // Suchen des Benutzers in der Datenbank
        if (userRepository.findByServerAndUsername(creator.getServer(), creator.getUsername()) != null) {
            User newCreator = getUser(creator.getServer(), creator.getUsername());
            User newSubscriber = getUser(subscriber.getServer(), subscriber.getUsername());
            Subscription newSubscription = new Subscription(newCreator, newSubscriber);
            subscriptionRepository.save(newSubscription);

            reply = true;
        } else {
            reply = false;
        }

        // Erstellen der Antwort
        MessageProperties replyProperties = MessagePropertiesBuilder.newInstance()
                .setCorrelationId(properties.getCorrelationId())
                .build();

        Message replyMessage = messageConverter.toMessage(reply, replyProperties);

        // Senden der Antwort
        rabbitTemplate.send(properties.getReplyTo(), replyMessage);
    }

    private Boolean sendSubscribe(String creatorServer, String creatorName, String subscriberName) {
        User creator = new User(null, creatorName);
        User subscriber = new User(hostname, subscriberName);
        Subscription subscription = new Subscription(creator, subscriber);

        return (Boolean) rabbitTemplate.convertSendAndReceive(SUBSCRIBE_EXCHANGE, subscribeRoutingKey(creatorServer), subscription);
    }

    @RabbitListener(queues = "#{subscribeQueue.name}")
    public boolean handleSubscribeRequest(Subscription subscription) {
        User creator = subscription.getCreator();
        User subscriber = subscription.getSubscriber();

        // Suchen des Benutzers in der Datenbank
        if (userRepository.findByServerAndUsername(creator.getServer(), creator.getUsername()) != null) {
            User newCreator = getUser(creator.getServer(), creator.getUsername());
            User newSubscriber = getUser(subscriber.getServer(), subscriber.getUsername());

            Subscription newSubscription = new Subscription(newCreator, newSubscriber);
            subscriptionRepository.save(newSubscription);

            return true;
        }

        return false;
    }

}
