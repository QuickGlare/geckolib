package software.bernie.geckolib3.renderers.geo;

import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.util.GeckoLibUtil;

import java.util.Collections;

@SuppressWarnings({"rawtypes", "unchecked"})
public class GeoItemRenderer<T extends Item & IAnimatable>
        implements IGeoRenderer<T>, BuiltinItemRendererRegistry.DynamicItemRenderer {
    protected AnimatedGeoModel<T> model;
    protected ItemStack currentItemStack;

    public GeoItemRenderer(AnimatedGeoModel<T> model) {
        this.model = model;
    }

    public void setModel(AnimatedGeoModel<T> model) {
        this.model = model;
    }

    @Override
    public AnimatedGeoModel<T> getModel() {
        return this.model;
    }

    @Override
    public void render(ItemStack itemStack, ModelTransformation.Mode mode, MatrixStack matrixStackIn,
                       VertexConsumerProvider bufferIn, int combinedLightIn, int combinedOverlayIn) {
        this.render((T) itemStack.getItem(), matrixStackIn, bufferIn, combinedLightIn, itemStack);
    }

    public void render(T animatable, MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn,
                       ItemStack itemStack) {
        this.currentItemStack = itemStack;
        AnimationEvent<T> itemEvent = new AnimationEvent<>(animatable, 0, 0,
                MinecraftClient.getInstance().getTickDelta(), false, Collections.singletonList(itemStack));
        model.setLivingAnimations(animatable, this.getUniqueID(animatable), itemEvent);
        stack.push();
        // stack.translate(0, 0.01f, 0);
        stack.translate(0.5, 0.5, 0.5);

        MinecraftClient.getInstance().getTextureManager().bindTexture(getTextureResource(animatable));
        GeoModel model = this.model.getGeoModel(this.model.getModelResource(animatable));
        Color renderColor = getRenderColor(animatable, 0, stack, bufferIn, null, packedLightIn);
        RenderLayer renderType = getRenderType(animatable, 0, stack, bufferIn, null, packedLightIn,
                getTextureResource(animatable));
        render(model, animatable, 0, renderType, stack, bufferIn, null, packedLightIn, OverlayTexture.DEFAULT_UV,
                (float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
                (float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);
        stack.pop();
    }

    @Override
    public Identifier getTextureResource(T instance) {
        return this.model.getTextureResource(instance);
    }

    @Override
    public Integer getUniqueID(T animatable) {
        return GeckoLibUtil.getIDFromStack(currentItemStack);
    }
}
