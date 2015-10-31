package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;
    private int health;
    private boolean onGround;
    public boolean createBullet;

    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
        this.health = 20;
    }


    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }


    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(JUMP_SPEED);
        }
    }


    public float getMaxSpeed() {
        return 0.5f;
    }
    
    public void decreaseHealth(int num) {
    	if ((this.health - num) < 0) {
    		this.health = 0;
    	}
    	else {
    	this.health -= num;
    	}
    }
    
    public void increaseHealth(int num) {
    	if ((this.health + num) > 40 ) {
    		this.health = 40;
    	}
    	else {
    		this.health += num;
    	}
    }
    
    public void setHealth(int num) {
    	if(this.health > 0 && this.health <= 40) {
    		this.health = num;
    	}
    	
    }
    
    public int getHealth() {
		return this.health;
	}

}
