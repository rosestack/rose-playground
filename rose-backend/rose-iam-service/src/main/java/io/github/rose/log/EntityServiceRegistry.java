package io.github.rose.log;

public interface EntityServiceRegistry {

    EntityService getServiceByEntityType(EntityType entityType);

}
