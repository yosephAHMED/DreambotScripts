import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import org.dreambot.api.Client;
import org.dreambot.api.input.Mouse;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.worldhopper.WorldHopper;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.wrappers.interactive.NPC;

@ScriptManifest(author = "mazurrek", description = "Safespots hellhounds with range", name = "HellhoundKiller", category = Category.COMBAT, version = 1)
public class HellhoundKiller extends AbstractScript {

	State state;
	int spot;

	NPC hellHound;

	Area safeSpotArea = new Area(2413, 9787, 2426, 9782);
	Tile playerSafeSpot = new Tile(2419, 9784);
	//Tile demonSafeSpot = new Tile(2851, 9776);
	
	int killCounter = 0;

	// list of worlds, may not be current since worlds get changed quite frequently
	int[] worlds = { 302, 303, 304, 305, 306, 307, 309, 310, 311, 312, 313, 314, 315, 317, 318, 319, 320, 321, 322, 323,
			324, 325, 327, 328, 329, 331, 332, 333, 334, 336, 337, 338, 339, 340, 341, 342, 343, 344, 346, 347, 348,
			350, 351, 352, 354, 355, 356, 357, 358, 359, 360, 362, 365, 367, 368, 369, 370, 374, 375, 376, 377, 378,
			386, 387, 388, 389, 390, 395, 421, 422, 424, 443, 444, 445, 446, 463, 464, 465, 466, 477, 478, 479, 480,
			481, 482, 484, 485, 486, 487, 488, 489, 490, 491, 492, 493, 494, 495, 496, 505, 506, 507, 508, 509, 511,
			512, 513, 514, 515, 516, 517, 518, 519, 520, 521, 522, 523, 524, 525 };

	@Override // Infinite loop
	public int onLoop() {

		// Determined by which state gets returned by getState() then do that case.
		switch (getState()) {

		case STOP:
			log("stop script");
			stop();
			break;

		case LOGOUT:
			log("logout");
			Tabs.logout();
			break;

		case WAITING_FOR_HELLHOUND:
			log("looking for demon");

			// get the closest black demon inside of the safe area
			hellHound = NPCs.closest(hellhound -> hellhound != null && 
					hellhound.getName().contentEquals("Hellhound")
					&& safeSpotArea.contains(hellhound));

			// if we have a demon
			if (hellHound != null) {

				sleep(randomNum(500, 1000));

				// attack demon once
				hellHound.interact("Attack");
				sleep(randomNum(500, 1000));

				// reset the pitch
				Camera.keyboardRotateToPitch(randomNum(316, 380));

				sleep(randomNum(2000, 3000));

				// go to safespot if player is not there
				if (!playerSafeSpot.equals(getLocalPlayer().getTile())) {
					Walking.walkExact(playerSafeSpot);
					sleep(randomNum(1000, 2000));
					hellHound.interact("Attack");
					sleep(randomNum(500, 1000));
				}
			}

			break;

		case ATTACKING:
			log("attacking hellhound");

			// ensure demon is alive and everything is in the safe spot
			if (hellHound != null && playerSafeSpot.equals(getLocalPlayer().getTile())) {

				// move mouse outside of screen while killing demon
				Mouse.moveMouseOutsideScreen();

				// demon is dead, reset target
				if (!hellHound.exists() || hellHound.getHealthPercent() == 0) {
					hellHound = null;

					// sleep after demon death
					sleep(randomNum(1000, 2000));

					// reset player safe spot if we are not already there
					if (!playerSafeSpot.equals(getLocalPlayer().getTile())) {
						Walking.walkExact(playerSafeSpot);
						sleep(randomNum(1000, 2000));
					}

					// adjust pitch to max
//					sleep(randomNum(200, 500));
//					Camera.keyboardRotateToPitch(randomNum(336, 380));
//					sleep(randomNum(200, 500));
//					Camera.rotateToYaw(randomNum(5, 52));
//					sleep(randomNum(1000, 2000));
					
					killCounter++;
				}
			}

			break;

		}
		return 0;
	}

	// State names
	private enum State {
		STOP, LOGOUT, ATTACKING, WAITING_FOR_HELLHOUND
	}

	// Checks if a certain condition is met, then return that state.
	private State getState() {

		if (!Client.isLoggedIn()) {
			state = State.STOP;
		}
//		else if(Client.isLoggedIn()) {
//			state = State.LOGOUT;
//		}
		else if (playerSafeSpot.equals(getLocalPlayer().getTile()) && !getLocalPlayer().isInCombat()) {
			// at safe-spot and not in combat
			state = State.WAITING_FOR_HELLHOUND;
		} else if (playerSafeSpot.equals(getLocalPlayer().getTile()) && getLocalPlayer().isInCombat()) {
			// at safe-spot and in combat
			state = State.ATTACKING;
		}

		return state;
	}

	// When script start load this.
	public void onStart() {
		log("Bot Started");
		for (int i = 0; i < worlds.length; i++) {
			if (worlds[i] == Client.getCurrentWorld()) {
				spot = i + 1;
			}
		}
	}

	// When script ends do this.
	public void onExit() {
		log("Bot Ended");
	}

	public void hop() { // hops to the next world if called.
		if (Client.getCurrentWorld() == worlds[worlds.length - 1]) {
			spot = 0;
			WorldHopper.hopWorld(worlds[spot]);
		} else {
			WorldHopper.hopWorld(worlds[spot]);
			spot++;
		}

	}

	// creates a random number in between i and k
	public int randomNum(int i, int k) {
		int num = (int) (Math.random() * (k - i + 1)) + i;
		return num;
	}

	@Override // plain gui
	public void onPaint(Graphics g) {
		g.setColor(Color.RED);
		g.setFont(new Font("Arial", Font.BOLD, 15));
		g.drawString("Kill count: " + killCounter, 15, 266);
		//g.drawString("Enter String here", 15, 282);
	}

}