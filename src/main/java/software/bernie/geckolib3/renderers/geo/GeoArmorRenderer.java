package software.bernie.geckolib3.renderers.geo;

import java.util.Arrays;
import java.util.Objects;

import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import software.bernie.geckolib3.compat.PatchouliCompat;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.model.IModel;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.core.util.Color;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.util.GeoUtils;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GeoArmorRenderer<T extends ArmorItem & IAnimatable> implements IGeoRenderer<T>, ArmorRenderer {
	protected AnimatedGeoModel<T> model;
	protected ItemStack currentItemStack;

	// Set these to the names of your armor's bones, or null if you aren't using
	// them
	public String headBone = "armorHead";
	public String bodyBone = "armorBody";
	public String rightArmBone = "armorRightArm";
	public String leftArmBone = "armorLeftArm";
	public String rightLegBone = "armorRightLeg";
	public String leftLegBone = "armorLeftLeg";
	public String rightBootBone = "armorRightBoot";
	public String leftBootBone = "armorLeftBoot";

	private T currentArmorItem;
	private LivingEntity entityLiving;
	private ItemStack itemStack;
	private EquipmentSlot armorSlot;
	private BipedEntityModel baseModel;

	public GeoArmorRenderer(AnimatedGeoModel<T> model) {
		this.model = model;
	}

	public void setModel(AnimatedGeoModel<T> model) {
		this.model = model;
	}

	@Override
	public void render(GeoModel model, T animatable, float partialTicks, RenderLayer type, MatrixStack matrixStackIn,
			VertexConsumerProvider renderTypeBuffer, VertexConsumer vertexBuilder, int packedLightIn,
			int packedOverlayIn, float red, float green, float blue, float alpha) {
		IGeoRenderer.super.render(model, animatable, partialTicks, type, matrixStackIn, renderTypeBuffer, vertexBuilder,
				packedLightIn, packedOverlayIn, red, green, blue, alpha);
	}

	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack,
			LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
		setCurrentItem(entity, stack, slot, contextModel);
		this.render(matrices, vertexConsumers, light);
	}

	public void render(MatrixStack stack, VertexConsumerProvider bufferIn, int packedLightIn) {
		stack.translate(0.0D, 24 / 16F, 0.0D);
		stack.scale(-1.0F, -1.0F, 1.0F);
		GeoModel model = this.model.getGeoModel(this.model.getModelResource(currentArmorItem));

		AnimationEvent<T> itemEvent = new AnimationEvent<T>(this.currentArmorItem, 0, 0,
				MinecraftClient.getInstance().getTickDelta(), false,
				Arrays.asList(this.itemStack, this.entityLiving, this.armorSlot));
		this.model.setLivingAnimations(currentArmorItem, this.getUniqueID(this.currentArmorItem), itemEvent);

		this.fitToBiped();
		this.applySlot();
		stack.push();
		MinecraftClient.getInstance().getTextureManager().bindTexture(getTextureResource(currentArmorItem));
		Color renderColor = getRenderColor(currentArmorItem, 0, stack, bufferIn, null, packedLightIn);
		RenderLayer renderType = getRenderType(currentArmorItem, 0, stack, bufferIn, null, packedLightIn,
				getTextureResource(currentArmorItem));
		render(model, currentArmorItem, 0, renderType, stack, bufferIn, null, packedLightIn, OverlayTexture.DEFAULT_UV,
				(float) renderColor.getRed() / 255f, (float) renderColor.getGreen() / 255f,
				(float) renderColor.getBlue() / 255f, (float) renderColor.getAlpha() / 255);

		if (FabricLoader.getInstance().isModLoaded("patchouli")) {
			PatchouliCompat.patchouliLoaded(stack);
		}
		stack.pop();
		stack.scale(-1.0F, -1.0F, 1.0F);
		stack.translate(0.0D, -1.501F, 0.0D);
	}

	private void fitToBiped() {
		if (!(this.entityLiving instanceof ArmorStandEntity)) {
			if (this.headBone != null) {
				IBone headBone = this.model.getBone(this.headBone);
				GeoUtils.copyRotations(baseModel.head, headBone);
				headBone.setPositionX(baseModel.head.pivotX);
				headBone.setPositionY(-baseModel.head.pivotY);
				headBone.setPositionZ(baseModel.head.pivotZ);
			}

			if (this.bodyBone != null) {
				IBone bodyBone = this.model.getBone(this.bodyBone);
				GeoUtils.copyRotations(baseModel.body, bodyBone);
				bodyBone.setPositionX(baseModel.body.pivotX);
				bodyBone.setPositionY(-baseModel.body.pivotY);
				bodyBone.setPositionZ(baseModel.body.pivotZ);
			}
			if (this.rightArmBone != null) {
				IBone rightArmBone = this.model.getBone(this.rightArmBone);
				GeoUtils.copyRotations(baseModel.rightArm, rightArmBone);
				rightArmBone.setPositionX(baseModel.rightArm.pivotX + 5);
				rightArmBone.setPositionY(2 - baseModel.rightArm.pivotY);
				rightArmBone.setPositionZ(baseModel.rightArm.pivotZ);
			}

			if (this.leftArmBone != null) {
				IBone leftArmBone = this.model.getBone(this.leftArmBone);
				GeoUtils.copyRotations(baseModel.leftArm, leftArmBone);
				leftArmBone.setPositionX(baseModel.leftArm.pivotX - 5);
				leftArmBone.setPositionY(2 - baseModel.leftArm.pivotY);
				leftArmBone.setPositionZ(baseModel.leftArm.pivotZ);
			}
			if (this.rightLegBone != null) {
				IBone rightLegBone = this.model.getBone(this.rightLegBone);
				GeoUtils.copyRotations(baseModel.rightLeg, rightLegBone);
				rightLegBone.setPositionX(baseModel.rightLeg.pivotX + 2);
				rightLegBone.setPositionY(12 - baseModel.rightLeg.pivotY);
				rightLegBone.setPositionZ(baseModel.rightLeg.pivotZ);
				if (this.rightBootBone != null) {
					IBone rightBootBone = this.model.getBone(this.rightBootBone);
					GeoUtils.copyRotations(baseModel.rightLeg, rightBootBone);
					rightBootBone.setPositionX(baseModel.rightLeg.pivotX + 2);
					rightBootBone.setPositionY(12 - baseModel.rightLeg.pivotY);
					rightBootBone.setPositionZ(baseModel.rightLeg.pivotZ);
				}
			}
			if (this.leftLegBone != null) {
				IBone leftLegBone = this.model.getBone(this.leftLegBone);
				GeoUtils.copyRotations(baseModel.leftLeg, leftLegBone);
				leftLegBone.setPositionX(baseModel.leftLeg.pivotX - 2);
				leftLegBone.setPositionY(12 - baseModel.leftLeg.pivotY);
				leftLegBone.setPositionZ(baseModel.leftLeg.pivotZ);
				if (this.leftBootBone != null) {
					IBone leftBootBone = this.model.getBone(this.leftBootBone);
					GeoUtils.copyRotations(baseModel.leftLeg, leftBootBone);
					leftBootBone.setPositionX(baseModel.leftLeg.pivotX - 2);
					leftBootBone.setPositionY(12 - baseModel.leftLeg.pivotY);
					leftBootBone.setPositionZ(baseModel.leftLeg.pivotZ);
				}
			}
		}
	}


	@Override
	public AnimatedGeoModel<T> getModel() {
		return this.model;
	}

	@Override
	public Identifier getTextureResource(T instance) {
		return this.model.getTextureResource(instance);
	}

	/**
	 * Everything after this point needs to be called every frame before rendering
	 */
	public GeoArmorRenderer<T> setCurrentItem(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot,
			BipedEntityModel model) {
		this.entityLiving = entityLiving;
		this.itemStack = itemStack;
		this.armorSlot = armorSlot;
		this.currentArmorItem = (T) itemStack.getItem();
		this.baseModel = model;

		return this;
	}

	@SuppressWarnings("incomplete-switch")
	public GeoArmorRenderer<T> applySlot() {
		model.getGeoModel(model.getModelResource(currentArmorItem));

		IBone headBone = this.getAndHideBone(this.headBone);
		IBone bodyBone = this.getAndHideBone(this.bodyBone);
		IBone rightArmBone = this.getAndHideBone(this.rightArmBone);
		IBone leftArmBone = this.getAndHideBone(this.leftArmBone);
		IBone rightLegBone = this.getAndHideBone(this.rightLegBone);
		IBone leftLegBone = this.getAndHideBone(this.leftLegBone);
		IBone rightBootBone = this.getAndHideBone(this.rightBootBone);
		IBone leftBootBone = this.getAndHideBone(this.leftBootBone);

		switch (armorSlot) {
		case HEAD:
			if (headBone != null)
				headBone.setHidden(false);
			break;
		case CHEST:
			if (bodyBone != null)
				bodyBone.setHidden(false);
			if (rightArmBone != null)
				rightArmBone.setHidden(false);
			if (leftArmBone != null)
				leftArmBone.setHidden(false);
			break;
		case LEGS:
			if (rightLegBone != null)
				rightLegBone.setHidden(false);
			if (leftLegBone != null)
				leftLegBone.setHidden(false);
			break;
		case FEET:
			if (rightBootBone != null)
				rightBootBone.setHidden(false);
			if (leftBootBone != null)
				leftBootBone.setHidden(false);
			break;
		}
		return this;
	}

	protected IBone getAndHideBone(String boneName) {
		if (boneName != null) {
			final IBone bone = this.model.getBone(boneName);
			bone.setHidden(true);
			return bone;
		}
		return null;
	}

	@Override
	public Integer getUniqueID(T animatable) {
		return Objects.hash(this.armorSlot, itemStack.getItem(), itemStack.getCount(),
				itemStack.hasNbt() ? itemStack.getNbt().toString() : 1, this.entityLiving.getUuid().toString());
	}
}