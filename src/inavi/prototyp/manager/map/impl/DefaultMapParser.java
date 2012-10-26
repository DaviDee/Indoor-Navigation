package inavi.prototyp.manager.map.impl;

import inavi.map.IBuildingComponent;
import inavi.map.IFloor;
import inavi.map.IMap;
import inavi.map.ITarget;
import inavi.map.ITargetCategory;
import inavi.prototyp.map.impl.DefaultComponent;
import inavi.prototyp.map.impl.DefaultFloor;
import inavi.prototyp.map.impl.DefaultMap;
import inavi.prototyp.map.impl.DefaultTarget;
import inavi.prototyp.map.impl.DefaultTargetCategory;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class DefaultMapParser {

	private final static String MAP_NODE = "map";
	private final static String COMPONENTS_NODE = "components";
	private final static String COMPONENT_NODE = "component";
	private final static String TARGETS_NODE = "targets";
	private final static String TARGET_NODE = "target";
	private final static String NEIGHBORS_NODE = "neighbors";
	private final static String PAAR_NODE = "paar";
	private final static String FLOOR_NODE = "floor";
	private final static String FLOORS_NODE = "floors";
	private final static String CATEGORIES_NODE = "categories";
	private final static String CATEGORY_NODE = "category";
	private static final String ELEVATORS_NODE = "elevators";
	private static final Object ELEVATOR_NODE = "elevator";

	public static IMap parse(String path) {
		IMap map = null;
		try {
			File file = new File(path);
			Document doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(file);
			Element root = doc.getDocumentElement();
			if (root == null || !root.getNodeName().equals(MAP_NODE))
				return null;
			String name = root.getAttribute("name");
			String north = root.getAttribute("north");

			Map<Integer, IBuildingComponent> components = getComponents(root);
			Map<Integer, IFloor> floors = getFloors(root, file.getParent());
			Map<String, ITargetCategory> categories = getCategories(root);
			addTargets(root, components, categories);
			addNeighbors(root, components);
			addComponentsToFloors(floors, components);
			addElevators(root, floors, components);

			if (north == null || north.length() == 0)
				map = new DefaultMap(name, new LinkedList<IFloor>(floors
						.values()), new LinkedList<ITargetCategory>(categories
						.values()));
			else
				map = new DefaultMap(name, new LinkedList<IFloor>(floors
						.values()), new LinkedList<ITargetCategory>(categories
						.values()), Float.valueOf(north));
		} catch (Exception e) {
			Log.e("INavi", e.getMessage());
			e.printStackTrace();
		}
		return map;
	}

	private static void addComponentsToFloors(Map<Integer, IFloor> floors,
			Map<Integer, IBuildingComponent> components) {
		for (IBuildingComponent component : components.values()) {
			if (floors.containsKey(component.getFloorNumber()))
				floors.get(component.getFloorNumber()).getBuildingComponents()
						.add(component);
		}
	}

	private static void addNeighbors(Element root,
			Map<Integer, IBuildingComponent> components) {
		NodeList nodeList = root.getElementsByTagName(NEIGHBORS_NODE);
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		Node node = nodeList.item(0);
		nodeList = node.getChildNodes();
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		int size = nodeList.getLength();
		for (int i = 0; i < size; i++) {
			Node child = nodeList.item(i);
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (element.getTagName().equals(PAAR_NODE)) {
				int id1 = Integer.parseInt(element.getAttribute("first"));
				int id2 = Integer.parseInt(element.getAttribute("second"));
				IBuildingComponent component1 = components.get(id1);
				IBuildingComponent component2 = components.get(id2);
				if (component1 != null && component2 != null) {
					component1.getNeighbors().add(component2);
					component2.getNeighbors().add(component1);
				}
			}
		}
	}

	private static void addTargets(Element root,
			Map<Integer, IBuildingComponent> components,
			Map<String, ITargetCategory> categories) {
		NodeList nodeList = root.getElementsByTagName(TARGETS_NODE);
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		Node node = nodeList.item(0);
		nodeList = node.getChildNodes();
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		int size = nodeList.getLength();
		for (int i = 0; i < size; i++) {
			Node child = nodeList.item(i);
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (element.getTagName().equals(TARGET_NODE)) {
				String name = element.getAttribute("name");
				String category = element.getAttribute("category");
				NodeList list = element.getChildNodes();
				List<IBuildingComponent> targetComponents = new LinkedList<IBuildingComponent>();
				if (list != null && list.getLength() > 0) {
					for (int j = 0; j < size; j++) {
						if (!(list.item(j) instanceof Element)
								|| !((Element) list.item(j)).getTagName()
										.equals(COMPONENT_NODE))
							continue;
						int id = Integer.parseInt(((Element) list.item(j))
								.getAttribute("id"));
						IBuildingComponent b = components.get(id);
						if (b.getId() == id) {
							targetComponents.add(b);
						}
					}
					if (categories.containsKey(category)) {
						categories.get(category).getTargets().add(
								new DefaultTarget(name, targetComponents));
					}
				}
			}
		}
	}

	private static Map<String, ITargetCategory> getCategories(Element root) {
		NodeList nodeList = root.getElementsByTagName(CATEGORIES_NODE);
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		Node node = nodeList.item(0);
		nodeList = node.getChildNodes();
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		Map<String, ITargetCategory> categories = new HashMap<String, ITargetCategory>();
		int size = nodeList.getLength();
		for (int i = 0; i < size; i++) {
			Node child = nodeList.item(i);
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (element.getTagName().equals(CATEGORY_NODE)) {
				String name = element.getAttribute("name");
				categories.put(name, new DefaultTargetCategory(name,
						new LinkedList<ITarget>()));
			}
		}
		return categories;
	}

	private static Map<Integer, IFloor> getFloors(Element root, String dir) {
		NodeList nodeList = root.getElementsByTagName(FLOORS_NODE);
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		Node node = nodeList.item(0);
		nodeList = node.getChildNodes();
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		Map<Integer, IFloor> floors = new HashMap<Integer, IFloor>();
		int size = nodeList.getLength();
		for (int i = 0; i < size; i++) {
			Node child = nodeList.item(i);
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (element.getTagName().equals(FLOOR_NODE)) {
				int number = Integer.parseInt(element.getAttribute("number"));
				String img = element.getAttribute("img");
				floors.put(number, new DefaultFloor(number,
						new LinkedList<IBuildingComponent>(), dir + "/" + img,
						new LinkedList<IBuildingComponent>()));
			}
		}
		return floors;
	}

	private static Map<Integer, IBuildingComponent> getComponents(Element root) {
		NodeList nodeList = root.getElementsByTagName(COMPONENTS_NODE);
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		Node node = nodeList.item(0);
		nodeList = node.getChildNodes();
		if (nodeList == null || nodeList.getLength() == 0)
			return null;
		Map<Integer, IBuildingComponent> components = new HashMap<Integer, IBuildingComponent>();
		int size = nodeList.getLength();
		for (int i = 0; i < size; i++) {
			Node child = nodeList.item(i);
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (element.getTagName().equals(COMPONENT_NODE)) {
				int id = Integer.parseInt(element.getAttribute("id"));
				int floor = Integer.parseInt(element.getAttribute("floor"));
				float x = Float.parseFloat(element.getAttribute("x"));
				float y = Float.parseFloat(element.getAttribute("y"));
				components.put(id, new DefaultComponent(id, x, y, floor));
			}
		}
		return components;
	}

	private static void addElevators(Element root, Map<Integer, IFloor> floors,
			Map<Integer, IBuildingComponent> components) {
		NodeList nodeList = root.getElementsByTagName(ELEVATORS_NODE);
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		Node node = nodeList.item(0);
		nodeList = node.getChildNodes();
		if (nodeList == null || nodeList.getLength() == 0)
			return;
		int size = nodeList.getLength();
		for (int i = 0; i < size; i++) {
			Node child = nodeList.item(i);
			if (!(child instanceof Element))
				continue;
			Element element = (Element) child;
			if (element.getTagName().equals(ELEVATOR_NODE)) {
				int id = Integer.valueOf(element.getAttribute("component"));
				IBuildingComponent bc = components.get(id);
				if (bc.getId() == id) {
					if (floors.containsKey(bc.getFloorNumber())) {
						floors.get(bc.getFloorNumber()).getElevators().add(bc);
						break;
					}
				}
			}
		}
	}
}
