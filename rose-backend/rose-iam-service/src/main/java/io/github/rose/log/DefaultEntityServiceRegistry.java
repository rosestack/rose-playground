package io.github.rose.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefaultEntityServiceRegistry implements EntityServiceRegistry {
    private final List<EntityService> entityDaoServices;
    private final Map<EntityType, EntityService> entityServiceMap = new HashMap<>();

    public void init() {
        log.info("Initializing EntityServiceRegistry on ContextRefreshedEvent");

        entityDaoServices.forEach(entityDaoService -> {
            EntityType entityType = entityDaoService.getEntityType();
            entityServiceMap.put(entityType, entityDaoService);
        });
        log.info("Initialized EntityServiceRegistry with total [{}] entries", entityServiceMap.size());
    }

    @Override
    public EntityService getServiceByEntityType(EntityType entityType) {
        return entityServiceMap.get(entityType);
    }

}
