package de.luaxlab.shipping.common.util;

import com.mojang.datafixers.util.Pair;
import de.luaxlab.shipping.common.entity.SpringableEntity;
import de.luaxlab.shipping.common.entity.vehicle.barge.AbstractBargeEntity;
import de.luaxlab.shipping.common.entity.vehicle.tug.AbstractTugEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Train {
    private final Optional<AbstractTugEntity> tug;
    private SpringableEntity tail;
    private SpringableEntity head;

    public Train(SpringableEntity entity){
        head = entity;
        tail = entity;
        this.tug = entity instanceof AbstractTugEntity ? Optional.of((AbstractTugEntity) entity) : Optional.empty();
    }

    public Optional<AbstractTugEntity> getTug() {
        return tug;
    }

    public SpringableEntity getTail() {
        return tail;
    }

    public void setTail(SpringableEntity tail) {
        this.tail = tail;
    }

    public SpringableEntity getHead() {
        return head;
    }

    public List<AbstractBargeEntity> getBarges(){
        if(this.head.checkNoLoopsDominated()) {
            // just in case - to avoid crashing the world.
            this.head.removeDominated();
            this.head.getDominated().map(Pair::getFirst).ifPresent(SpringableEntity::removeDominant);
            return new ArrayList<>();
        }
        return tug.map(tugEntity -> {
            List<AbstractBargeEntity> barges = new ArrayList<>();
            for (Optional<AbstractBargeEntity> barge = getNextBarge(tugEntity); barge.isPresent(); barge = getNextBarge(barge.get())){
                barges.add(barge.get());
            }
            return barges;
        }).orElse(new ArrayList<>());
    }

    public Optional<AbstractBargeEntity> getNextBarge(SpringableEntity entity){
        return entity.getDominated().map(Pair::getFirst).map(e -> (AbstractBargeEntity) e);
    }

    public void setHead(SpringableEntity head) {
        this.head = head;
    }
}
