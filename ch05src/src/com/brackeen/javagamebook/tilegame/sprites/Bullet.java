package com.brackeen.javagamebook.tilegame.sprites;

import java.lang.reflect.Constructor;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

public class Bullet extends Sprite {
	
	//private Animation right;
	//private Animation deadleft;
	//private Animation deadright;
	public static final int STATE_NORMAL = 1;
	public static final int STATE_DEAD = 0;
    
	public Bullet(Animation anim) {
		super(anim);
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
	 
	  
	
}
