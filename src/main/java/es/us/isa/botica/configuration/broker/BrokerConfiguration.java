package es.us.isa.botica.configuration.broker;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Void.class)
@JsonSubTypes({@JsonSubTypes.Type(value = RabbitMqConfiguration.class, name = "rabbitmq")})
public interface BrokerConfiguration {}
