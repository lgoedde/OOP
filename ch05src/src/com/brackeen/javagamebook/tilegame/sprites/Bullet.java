package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

public class Bullet extends Creature {
	
	private Animation left;
	private Animation right;
	private Animation deadleft;
	private Animation deadright;
	private float x;
    private float y;
    private float dx;
    private float dy;
	public Bullet(Animation left, Animation right, Animation deadleft, Animation deadright) {
		super(left,right,deadleft,deadright);
	}

	 public void collideHorizontal() {
	        setVelocityX(0);
	    }
	 
	 
	
}
