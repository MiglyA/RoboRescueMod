package com.roborescuemod.kernel;

import java.io.InputStream;

import com.ibm.icu.impl.data.ResourceReader;
import com.roborescuemod.agent.ActionAgent;
import com.roborescuemod.agent.AgentControl;
import com.roborescuemod.communication.RescueInfo;
//import com.roborescuemod.agent.EntitySample;
import com.roborescuemod.communication.SocketClient;
import com.roborescuemod.map.BuildMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.world.World;

public class Cycles {

	private static InputStream inputStream = null;
	private static BuildMap buildMap = null;

	public static World world = null;
	public static Entity sample_mob;

	private int cycle = -1;
	private boolean start = false;
	private String Path = null;

	private SocketClient client = null;

	public Cycles(World world) {
		Cycles.world = world;
		// agentControl = new AgentControl();
		ActionAgent actionAgent = new ActionAgent();
	}

	public void calcCycles() {

		if (world == null)
			return;

		// ready build map
		if (buildMap == null) {

			if (inputStream != null) {
				buildMap = new BuildMap(Cycles.world, inputStream);
				cycle = -1;
			} else if ((this.Path = BuildCommand.Path) != null) {
				BuildCommand.Path = null;
				buildMap = new BuildMap(Cycles.world, this.Path);
				cycle = -1;
			}

			if (buildMap == null && cycle == -1)
				return;
		}

		///// cycle/////////////////////////
		if (cycle <= -1) {
			// build map
			if (buildMap.buildMap() == 0) {
				cycle = 0;
				client = new SocketClient();
				buildMap = null;
				inputStream = null;
				this.Path = null;
			}

		} else if (cycle <= 3) {

			System.out.println(cycle);

			cycle = RescueInfo.time;

			ActionAgent.update();

			/*
			 * sample_mob = new EntityCreeper(world);
			 * 
			 * sample_mob.setPosition(100, 10, 100);
			 * 
			 * world.spawnEntity(sample_mob);
			 * 
			 * cycle = 1;
			 */

		} else {

			ActionAgent.update();

			cycle = RescueInfo.time;

			/*
			 * sample_mob.setRotationYawHead(i++); sample_mob.move(MoverType.SELF, 0, 0, 0);
			 * 
			 * System.out.println(sample_mob.getRotationYawHead());
			 * System.out.println(sample_mob.getPosition());
			 */
		}
	}

}
