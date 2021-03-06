package com.module.render;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import com.module.anget.StandardAgent;
import com.module.anget.ambulanceteam.AT;
import com.module.anget.civilian.Civilian;
import com.module.anget.firebrigade.FB;
import com.module.anget.policeforce.PF;
import com.module.commons.Point3D;
import com.module.information.Worldinfo;
import com.module.map.MinecraftMap;
import com.module.map.parts.Building;
import com.module.map.parts.Edge;
import com.module.map.parts.Road;

import net.minecraft.block.BlockPlanks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Render {

	public World world;
	private int build_index = 0;
	private int road_index = 0;
	private int neighbour_index = 0;
	private int spawn_agent_index = 0;

	public Render(World world) {
		this.world = world;
	}

	public void renderMap() {
		if (Worldinfo.canMinecraftMap()) {
			// Road
			if (road_index != -1) {
				if (drawRoad(road_index, world)) {
					road_index++;
				} else {
					road_index = -1;
				}
			}
			// Building
			if (build_index != -1) {
				if (drawBuildings(build_index, world)) {
					build_index++;
				} else {
					build_index = -1;
				}
			}
			if (road_index == -1 && build_index == -1) {
				Worldinfo.completeMap = true;
			}
		}
	}

	public void renderScenario() {
		// 入り口作成
		if (Worldinfo.canNeighbour()) {
			if (neighbour_index != -1) {
				if (drawNighbours(neighbour_index, world)) {
					neighbour_index++;
				} else {
					neighbour_index = -1;
				}
			}
		}
		// 初期スポーン
		if (Worldinfo.canSpawnAgent()) {
			if (spawn_agent_index != -1) {
				if (spawnAgent(spawn_agent_index, Worldinfo.minecraftMap, world)) {
					spawn_agent_index++;
				} else {
					spawn_agent_index = -1;
				}
			}
		}

		if (spawn_agent_index == -1 && neighbour_index == -1) {
			Worldinfo.completeScenario = true;
		}
	}

	public void renderStetas() {
		if (Worldinfo.canStetas()) {
			Worldinfo.completeStetas = true;
			for (Map.Entry<Integer, StandardAgent> entry : Worldinfo.getAgents().entrySet()) {
				int x = 0, z = 0;
				StandardAgent agent = entry.getValue();
				if (!agent.isHistory()) {
					Worldinfo.completeStetas = false;
					Point3D point = new Point3D(agent.popHistory(), 0, agent.popHistory());
					Point3D newPoint = Worldinfo.minecraftMap.toMinecraftPoint(point);
					x = newPoint.x - agent.getPosition().x;
					z = newPoint.z - agent.getPosition().z;
				}
				agent.getEntity().move(MoverType.SELF, x, 0, z);
			}
		} else {
			for (Map.Entry<Integer, StandardAgent> entry : Worldinfo.getAgents().entrySet()) {
				entry.getValue().getEntity().move(MoverType.SELF, 0.0, 0.0, 0.0);
			}
		}
	}
	///////////////////////////////////////////////////////////////////////////////

	private static boolean contains(int x, int y, int z, HashSet<Point3D> closed, int[] bounding_box) {

		// [0]:min_x, [1]:min_z, [2]:max_x, [3]:max_z
		boolean p_x = true;
		boolean m_x = true;
		boolean p_z = true;
		boolean m_z = true;
		int i = 0;

		if (closed.contains(new Point3D(x, y, z)))
			return false;

		while (p_x || m_x || p_z || m_z) {
			if (x + i > bounding_box[2] && x - i < bounding_box[0] && z + i > bounding_box[3]
					&& z - i < bounding_box[1])
				return false;
			i++;
			// +x方向
			if (closed.contains(new Point3D(x + i, y, z)))
				p_x = false;
			// -x方向
			if (closed.contains(new Point3D(x - i, y, z)))
				m_x = false;
			// +z方向
			if (closed.contains(new Point3D(x, y, z + i)))
				p_z = false;
			// -z方向
			if (closed.contains(new Point3D(x, y, z - i)))
				m_z = false;
		}
		return true;
	}

	private static Point3D[] serchArea(Point3D point) {

		Point3D[] targets = new Point3D[4];

		targets[0] = new Point3D(point.x, point.y, point.z + 1);
		targets[1] = new Point3D(point.x + 1, point.y, point.z);
		targets[2] = new Point3D(point.x, point.y, point.z - 1);
		targets[3] = new Point3D(point.x - 1, point.y, point.z);
		return targets;
	}

	private static HashSet<Point3D> completionArea(ArrayList<Point3D> flame) {

		HashSet<Point3D> open = new HashSet<>();
		HashSet<Point3D> closed = new HashSet<>();
		Point3D[] temp;
		int[] bounding_box = new int[4];

		// 探索済み座標格納
		for (Point3D point3d : flame) {
			closed.add(point3d);
			if (bounding_box[0] > point3d.x)
				bounding_box[0] = point3d.x;
			if (bounding_box[1] > point3d.z)
				bounding_box[1] = point3d.z;
			if (bounding_box[2] < point3d.x)
				bounding_box[2] = point3d.x;
			if (bounding_box[3] < point3d.z)
				bounding_box[3] = point3d.z;
		}
		boolean flag = false;
		// 塗りつぶし
		for (Point3D point3d : flame) {
			if (flag)
				break;
			temp = serchArea(point3d);
			for (Point3D serch : temp) {
				if (closed.contains(serch))
					continue;
				if (contains(serch.x, serch.y, serch.z, closed, bounding_box)) { // エッジ内の場合
					flag = true;
					/////// 幅優先///////////////
					Point3D pop;
					// 開始地点を格納
					open.add(serch);
					while (!open.isEmpty()) {
						if (open.size() > 5000) {
							break;
						}
						pop = open.iterator().next(); // pop
						open.remove(pop);
						closed.add(pop); // set
						Point3D[] targets = serchArea(pop);
						for (Point3D target : targets) {
							if (!closed.contains(target))
								open.add(target);
						}
					}
				}
			}
		}
		return closed;
	}

	private static ArrayList<Point3D> completionLine(Point3D start, Point3D end) {

		int nextX = (int) start.x;
		int nextY = (int) start.y;
		int nextZ = (int) start.z;
		int deltaX = (int) (end.x - start.x);
		int deltaZ = (int) (end.z - start.z);
		int stepX, stepZ;
		int fraction;
		ArrayList<Point3D> result = new ArrayList<>();

		if (deltaX < 0)
			stepX = -1;
		else
			stepX = 1;
		if (deltaZ < 0)
			stepZ = -1;
		else
			stepZ = 1;
		deltaX = Math.abs(deltaX * 2);
		deltaZ = Math.abs(deltaZ * 2);
		result.add(new Point3D(nextX, nextY, nextZ));

		if (deltaX > deltaZ) {
			fraction = deltaZ - deltaX / 2;
			while (nextX != end.x) {
				if (fraction >= 0) {
					nextZ += stepZ;
					fraction -= deltaX;
				}
				nextX += stepX;
				fraction += deltaZ;
				result.add(new Point3D(nextX, nextY, nextZ));
			}
		} else {
			fraction = deltaX - deltaZ / 2;
			while (nextZ != end.z) {
				if (fraction >= 0) {
					nextX += stepX;
					fraction -= deltaZ;
				}
				nextZ += stepZ;
				fraction += deltaX;
				result.add(new Point3D(nextX, nextY, nextZ));
			}
		}

		return result;
	}

	public void resetField(World world, MinecraftMap minecraftMap) {

		for (int i = 0; i < 5; i++) {

			for (int z = (int) (Worldinfo.minecraftMap.min.getZ()) - 5; z < (int) (Worldinfo.minecraftMap.max.getZ())
					+ 5; z++) {
				for (int x = (int) (Worldinfo.minecraftMap.min.getX())
						- 5; x < (int) (Worldinfo.minecraftMap.max.getX()) + 5; x++) {

					BlockPos pos = new BlockPos(x, i + 3, z);

					if (i == 0) {
						world.setBlockState(pos, Blocks.GRASS.getDefaultState());
					} else {
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
					}

				}
			}

		}
	}

	public boolean drawRoad(int index, World world) throws NullPointerException {

		int entityID;
		ArrayList<Point3D> edges;
		ArrayList<Point3D> flame = new ArrayList<>();
		HashSet<Point3D> area;
		MinecraftMap minecraftMap = Worldinfo.minecraftMap;
		Map<Integer, Road> roads = Worldinfo.minecraftMap.getRoads();

		if (index < roads.size()) {
			entityID = roads.keySet().toArray(new Integer[0])[index];
			Road road = minecraftMap.getRoads().get(entityID);
			for (Edge edge : road.getEdges()) {
				edges = completionLine(edge.getStartNode().getPoint(), edge.getEndNode().getPoint());
				flame.addAll(edges);
			}
			area = completionArea(flame);
			for (Point3D point : area) { // draw
				BlockPos pos = new BlockPos(point.x, point.y, -1 * point.z);
				world.setBlockState(pos, Blocks.DOUBLE_STONE_SLAB.getDefaultState());
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean drawBuildings(int index, World world) throws NullPointerException {

		int entityID;
		ArrayList<Point3D> edges = new ArrayList<>();
		ArrayList<Point3D> flame = new ArrayList<>();
		HashSet<Point3D> area = new HashSet<>();
		MinecraftMap minecraftMap = Worldinfo.minecraftMap;
		Map<Integer, Building> buildings = minecraftMap.getBuildins();

		if (index < buildings.size()) {
			entityID = buildings.keySet().toArray(new Integer[0])[index];
			Building building = minecraftMap.getBuildins().get(entityID);
			for (Edge edge : building.getEdges()) {
				edges = completionLine(edge.getStartNode().getPoint(), edge.getEndNode().getPoint());
				flame.addAll(edges);
			}
			area = completionArea(flame);
			for (int i = 0; i <= building.getFloor() * 4; i++) {
				if (i % 4 == 0) {
					for (Point3D point : area) { // draw
						BlockPos pos = new BlockPos(point.x, point.y + i, -1 * point.z);
						world.setBlockState(pos, Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT,
								BlockPlanks.EnumType.byMetadata(building.getID() % 6)));
					}
				} else {
					for (Point3D point : flame) { // draw
						BlockPos pos = new BlockPos(point.x, point.y + i, -1 * point.z);
						world.setBlockState(pos, Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT,
								BlockPlanks.EnumType.byMetadata(building.getID() % 6)));
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean drawNighbours(int index, World world) {

		ArrayList<Integer> neighbours = Worldinfo.neighbours;
		MinecraftMap minecraftMap = Worldinfo.minecraftMap;

		if (index < neighbours.size()) {
			Edge edge = minecraftMap.getEdges().get(neighbours.get(index));
			Point3D point = edge.getPosition();
			for (int x = -1; x <= 1; x++) {
				for (int y = 0; y < 2; y++) {
					for (int z = -1; z <= 1; z++) {
						BlockPos pos = new BlockPos(point.x + x, point.y + 1 + y, -1 * (point.z + z));
						world.setBlockState(pos, Blocks.AIR.getDefaultState());
					}
				}
			}
		} else {
			return false;
		}
		return true;
	}

	public boolean spawnAgent(int index, MinecraftMap minecraftMap, World world) {

		int entityID;
		Entity entity;
		Map<Integer, StandardAgent> agents = Worldinfo.getAgents();

		if (index < agents.size()) {
			entityID = agents.keySet().toArray(new Integer[0])[index];
			StandardAgent standardAgent = agents.get(entityID);
			if (standardAgent instanceof Civilian) {
				Civilian civilian = (Civilian) standardAgent;
				Point3D point = minecraftMap.getPosition(civilian.spawn_locationID);
				entity = new EntityVillager(world);
				entity.setPosition(point.x, point.y + 1, point.z);
				world.spawnEntity(entity);
				civilian.entity = entity;
				civilian.spawned = true;
				System.out.println("Civilian spawned");
			}
			if (standardAgent instanceof AT) {
				AT at = (AT) standardAgent;
				Point3D point = minecraftMap.getPosition(at.spawn_locationID);
				entity = new EntityVillager(world);
				entity.setPosition(point.x, point.y + 1, point.z);
				world.spawnEntity(entity);
				at.entity = entity;
				at.spawned = true;
				System.out.println("AT spawned");
			}
			if (standardAgent instanceof FB) {
				FB fb = (FB) standardAgent;
				Point3D point = minecraftMap.getPosition(fb.spawn_locationID);
				entity = new EntityVillager(world);
				entity.setPosition(point.x, point.y + 1, point.z);
				world.spawnEntity(entity);
				fb.entity = entity;
				fb.spawned = true;
				System.out.println("FB spawned");
			}
			if (standardAgent instanceof PF) {
				PF pf = (PF) standardAgent;
				Point3D point = minecraftMap.getPosition(pf.spawn_locationID);
				entity = new EntityVillager(world);
				entity.setPosition(point.x, point.y + 1, point.z);
				world.spawnEntity(entity);
				pf.entity = entity;
				pf.spawned = true;
				System.out.println("PF spawned");
			}
		} else {
			return false;
		}
		return true;
	}

}
