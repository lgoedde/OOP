package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;


/**
    A Grub is a Creature that moves slowly on the ground.
*/
public class Grub extends Creature {

	public boolean shouldShoot = false;
	
    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }


    public float getMaxSpeed() {
        return 0.05f;
    }
    
    public void wakeUp() {
    	if (getState() == STATE_NORMAL && getVelocityX() == 0) {
            setVelocityX(-getMaxSpeed());
        	this.shouldShoot = true;
        }

    }

}
