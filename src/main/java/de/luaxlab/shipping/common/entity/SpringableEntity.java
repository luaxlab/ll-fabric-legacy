package de.luaxlab.shipping.common.entity;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.util.Train;
import net.minecraft.entity.Entity;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

public interface SpringableEntity {

    Optional<Pair<SpringableEntity, SpringEntity>> getDominated();
    Optional<Pair<SpringableEntity, SpringEntity>> getDominant();
    void setDominated(SpringableEntity entity, SpringEntity spring);
    void setDominant(SpringableEntity entity, SpringEntity spring);
    void removeDominated();
    void removeDominant();
    Train getTrain();
    void setTrain(Train train);
    boolean hasWaterOnSides();

    default void handleSpringableKill(){
        this.getDominated().map(Pair::getFirst).ifPresent(SpringableEntity::removeDominant);
        this.getDominant().map(Pair::getFirst).ifPresent(SpringableEntity::removeDominated);
    }

    default boolean checkNoLoopsDominated(){
        return checkNoLoopsHelper(this, (entity -> entity.getDominated().map(Pair::getFirst)), new HashSet<>());
    }

    default boolean checkNoLoopsDominant(){
        return checkNoLoopsHelper(this, (entity -> entity.getDominant().map(Pair::getFirst)), new HashSet<>());
    }

    default boolean checkNoLoopsHelper(SpringableEntity entity, Function<SpringableEntity, Optional<SpringableEntity>> next, Set<SpringableEntity> set){
        if(set.contains(entity)){
            return true;
        }
        set.add(entity);
        Optional<SpringableEntity> nextEntity = next.apply(entity);
        return nextEntity.map(e -> this.checkNoLoopsHelper(e, next, set)).orElse(false);
    }

    default<U> Stream<U> applyWithAll(Function<SpringableEntity, U> function){
        return this.getTrain().getHead().applyWithDominated(function);
    }

    default<U> Stream<U> applyWithDominant(Function<SpringableEntity, U> function){
        Stream<U> ofThis = Stream.of(function.apply(this));

        return checkNoLoopsDominant() ? ofThis : this.getDominant().map(dom ->
                Stream.concat(ofThis, dom.getFirst().applyWithDominant(function))
        ).orElse(ofThis);

    }

    default<U> Stream<U> applyWithDominated(Function<SpringableEntity, U> function){
        Stream<U> ofThis = Stream.of(function.apply(this));

        return checkNoLoopsDominated() ? ofThis : this.getDominated().map(dom ->
                Stream.concat(ofThis, dom.getFirst().applyWithDominated(function))
        ).orElse(ofThis);

    }

    default void tickSpringAliveCheck(){
        this.getDominant().map(Pair::getSecond).map(Entity::isAlive).ifPresent(alive -> {
            if(!alive){
                this.removeDominant();
            }
        });

        this.getDominated().map(Pair::getSecond).map(Entity::isAlive).ifPresent(alive -> {
            if(!alive){
                this.removeDominated();
            }
        });


    }
}
