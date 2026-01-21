package core.behavior;

import core.entity.Entity;

public interface Behavior<T extends Entity<?>> {
    void update(Entity<?> entity, float deltaTime);

}
