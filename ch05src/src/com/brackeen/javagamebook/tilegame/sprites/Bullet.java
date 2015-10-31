package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

public class Bullet extends Sprite {
	
	public static final int STATE_NORMAL = 0;
	public static final int STATE_DYING = 1;
	public static final int STATE_DEAD = 2;
	private int state;
    private long stateTime;
    private static final int DIE_TIME = 1000;
    private float x;
    private float y;
    // velocity (pixels per millisecond)
    private float dx;
    private float dy;
    
	public Bullet(Animation anim) {
		super(anim);
		state = STATE_NORMAL;
	}

	public void collideHorizontal() {
		setVelocityX(0.1f);
	}

	public Object clone() {
		// use reflection to create the correct subclass
		Constructor constructor = getClass().getConstructors()[0];
		try {
			return constructor.newInstance(new Object[] {
					(Animation)anim.clone()
			});
		}
		catch (Exception ex) {
			// should never happen
			ex.printStackTrace();
			return null;
		}
	}

	public void setState(int state) {
		if (this.state != state) {
			this.state = state;
			stateTime = 0;
			if (state == STATE_DYING) {
				setVelocityX(0);
				setVelocityY(0);
			}
		}
	}

	public int getState() {
		return state;
	}


	//public void update(long elapsedTime) {
	//	x += dx * elapsedTime;
	//	y += dy * elapsedTime;
	//	anim.update(elapsedTime);
		// update to "dead" state
	//	stateTime += elapsedTime;
	//	if (state == STATE_DYING && stateTime >= DIE_TIME) {
	//		setState(STATE_DEAD);
	//	}
	//}


}
