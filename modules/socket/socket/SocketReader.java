package socket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import anget.StandardAgent;
import anget.ambulanceteam.AT;
import anget.civilian.Civilian;
import anget.firebrigade.FB;
import anget.policeforce.PF;
import commons.Point3D;
import information.Worldinfo;
import map.GMLMap;
import map.MinecraftMap;
import map.RescueMap;
import map.parts.Building;
import map.parts.Edge;
import map.parts.Road;

public class SocketReader {

	public Map<Integer, Point3D> nodes = new HashMap<>();
	public Map<Integer, Edge> edges = new HashMap<>();
	public ArrayList<Road> roads = new ArrayList<>();
	public ArrayList<Building> buildings = new ArrayList<>();
	public HashMap<Integer, StandardAgent> agents = new HashMap<>();
	public GMLMap gmlMap;
	public RescueMap rescueMap;
	public MinecraftMap minecraftMap;

	public void readCommand(String msg) {
		String[] msgs = msg.split(",");
		switch (msgs[1]) {
		case "registry_map":
			// gmlマップ作成
			gmlMap = new GMLMap(nodes);
			gmlMap.setEdges(edges);
			gmlMap.setRoads(roads);
			gmlMap.setBuildings(buildings);

			// rescueマップ作成
			rescueMap = new RescueMap(gmlMap);
			// minecraftマップ作成
			minecraftMap = new MinecraftMap(gmlMap);

			// Worldinfoに登録
			Worldinfo.gmlMap = gmlMap;
			Worldinfo.readyGmlMap = true;

			Worldinfo.rescueMap = rescueMap;
			Worldinfo.readyRescueMap = true;

			Worldinfo.minecraftMap = minecraftMap;
			Worldinfo.readyMinecraftMap = true;

			System.out.println("マップ登録完了");
			break;

		case "orient_scenario":
			Worldinfo.agents.putAll(agents);
			Worldinfo.readyAgent = true;

			System.out.println("シナリオ登録完了");
			break;

		default:
			System.out.println(msg);
			System.out.println("Command例外受信");
			break;
		}
	}

	////////////////////////////////////////////
	public void readNode(String msg) {
		String[] msgs = msg.split(",");
		// {node, entityID, x, y, z}
		for (int i = 1; i < msgs.length; i += 4) {
			int id = Integer.parseInt(msgs[i]);
			Point3D point3d = new Point3D((int) Double.parseDouble(msgs[i + 1]), (int) Double.parseDouble(msgs[i + 2]),
					(int) Double.parseDouble(msgs[i + 3]));
			point3d.y = 3;
			nodes.put(id, point3d);
		}
	}

	public void readEdge(String msg) {
		String[] msgs = msg.split(",");
		// {edge, entityID, firstID, endID}
		for (int i = 1; i < msgs.length; i += 3) {
			int id = Integer.parseInt(msgs[i]);
			Integer[] nodes = new Integer[2];
			nodes[0] = Integer.parseInt(msgs[i + 1]);
			nodes[1] = Integer.parseInt(msgs[i + 2]);
			Edge edge = new Edge(id, nodes);
			edges.put(edge.getId(), edge);
		}
	}

	public void readRoad(String msg) {
		String[] msgs = msg.split(",");
		// {road, entityID, edgeID,・・・ }
		int id = Integer.parseInt(msgs[1]);
		ArrayList<Integer> edge_ids = new ArrayList<>();
		for (int i = 2; i < msgs.length; i++) {
			edge_ids.add(Integer.parseInt(msgs[i]));
		}
		roads.add(new Road(id, edge_ids));
	}

	public void readBuilding(String msg) {
		String[] msgs = msg.split(",");
		// {building, entityID, floor, material, edgeID,・・・ }
		int id = Integer.parseInt(msgs[1]);
		int floor = Integer.parseInt(msgs[2]);
		String material = msgs[3];
		ArrayList<Integer> edge_ids = new ArrayList<>();
		for (int i = 4; i < msgs.length; i++) {
			edge_ids.add(Integer.parseInt(msgs[i]));
		}
		buildings.add(new Building(id, floor, material, edge_ids));
	}

	////////////////////////////////////////////

	public void readScenario(String msg) {
		String[] msgs = msg.split(",");
		switch (msgs[1]) {
		case "civilian":
			System.out.println("civilian");
			// {scenario, civilian, entityID, locarionID}
			Worldinfo.agents.put(Integer.parseInt(msgs[2]),
					new Civilian(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3])));
			break;

		case "policeforce":
			System.out.println("policeforce");
			// {scenario, policeforce, entityID, locarionID}
			Worldinfo.agents.put(Integer.parseInt(msgs[2]),
					new PF(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3])));
			break;

		case "firebrigade":
			System.out.println("firebrigade");
			// {scenario, firebrigade, entityID, locarionID}
			Worldinfo.agents.put(Integer.parseInt(msgs[2]),
					new FB(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3])));
			break;

		case "ambulanceteam":
			System.out.println("ambulanceteam");
			// {scenario, ambulanceteam, entityID, locarionID}
			Worldinfo.agents.put(Integer.parseInt(msgs[2]),
					new AT(Integer.parseInt(msgs[2]), Integer.parseInt(msgs[3])));
			break;

		case "fire":
			System.out.println("fire scenario");
			break;

		default:
			System.out.println("scenario例外受信");
			break;
		}
	}

	////////////////////////////////////////////////////////////////

	public void readCivilian(String msg) {
		for (int i = 1; i < msg.split(",").length; i++) {

		}
	}

	public void readAT(String msg) {
		for (int i = 1; i < msg.split(",").length; i++) {

		}
	}

	public void readFB(String msg) {
		for (int i = 1; i < msg.split(",").length; i++) {

		}
	}

	public void readPF(String msg) {
		for (int i = 1; i < msg.split(",").length; i++) {

		}
	}

	public void readBlickade(String msg) {
		for (int i = 1; i < msg.split(",").length; i++) {

		}
	}

}
