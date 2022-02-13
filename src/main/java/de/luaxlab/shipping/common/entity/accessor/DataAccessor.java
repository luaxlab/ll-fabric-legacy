package de.luaxlab.shipping.common.entity.accessor;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PropertyDelegate;

public class DataAccessor implements PropertyDelegate {
    protected PropertyDelegate data;

    public DataAccessor(PropertyDelegate data) {
        this.data = data;
    }

    public PropertyDelegate getRawData() {
        return this.data;
    }

    public void write(PacketByteBuf buffer) {
        for (int i = 0; i < data.size(); i++) {
            buffer.writeInt(data.get(i));
        }
    }

    public int getEntityUUID() {
        return this.data.get(0);
    }

    @Override
    public int get(int i) {
        return data.get(i);
    }

    @Override
    public void set(int i, int j) {
        data.set(i, j);
    }

    @Override
    public int size() {
        return data.size();
    }
}
