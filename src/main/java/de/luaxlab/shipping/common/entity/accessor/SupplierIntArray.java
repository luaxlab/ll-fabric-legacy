package de.luaxlab.shipping.common.entity.accessor;

import net.minecraft.screen.PropertyDelegate;

import java.util.function.IntSupplier;

public class SupplierIntArray implements PropertyDelegate {
    private int count;
    private IntSupplier[] suppliers;

    public SupplierIntArray(int count) {
        this.suppliers = new IntSupplier[count];
    }

    @Override
    public int get(int i) {
        return suppliers[i].getAsInt();
    }

    @Override
    public void set(int i, int j) {
        this.suppliers[i] = () -> j;
    }

    public void setSupplier(int i, IntSupplier supplier) {
        this.suppliers[i] = supplier;
    }

    @Override
    public int size() {
        return suppliers.length;
    }
}
