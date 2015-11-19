package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;
import com.brackeen.javagamebook.tilegame.sprites.PowerUp.Mushroom;
/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;

    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private static ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private Sound fireSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private GameAction shoot;
    private String direction;
    private int count = 0; //Counts how many bullets we've fired
    private int rate = 300; //rate which the bullets fire
    private boolean pause = false; //1 second wait for cooldown
    private long prev = 0; //records point at which previous bullet was fired
    private long p_start = 0; //time to start count for cooldown period
    private int grubRate = 2 * rate; //grub rate is 2x the player rate
    private long gprev = 0; //previous time for grub
    private boolean onscreen = false; //check to see if grub is on screen so we can start shooting
    private float curr_x = 0;
    private float curr_y = 0; //current positions for the grub
    private float prev_x = 0;
    private long still = 0; //If the player is still he gets some health
    private float gbulletCreation [];
    private int create_index = 0;
    private int star_step = 0;
    
    public void init() {
        super.init();

        // set up input manager
        initInput();
       
        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("./sounds/prize.wav");
        boopSound = soundManager.getSound("./sounds/boop2.wav");
        //fireSound = soundManager.getSound("./sounds/bullet.wav");

        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
        
        //initial direction
        direction = "right";
    }


    /**
        Closes any resurces used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);

        shoot = new GameAction("shoot");

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(shoot, KeyEvent.VK_S);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }
        Player player = (Player)map.getPlayer();
        player.createBullet = false;
        if (player.isAlive()) {
            float velocityX = 0;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
                direction = "left";
                if(star_step > 0){
                	star_step = star_step -1;
                }
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
                direction = "right";
                if(star_step > 0){
                	star_step = star_step -1;
                }
            }
            System.out.print(star_step + "\n");
            if (jump.isPressed()) {
                player.jump(false);
            }
            if(shoot.isPressed()) {
            	player.createBullet = true;
            }
            player.setVelocityX(velocityX);
                    
        }
        
    }
 

    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }
        
        

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }

    public boolean checkMovement(int curr, int prev) {
    	if(curr - prev > 1) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }
    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();
        Player newPlayer = (Player)map.getPlayer(); //so we can update health n stuff

        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
        	newPlayer.setHealth(0);
        	still = 0;
        	prev_x = 0;
            map = resourceManager.reloadMap();
            return;
        }
        if(newPlayer.getVelocityX() == 0) {
        	still += elapsedTime;
        }
        else {
        	still = 0;
        }
        if(newPlayer.getHealth() <= 0) {
        	newPlayer.setState(Creature.STATE_DYING);
        }
        
        if(prev_x == 0) {
        	prev_x = newPlayer.getX();
        }
        
        else {
        	int curr_tile = TileMapRenderer.pixelsToTiles(newPlayer.getX());
        	int prev_tile = TileMapRenderer.pixelsToTiles(prev_x);
        	if(checkMovement(curr_tile,prev_tile)) {
        		newPlayer.increaseHealth(1);
        		prev_x = newPlayer.getX();
        	}
        if(still >= 1000) {
        	newPlayer.increaseHealth(5);
        	still = 0;
        }
        
        	
        }
        
        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

        gbulletCreation = new float[100];
        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);
                    
                }
            }
            if( sprite instanceof Bullet ){
            	Bullet bullet = (Bullet)sprite;
                if (bullet.getState() == Bullet.STATE_DEAD) {
                    i.remove();
                }
                else {
        		checkBulletCollision((Bullet)sprite);
                }
        	}
            if( sprite instanceof grubBullet ){
            	grubBullet gbullet = (grubBullet)sprite;
                if (gbullet.getState() == grubBullet.STATE_DEAD) {
                    i.remove();
                }
                
            }
            // normal update
            sprite.update(elapsedTime);
        }
        
        for( int h=0; h < create_index; h=h+2) {
        	instantiateGbullet(gbulletCreation[h], gbulletCreation[h+1]);
        }
        create_index = 0;
    }

    public boolean checkRate() {
    	long curr_rate = System.currentTimeMillis() - prev;
    	if (curr_rate > rate) {
    		return true;
    	}
    	else { return false; }
    }
    
    public void countBullets() {
    	long curr_rate = System.currentTimeMillis() - prev;
    	int grace_rate = rate + 30; 
    	if (curr_rate <= grace_rate) {
    		count += 1; //increase the count if we are firing in auto mode
    	}
    	else {
    		count = 0; //we aren't firing in automode so reset for next automode
    	}
    }
   
    public void instantiateBullet() {
    	Bullet bullet = (Bullet)resourceManager.getBullets(1).clone();
		Player player1 = (Player)map.getPlayer();
		bullet.setX( (direction == "right") ? player1.getX() : player1.getX());
		bullet.setVelocityX( (direction == "right") ? .5f : -.5f );
		bullet.setY(player1.getY() + 10);
		map.addSprite(bullet);
    }
    
    public void instantiateGbullet(float bull_x, float bull_y) {
    	//long curr_wait = System.currentTimeMillis() - gprev;
    	gprev +=1;
    	System.out.print("B4 check GBullet\n");
    	if(gprev == 700 && System.currentTimeMillis() > 1000) {
    		System.out.print("In check GBullet\n");
    		grubBullet gbullet = (grubBullet)resourceManager.getBullets(0).clone();
    		gbullet.setX(bull_x);
    		gbullet.setVelocityX(-.5f);
    		gbullet.setY(bull_y);
    		map.addSprite(gbullet);
    		onscreen = false;
    		gprev = 0;
    	}
    }
    
    public void produceBullets() {
    	if (checkRate() && !(pause)) {
    		countBullets();
    		if (count < 10) {
    			instantiateBullet();
    			prev = System.currentTimeMillis();
    			//need to play a shoot sound here
    		}
    		else {
    			//We need to go to cooldown period so set pause to true and start the timer for count
    			pause = true;
    			p_start = System.currentTimeMillis();		
    		}
    	}
    	
    	else {
    		//we are waiting for cooldown period to be over
    		long curr_wait = System.currentTimeMillis() - p_start;
    		if (curr_wait >= 1000) {
    			pause = false;
    		}
    	}
    }
    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {

        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }

        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }
        if( creature instanceof Player){
        	Player player1 = (Player)creature;
        	if(player1.createBullet) {
        		System.out.print("producing bullets\n");
            	produceBullets();
            	//soundManager.play(fireSound);
            }
        }
        else {
        	onscreen = true; 
        	if( creature.getVelocityX() != 0 ){
	        	curr_x = creature.getX();
	        	curr_y = creature.getY();
	        	gbulletCreation[create_index] = curr_x;
	        	gbulletCreation[create_index + 1] = curr_y;
	        	create_index += 2;
        	}
        	//instantiateGbullet();
        	//gprev = System.currentTimeMillis();
        
        }
        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }

    }


    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
                soundManager.play(boopSound);
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.increaseHealth(10);
                player.jump(true);
            }
            else {
                // player dies!()
            	if(star_step == 0)
            	{
            		player.setState(Creature.STATE_DYING);
            		player.setHealth(0);
            	}
                //Play a dying sound
            }
        }
        
       else if (collisionSprite instanceof grubBullet) {
    	   if(star_step == 0 ){
    		   player.decreaseHealth(5);
    	   }
    	   grubBullet gbullet = (grubBullet)collisionSprite;
    	   gbullet.setState(grubBullet.STATE_DEAD);
        }
        
      
    }


    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);
        Player player = (Player) map.getPlayer();
        
        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
        	star_step = 500 + star_step;
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
        else if (powerUp instanceof PowerUp.Mushroom){
        	player.increaseHealth(5);
        }
    }
    
    public void checkBulletCollision(Bullet bullet){
    	Sprite collisionSprite = getSpriteCollision(bullet);
    	
    	if (collisionSprite instanceof Grub ){ //&& !(collisionSprite instanceof Player)) {
            Grub badguy = (Grub)collisionSprite;
            //System.out.print(badguy);
            // kill the badguy and make player bounce
            soundManager.play(boopSound);
            badguy.setState(Creature.STATE_DYING);
            //map.removeSprite(bullet);
            bullet.setState(Bullet.STATE_DEAD);
            Player player = (Player)map.getPlayer();
            player.increaseHealth(10);
    	}
    }
    

}
