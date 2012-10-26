package inavi.prototyp.manager.route.impl;

import inavi.map.IBuildingComponent;
import inavi.map.IFloor;
import inavi.map.IMap;
import inavi.map.ITarget;
import inavi.map.Point;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class DefaultRoute {

	private class Node {
		float dist;
		IBuildingComponent component;
		Node prev;
		boolean visited;
	}

	private int maxDiff = 2;
	private boolean shortestPath = false;
	private Comparator<Node> comparator = new Comparator<Node>() {

		@Override
		public int compare(Node o1, Node o2) {
			if (o1.component.getId() == o2.component.getId())
				return 0;
			else if (o2.dist < o1.dist)
				return 1;
			else {
				return -1;
			}
		}
	};

	public List<IBuildingComponent> getShortestPath(IMap map,
			IBuildingComponent from, ITarget target) {
		return getShortestPath(map, from, target.getBuildingComponents());
	}

	public List<IBuildingComponent> getShortestPath(IMap map,
			IBuildingComponent from, List<IBuildingComponent> tos) {
		List<IBuildingComponent> result = null;
		for (IBuildingComponent to : tos) {
			List<IBuildingComponent> list = getShortestPath(map, from, to);
			if (list != null) {
				if (result == null)
					result = list;
				else if (getLength(result) > getLength(list))
					result = list;
			}
		}
		return result;
	}

	public List<IBuildingComponent> getShortestPath(IMap map,
			IBuildingComponent from, IBuildingComponent to) {
		int diff = Math.abs(from.getFloorNumber() - to.getFloorNumber());
		if (diff < maxDiff)
			return dijkstra(from, to);
		IFloor floor = map.getFloor(from.getFloorNumber());
		List<IBuildingComponent> elevators = floor.getElevators();
		if (elevators == null || elevators.isEmpty()) {
			return dijkstra(from, to);
		}
		List<IBuildingComponent> path = getShortestPath(map, from, elevators);
		path.addAll(dijkstra(path.get(path.size() - 1), to));
		return path;
	}

	public List<IBuildingComponent> dijkstra(IBuildingComponent from,
			IBuildingComponent to) {
		if (shortestPath)
			return getShortest(from, to);
		return getMinimal(from, to);
	}

	private List<IBuildingComponent> getShortest(IBuildingComponent from,
			IBuildingComponent to) {

		Map<Integer, Node> map = new LinkedHashMap<Integer, Node>();
		TreeSet<Node> notVisitedNodes = new TreeSet<Node>(comparator);

		Node node = new Node();
		node.component = from;
		node.dist = 0;
		notVisitedNodes.add(node);
		map.put(from.getId(), node);
		while (!notVisitedNodes.isEmpty()) {
			node = notVisitedNodes.first();
			notVisitedNodes.remove(node);
			// is not supported under Android 2.3
			// node = notVisitedNodes.pollFirst();
			node.visited = true;
			if (node.component.getId() == to.getId()) {
				break;
			}
			if (node.component.getNeighbors() == null)
				continue;
			for (IBuildingComponent neighbor : node.component.getNeighbors()) {
				Node newNode = map.get(neighbor.getId());
				if (newNode == null) {
					newNode = new Node();
					newNode.component = neighbor;
					newNode.dist = node.dist
							+ neighbor.getPosition().distanceTo(
									node.component.getPosition());
					;
					newNode.prev = node;
					notVisitedNodes.add(newNode);
					map.put(neighbor.getId(), newNode);
				} else if (!node.visited) {
					float dist = newNode.component.getPosition().distanceTo(
							node.component.getPosition());
					if (newNode.dist > node.dist + dist) {
						newNode.dist = node.dist + dist;
						notVisitedNodes.remove(newNode);
						notVisitedNodes.add(newNode);
					}
				}
			}
		}
		if (node.component.getId() == to.getId()) {
			List<IBuildingComponent> list = new LinkedList<IBuildingComponent>();
			list.add(node.component);
			while (node.prev != null) {
				list.add(0, node.prev.component);
				node = node.prev;
			}
			return list;
		}
		return null;
	}

	private List<IBuildingComponent> getMinimal(IBuildingComponent from,
			IBuildingComponent to) {

		Map<Integer, Node> map = new LinkedHashMap<Integer, Node>();
		TreeSet<Node> notVisitedNodes = new TreeSet<Node>(comparator);

		Node node = new Node();
		node.component = from;
		node.dist = 0;
		notVisitedNodes.add(node);
		map.put(from.getId(), node);
		while (!notVisitedNodes.isEmpty()) {
			node = notVisitedNodes.first();
			notVisitedNodes.remove(node);
			// is not supported under Android 2.3
			// node = notVisitedNodes.pollFirst();
			node.visited = true;
			if (node.component.getId() == to.getId()) {
				break;
			}
			if (node.component.getNeighbors() == null)
				continue;
			for (IBuildingComponent neighbor : node.component.getNeighbors()) {
				Node newNode = map.get(neighbor.getId());
				if (newNode == null) {
					newNode = new Node();
					newNode.component = neighbor;
					newNode.dist = node.dist + 1;
					;
					newNode.prev = node;
					notVisitedNodes.add(newNode);
					map.put(neighbor.getId(), newNode);
				} else if (!node.visited) {
					if (newNode.dist > node.dist + 1) {
						newNode.dist = node.dist + 1;
						notVisitedNodes.remove(newNode);
						notVisitedNodes.add(newNode);
					}
				}
			}
		}
		if (node.component.getId() == to.getId()) {
			List<IBuildingComponent> list = new LinkedList<IBuildingComponent>();
			list.add(node.component);
			while (node.prev != null) {
				list.add(0, node.prev.component);
				node = node.prev;
			}
			return list;
		}
		return null;
	}

	private float getLength(List<IBuildingComponent> list) {
		if (!shortestPath)
			return list.size();
		float result = 0;
		for (int i = 1; i < list.size(); i++) {
			Point p1 = list.get(i - 1).getPosition();
			Point p2 = list.get(i).getPosition();
			result += p1.distanceTo(p2);
		}
		return result;
	}

	public int getDiff() {
		return maxDiff;
	}

	public void setDiff(int diff) {
		this.maxDiff = diff;
	}

	public void setShortestPath(boolean shortestPath) {
		this.shortestPath = shortestPath;
	}

	public boolean isShortestPath() {
		return shortestPath;
	}

}