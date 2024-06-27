package es.us.isa.botica.configuration.bot.lifecycle;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import es.us.isa.botica.util.configuration.Configuration;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    defaultImpl = InvalidBotLifecycleConfiguration.class)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ProactiveBotLifecycleConfiguration.class, name = "proactive"),
  @JsonSubTypes.Type(value = ReactiveBotLifecycleConfiguration.class, name = "reactive")
})
public interface BotLifecycleConfiguration extends Configuration {
  @JsonIgnore
  BotLifecycleType getType();
}
