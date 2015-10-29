package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

public class Bullet extends Sprite {
	
	private Animation anim;
	//private Animation right;
	//private Animation deadleft;
	//private Animation deadright;
	private float x;
    private float y;
    private float dx;
    private float dy;
	public Bullet(Animation anim) {
		super(anim);
	}

	 public void collideHorizontal() {
	        setVelocityX(0.1f);
	    }
	 
	 
	
}
