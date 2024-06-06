package es.us.isa.botica.configuration.broker;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import es.us.isa.botica.util.configuration.Configuration;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    defaultImpl = InvalidBrokerConfiguration.class)
@JsonSubTypes({@JsonSubTypes.Type(value = RabbitMqConfiguration.class, name = "rabbitmq")})
public interface BrokerConfiguration extends Configuration {}
