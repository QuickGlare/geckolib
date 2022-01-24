package software.bernie.geckolib3.file;

import software.bernie.geckolib3.core.builder.Animation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class AnimationFile {
	private final HashMap<String, Animation> animations = new HashMap<>();

	public Optional<Animation> getAnimation(String name) {
		return Optional.ofNullable(animations.get(name));
	}

	public Collection<Animation> getAllAnimations() {
		return this.animations.values();
	}

	public void putAnimation(String name, Animation animation) {
		this.animations.put(name, animation);
	}
}
