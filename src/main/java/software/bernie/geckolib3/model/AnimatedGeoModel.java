package software.bernie.geckolib3.model;

import java.util.Collections;
import java.util.Optional;

import com.eliotlash.molang.MolangParser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.model.IAnimatableModel;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.processor.AnimationProcessor;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.file.AnimationFile;
import software.bernie.geckolib3.geo.exception.GeckoLibException;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.provider.AbstractGeoModel;
import software.bernie.geckolib3.model.provider.IAnimationProvider;
import software.bernie.geckolib3.resource.GeckoLibCache;
import software.bernie.geckolib3.util.MolangUtils;

public abstract class AnimatedGeoModel<T extends IAnimatable> extends AbstractGeoModel<T>
		implements IAnimatableModel<T>, IAnimationProvider<T> {
	@SuppressWarnings("rawtypes")
	private final AnimationProcessor animationProcessor;
	private GeoModel currentModel;

	@SuppressWarnings("rawtypes")
	protected AnimatedGeoModel() {
		this.animationProcessor = new AnimationProcessor(this);
	}

	public void registerBone(GeoBone bone) {
		registerModelRenderer(bone);

		for (GeoBone childBone : bone.childBones) {
			registerBone(childBone);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void setLivingAnimations(T entity, Integer uniqueID, AnimationEvent customPredicate) {
		// Each animation has it's own collection of animations (called the
		// EntityAnimationManager), which allows for multiple independent animations
		AnimationData manager = entity.getFactory().getOrCreateAnimationData(uniqueID);
		if (manager.startTick == null) {
			manager.startTick = getCurrentTick();
		}

		if (!MinecraftClient.getInstance().isPaused() || manager.shouldPlayWhilePaused) {
			manager.tick = (getCurrentTick() - manager.startTick);
			double gameTick = manager.tick;
			double deltaTicks = gameTick - lastGameTickTime;
			seekTime += deltaTicks;
			lastGameTickTime = gameTick;
		}

		AnimationEvent<T> predicate;
		if (customPredicate == null) {
			predicate = new AnimationEvent<T>(entity, 0, 0, 0, false, Collections.emptyList());
		} else {
			predicate = customPredicate;
		}

		predicate.animationTick = seekTime;

		animationProcessor.preAnimationSetup(predicate.getAnimatable(), seekTime);
		if (!this.animationProcessor.getModelRendererList().isEmpty()) {
			animationProcessor.tickAnimation(entity, uniqueID, seekTime, predicate, GeckoLibCache.getInstance().parser,
					shouldCrashOnMissing);
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public AnimationProcessor getAnimationProcessor() {
		return this.animationProcessor;
	}

	public void registerModelRenderer(IBone modelRenderer) {
		animationProcessor.registerModelRenderer(modelRenderer);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<Animation> getAnimation(String name, IAnimatable animatable) {
		AnimationFile animation = GeckoLibCache.getInstance().getAnimations()
				.get(this.getAnimationResource((T) animatable));
		if (animation == null) {
			throw new GeckoLibException(this.getAnimationResource((T) animatable),
					"Could not find animation file. Please double check name.");
		}
		return animation.getAnimation(name);
	}

	@Override
	public GeoModel getGeoModel(Identifier location) {
		GeoModel model = super.getGeoModel(location);
		if (model == null) {
			throw new GeckoLibException(location,
					"Could not find model. If you are getting this with a built mod, please just restart your game.");
		}
		if (model != currentModel) {
			this.animationProcessor.clearModelRendererList();
			for (GeoBone bone : model.topLevelBones) {
				registerBone(bone);
			}
			this.currentModel = model;
		}
		return model;
	}

	@Override
	public void setMolangQueries(IAnimatable animatable, double currentTick) {
		MolangParser parser = GeckoLibCache.getInstance().parser;
		MinecraftClient minecraftInstance = MinecraftClient.getInstance();

		parser.setValue("query.actor_count", minecraftInstance.world.getRegularEntityCount());
		parser.setValue("query.time_of_day", MolangUtils.normalizeTime(minecraftInstance.world.getTimeOfDay()));
		parser.setValue("query.moon_phase", minecraftInstance.world.getMoonPhase());

		if (animatable instanceof Entity) {
			parser.setValue("query.distance_from_camera",
					minecraftInstance.gameRenderer.getCamera().getPos().distanceTo(((Entity) animatable).getPos()));
			parser.setValue("query.is_on_ground", MolangUtils.booleanToFloat(((Entity) animatable).isOnGround()));
			parser.setValue("query.is_in_water", MolangUtils.booleanToFloat(((Entity) animatable).isTouchingWater()));
			// Should probably check specifically whether it's in rain?
			parser.setValue("query.is_in_water_or_rain", MolangUtils.booleanToFloat(((Entity) animatable).isWet()));

			if (animatable instanceof LivingEntity) {
				LivingEntity livingEntity = (LivingEntity) animatable;
				parser.setValue("query.health", livingEntity.getHealth());
				parser.setValue("query.max_health", livingEntity.getMaxHealth());

				parser.setValue("query.is_on_fire", MolangUtils.booleanToFloat(livingEntity.isOnFire()));
				// Doesn't work for some reason?
				parser.setValue("query.on_fire_time", livingEntity.getFireTicks());

				Vec3d velocity = livingEntity.getVelocity();
				// Must be always positive to prevent NaNs
				float groundSpeed = MathHelper.sqrt((float) (+velocity.x * +velocity.z));
				parser.setValue("query.ground_speed", groundSpeed);

				float yawSpeed = livingEntity.getYaw((float) currentTick)
						- livingEntity.getYaw((float) (currentTick - 0.1));
				parser.setValue("query.yaw_speed", yawSpeed);
			}
		}
	}

	@Override
	public double getCurrentTick() {
		return GlfwUtil.getTime() * 20;
	}
}