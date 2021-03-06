
package de.unratedfilms.scriptspace.common.selection;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public class SelectionEntity extends Selection {

    public final int selectedEntityId;

    public SelectionEntity(Entity selectedEntity) {

        this(selectedEntity.dimension, selectedEntity.getEntityId());
    }

    public SelectionEntity(int dimensionId, int selectedEntityId) {

        super(dimensionId);

        this.selectedEntityId = selectedEntityId;
    }

    public Entity getSelectedEntity() {

        return getWorld().getEntityByID(selectedEntityId);
    }

    @Override
    public Vec3 getCenter() {

        Entity selectedEntity = getSelectedEntity();
        return Vec3.createVectorHelper(selectedEntity.posX, selectedEntity.posY, selectedEntity.posZ);
    }

    @Override
    public AxisAlignedBB getAABB() {

        return getSelectedEntity().boundingBox;
    }

    @Override
    public boolean intersects(double x, double y, double z) {

        return getAABB().isVecInside(Vec3.createVectorHelper(x, y, z));
    }

    @Override
    public List<Vec3> getLocations(double distance) {

        return Arrays.asList(getCenter());
    }

    @Override
    public List<Entity> getEntities() {

        Entity selectedEntity = getSelectedEntity();
        return selectedEntity == null ? Collections.<Entity> emptyList() : Arrays.asList(selectedEntity);
    }

    @Override
    public List<TileEntity> getTileEntities() {

        return Collections.emptyList();
    }

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + selectedEntityId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SelectionEntity other = (SelectionEntity) obj;
        if (selectedEntityId != other.selectedEntityId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {

        return "entity with id " + selectedEntityId;
    }

}
