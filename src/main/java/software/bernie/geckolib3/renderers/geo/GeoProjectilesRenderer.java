package software.bernie.geckolib3.renderers.geo;

import java.util.Collections;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.model.IAnimatableModel;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.model.IModel;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.model.provider.data.EntityModelData;

@SuppressWarnings("unchecked")
public class GeoProjectilesRenderer<T extends Entity & IAnimatable> extends EntityRenderer<T>
		implements IGeoRenderer<T> {

	private final AnimatedGeoModel<T> model;

	public GeoProjectilesRenderer(EntityRendererFactory.Context ctx, AnimatedGeoModel<T> model) {
		super(ctx);
		this.model = model;
	}

	@Override
	public void render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn,
			VertexConsumerProvider bufferIn, int packedLightIn) {
		GeoModel model = this.model.getGeoModel(this.model.getModelResource(entityIn));
		matrixStackIn.push();
		matrixStackIn.multiply(Vec3f.POSITIVE_Y
				.getDegreesQuaternion(MathHelper.lerp(partialTicks, entityIn.prevYaw, entityIn.getYaw()) - 90.0F));
		matrixStackIn.multiply(Vec3f.POSITIVE_Z
				.getDegreesQuaternion(MathHelper.lerp(partialTicks, entityIn.prevPitch, entityIn.getPitch())));
		matrixStackIn.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(45.0F));
		MinecraftClient.getInstance().getTextureManager().bindTexture(getTexture(entityIn));
		Color renderColor = getRenderColor(entityIn, partialTicks, matrixStackIn, bufferIn, null, packedLightIn);
		RenderLayer renderType = getRenderType(entityIn, partialTicks, matrixStackIn, bufferIn, null, packedLightIn,
				getTexture(entityIn));
		render(model, entityIn, partialTicks, renderType, matrixStackIn, bufferIn, null, packedLightIn,
				getPackedOverlay(entityIn, 0), (float) renderColor.getRed() / 255f,
				(float) renderColor.getGreen() / 255f, (float) renderColor.getBlue() / 255f,
				(float) renderColor.getAlpha() / 255);

		EntityModelData entityModelData = new EntityModelData();
		AnimationEvent<T> predicate = new AnimationEvent<T>(entityIn, 0.0F, 0.0F, partialTicks,
				false, Collections.singletonList(entityModelData));
		this.model.setLivingAnimations(entityIn, this.getUniqueID(entityIn), predicate);
		matrixStackIn.pop();
		super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
	}

	public static int getPackedOverlay(Entity livingEntityIn, float uIn) {
		return OverlayTexture.getUv(OverlayTexture.getU(uIn), false);
	}

	@Override
	public AnimatedGeoModel<T> getModel() {
		return this.model;
	}

	@Override
	public Identifier getTextureResource(T instance) {
		return this.model.getTextureResource(instance);
	}

	@Override
	public Identifier getTexture(T entity) {
		return getTextureResource(entity);
	}

}