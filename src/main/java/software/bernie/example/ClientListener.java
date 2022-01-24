/*
 * Copyright (c) 2020.
 * Author: Bernie G. (Gecko)
 */

package software.bernie.example;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.blockrenderlayer.BlockRenderLayerMapImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import software.bernie.example.block.tile.BotariumTileEntity;
import software.bernie.example.block.tile.FertilizerTileEntity;
import software.bernie.example.client.renderer.armor.PotatoArmorRenderer;
import software.bernie.example.client.renderer.entity.*;
import software.bernie.example.client.renderer.item.JackInTheBoxRenderer;
import software.bernie.example.client.renderer.item.PistolRender;
import software.bernie.example.client.renderer.tile.BotariumTileRenderer;
import software.bernie.example.client.renderer.tile.FertilizerTileRenderer;
import software.bernie.example.entity.*;
import software.bernie.example.registry.BlockRegistry;
import software.bernie.example.registry.EntityRegistry;
import software.bernie.example.registry.ItemRegistry;
import software.bernie.example.registry.TileRegistry;
import software.bernie.geckolib3.GeckoLib;
import software.bernie.geckolib3.GeckolibRegistry;

import java.util.UUID;
import java.util.function.Function;

@SuppressWarnings("deprecation")
public class ClientListener implements ClientModInitializer {

    @SuppressWarnings({"unchecked"})
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment() && !GeckoLibMod.DISABLE_IN_DEV) {

            //Register entities
            GeckolibRegistry.registerLivingEntityRenderer(ExampleGeoRenderer::new, EntityRegistry.GEO_EXAMPLE_ENTITY,
                    ExampleEntity.class);
            GeckolibRegistry.registerLivingEntityRenderer(LERenderer::new, EntityRegistry.GEOLAYERENTITY,
                    LEEntity.class);
            GeckolibRegistry.registerLivingEntityRenderer(BikeGeoRenderer::new, EntityRegistry.BIKE_ENTITY,
                    BikeEntity.class);

            //Register projectiles
            GeckolibRegistry.registerProjectileRenderer(RocketRender::new, EntityRegistry.ROCKET,
                    RocketProjectile.class);

            //Register items
            GeckolibRegistry.registerItemRenderer(new JackInTheBoxRenderer(), ItemRegistry.JACK_IN_THE_BOX);
            GeckolibRegistry.registerItemRenderer(new PistolRender(), ItemRegistry.PISTOL);

            //Register armor
            GeckolibRegistry.registerArmorRenderer(new PotatoArmorRenderer(), ItemRegistry.POTATO_HEAD,
                    ItemRegistry.POTATO_CHEST, ItemRegistry.POTATO_LEGGINGS, ItemRegistry.POTATO_BOOTS);

            //Register blocks
            GeckolibRegistry.registerBlockRenderer((ctx) -> new BotariumTileRenderer(), TileRegistry.BOTARIUM_TILE,
                    BotariumTileEntity.class);
            GeckolibRegistry.registerBlockRenderer((ctx) -> new FertilizerTileRenderer(), TileRegistry.FERTILIZER,
                    FertilizerTileEntity.class);

            GeckolibRegistry.registerReplacedEntityRenderer(ReplacedCreeperRenderer::new, EntityType.CREEPER,
                    ReplacedCreeperEntity.class);

            BlockRenderLayerMapImpl.INSTANCE.putBlock(BlockRegistry.BOTARIUM_BLOCK, RenderLayer.getCutout());
            ClientSidePacketRegistry.INSTANCE.register(EntityPacket.ID, EntityPacketOnClient::onPacket);
        }
    }

    public class EntityPacketOnClient {
        @Environment(EnvType.CLIENT)
        public static void onPacket(PacketContext context, PacketByteBuf byteBuf) {
            EntityType<?> type = Registry.ENTITY_TYPE.get(byteBuf.readVarInt());
            UUID entityUUID = byteBuf.readUuid();
            int entityID = byteBuf.readVarInt();
            double x = byteBuf.readDouble();
            double y = byteBuf.readDouble();
            double z = byteBuf.readDouble();
            float pitch = (byteBuf.readByte() * 360) / 256.0F;
            float yaw = (byteBuf.readByte() * 360) / 256.0F;
            context.getTaskQueue().execute(() -> {
                @SuppressWarnings("resource")
                ClientWorld world = MinecraftClient.getInstance().world;
                Entity entity = type.create(world);
                if (entity != null) {
                    entity.updatePosition(x, y, z);
                    entity.updateTrackedPosition(x, y, z);
                    entity.setPitch(pitch);
                    entity.setYaw(yaw);
                    entity.setId(entityID);
                    entity.setUuid(entityUUID);
                    world.addEntity(entityID, entity);
                }
            });
        }
    }

    public class EntityPacket {
        public static final Identifier ID = new Identifier(GeckoLib.MOD_ID, "spawn_entity");

        public static Packet<?> createPacket(Entity entity) {
            PacketByteBuf buf = createBuffer();
            buf.writeVarInt(Registry.ENTITY_TYPE.getRawId(entity.getType()));
            buf.writeUuid(entity.getUuid());
            buf.writeVarInt(entity.getId());
            buf.writeDouble(entity.getX());
            buf.writeDouble(entity.getY());
            buf.writeDouble(entity.getZ());
            buf.writeByte(MathHelper.floor(entity.getPitch() * 256.0F / 360.0F));
            buf.writeByte(MathHelper.floor(entity.getYaw() * 256.0F / 360.0F));
            buf.writeFloat(entity.getPitch());
            buf.writeFloat(entity.getYaw());
            return ServerPlayNetworking.createS2CPacket(ID, buf);
        }

        private static PacketByteBuf createBuffer() {
            return new PacketByteBuf(Unpooled.buffer());
        }

    }
}
