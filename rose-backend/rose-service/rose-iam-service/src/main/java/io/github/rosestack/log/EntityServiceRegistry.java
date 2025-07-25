package io.github.rosestack.log;

public interface EntityServiceRegistry {

    EntityService getServiceByEntityType(EntityType entityType);

}
