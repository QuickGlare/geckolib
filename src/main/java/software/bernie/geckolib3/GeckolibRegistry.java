package software.bernie.geckolib3;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import software.bernie.geckolib3.core.GeckolibCoreRegistry;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.renderers.geo.*;

import java.util.function.Function;

public class GeckolibRegistry {
    public static <T extends Item & IAnimatable> void registerItemRenderer(GeoItemRenderer<T> renderer,
                                                                           T... animatables) {
        for (T animatable : animatables) {
            BuiltinItemRendererRegistry.INSTANCE.register(animatable, renderer);
            GeckolibCoreRegistry.registerModel((Class<? extends IAnimatable>) animatable.getClass(),
                    renderer.getModel());
        }
    }

    public static <T extends ArmorItem & IAnimatable> void registerArmorRenderer(GeoArmorRenderer<T> renderer,
                                                                                 T... animatables) {
        for (T animatable : animatables) {
            ArmorRenderer.register(renderer, animatable);
            GeckolibCoreRegistry.registerModel((Class<? extends IAnimatable>) animatable.getClass(),
                    renderer.getModel());
        }
    }

    public static <T extends BlockEntity & IAnimatable, E extends BlockEntityType<T>> void registerBlockRenderer(
            Function<BlockEntityRendererFactory.Context, GeoBlockRenderer<T>> rendererFactory,
            E blockEntityType, Class<T> blockEntityClass) {
        // Since vanilla creates BlockEntityRenderers on demand, we can't just add them directly to the registry,
        // so instead we overwrite the registered renderer anytime vanilla creates it
        BlockEntityRendererRegistry.register(blockEntityType, ctx -> rendererFactory.andThen((renderer) -> {
            GeckolibCoreRegistry.registerModel(blockEntityClass,
                    renderer.getModel());
            return renderer;
        }).apply(ctx));
    }

    public static <T extends LivingEntity & IAnimatable, E extends EntityType<T>> void registerLivingEntityRenderer(
            Function<EntityRendererFactory.Context, GeoEntityRenderer<T>> rendererFactory,
            E entityType, Class<T> entityClass) {
        // Since vanilla creates EntityRenderers on demand, we can't just add them directly to the registry,
        // so instead we overwrite the registered renderer anytime vanilla creates it
        EntityRendererRegistry.register(entityType, ctx -> rendererFactory.andThen((renderer) -> {
            GeckolibCoreRegistry.registerModel(entityClass, renderer.getModel());
            return renderer;
        }).apply(ctx));
    }

    public static <T extends Entity & IAnimatable, E extends EntityType<T>> void registerProjectileRenderer(
            Function<EntityRendererFactory.Context, GeoProjectilesRenderer<T>> rendererFactory,
            E entityType, Class<T> entityClass) {
        // Since vanilla creates EntityRenderers on demand, we can't just add them directly to the registry,
        // so instead we overwrite the registered renderer anytime vanilla creates it
        EntityRendererRegistry.register(entityType, ctx -> rendererFactory.andThen((renderer) -> {
            GeckolibCoreRegistry.registerModel(entityClass, renderer.getModel());
            return renderer;
        }).apply(ctx));
    }

    public static <NEW extends IAnimatable, OLD extends LivingEntity> void registerReplacedEntityRenderer(
            Function<EntityRendererFactory.Context, ? extends GeoReplacedEntityRenderer> rendererFactory,
            EntityType<OLD> oldEntityType, Class<NEW> newEntityClass) {
        // Since vanilla creates EntityRenderers on demand, we can't just add them directly to the registry,
        // so instead we overwrite the registered renderer anytime vanilla creates it
        EntityRendererRegistry.register(oldEntityType, ctx -> rendererFactory.andThen((renderer) -> {
            GeckolibCoreRegistry.registerModel(newEntityClass, renderer.getModel());
            return renderer;
        }).apply(ctx));
    }
}
