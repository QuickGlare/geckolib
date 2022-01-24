package software.bernie.example.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ExampleEntity extends PathAwareEntity implements IAnimatable, IAnimationTickable {
	AnimationFactory factory = new AnimationFactory(this);
	private boolean isAnimating = false;

	public ExampleEntity(EntityType<? extends PathAwareEntity> type, World worldIn) {
		super(type, worldIn);
		this.ignoreCameraFrustum = true;
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
		if (this.isAnimating) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.bat.fly", true));
		} else {
			event.getController().clearAnimationCache();
			return PlayState.STOP;
		}
		return PlayState.CONTINUE;
	}

	@Override
	public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
		if (hand == Hand.MAIN_HAND) {
			this.isAnimating = !this.isAnimating;
		}
		return super.interactAt(player, hitPos, hand);
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<ExampleEntity>(this, "controller", 0, this::predicate));
	}

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0F));
		super.initGoals();
	}

	@Override
	public int tickTimer() {
		return age;
	}
}
