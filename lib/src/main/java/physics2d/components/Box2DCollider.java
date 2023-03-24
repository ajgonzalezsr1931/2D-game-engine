package physics2d.components;

import org.joml.Vector2f;
import org.joml.Vector2fc;

import components.Component;

public class Box2DCollider extends Collider{
	private Vector2f halfSize = new Vector2f(1);
	private Vector2fc origin = new Vector2f();

	public Vector2f getHalfSize() {
		return halfSize;
	}

	public void setHalfSize(Vector2f halfSize) {
		this.halfSize = halfSize;
	}

	public Vector2fc getOrigin() {
		return this.origin;
	}
}

