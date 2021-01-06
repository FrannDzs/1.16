package cofh.core.init;

import cofh.core.inventory.container.ItemFilterContainer;
import cofh.core.util.ProxyUtils;
import net.minecraftforge.common.extensions.IForgeContainerType;

import static cofh.core.CoFHCore.CONTAINERS;
import static cofh.lib.util.references.CoreIDs.ID_CONTAINER_ITEM_FILTER;

public class CoreContainers {

    private CoreContainers() {

    }

    public static void register() {

        CONTAINERS.register(ID_CONTAINER_ITEM_FILTER, () -> IForgeContainerType.create((windowId, inv, data) -> new ItemFilterContainer(windowId, inv, ProxyUtils.getClientPlayer())));
    }

}