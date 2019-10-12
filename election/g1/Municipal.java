package election.g1;

import java.util.*;
import election.sim.*;
import java.awt.geom.*;
import javafx.util.Pair;

public class Municipal {
	
	private static final double EPSILON = 1e-8;

	private Map<Pair<Integer, Integer>, Polygon2D> map = new HashMap<>();
	private Polygon2D polygon = new Polygon2D();
	private Set<Point2D> seenPoints = new HashSet<>();
	private int numTriangles = 0;

	public Municipal(Pair<Pair<Integer, Integer>, Polygon2D> newTriangle) {
		this.map.put(newTriangle.getKey(), newTriangle.getValue());
		this.polygon = newTriangle.getValue();
		for (Point2D p : newTriangle.getValue().getPoints()) {
			this.seenPoints.add(p);
		}
		this.numTriangles++;
	}

	// assume that the new polygon is a triangle
	public boolean add(Pair<Pair<Integer, Integer>, Polygon2D> newTriangle) {
		if (map.keySet().contains(newTriangle.getKey())) return false;
		this.map.put(newTriangle.getKey(), newTriangle.getValue());
		this.updatePolygon(newTriangle.getValue());
		this.numTriangles++;
		return true;
	}

	public void updatePolygon(Polygon2D newPolygon) {
		// assume it is a neighboring triangle, 3 sides
		// System.out.println("Before: " + this.polygon.toString());
		// System.out.println("Appending: " + newPolygon.toString());
		Polygon2D updatedPolygon = new Polygon2D();
		List<Point2D> points = newPolygon.getPoints();
		List<Point2D> oldPoints = polygon.getPoints();
		List<Point2D> newPoints = new ArrayList<>();

		int pointsIndex = 0;
		int oldPointsIndex = 0;

		Point2D p0 = points.get(0);
		Point2D p1 = points.get(1);
		Point2D p2 = points.get(2);

		while(oldPointsIndex < oldPoints.size()) {
			Point2D oldPrevious = oldPoints.get((oldPointsIndex-1+oldPoints.size()) % oldPoints.size());
			Point2D oldCurrent = oldPoints.get(oldPointsIndex);
			Point2D oldNext = oldPoints.get((oldPointsIndex+1) % oldPoints.size());
			Point2D oldNextNext = oldPoints.get((oldPointsIndex+2) % oldPoints.size());
			// System.out.println("appending current: " + oldCurrent);
			// boolean success = updatedPolygon.append(oldCurrent);
			// newPoints.add(oldCurrent);
			// System.out.println("success?: " + success);
			if ((approxEquals(oldCurrent, p0) && approxEquals(oldNext, p1) || (approxEquals(oldCurrent, p1) && approxEquals(oldNext, p0)))) {
				// System.out.println("p0,p1 equal");
				if (approxEquals(oldPrevious, p2)) {
					// ; intentionally do not add, this is a middle point that will be processed later.
				} else {
					newPoints.add(oldCurrent);
					if (!approxEquals(oldNextNext, p2)) {
						newPoints.add(p2);
					}
				}	
			} else if ((approxEquals(oldCurrent, p1) && approxEquals(oldNext, p2) || (approxEquals(oldCurrent, p2) && approxEquals(oldNext, p1)))) {
				// System.out.println("p1,p2 equal");
				if (approxEquals(oldPrevious, p0)) {
					// ; intentionally do not add, this is a middle point that will be processed later.
				} else {
					newPoints.add(oldCurrent);
					if (!approxEquals(oldNextNext, p0)) {
						newPoints.add(p0);
					}
				}
			} else if ((approxEquals(oldCurrent, p2) && approxEquals(oldNext, p0) || (approxEquals(oldCurrent, p0) && approxEquals(oldNext, p2)))) {
				// System.out.println("p2,p0 equal");
				if (approxEquals(oldPrevious, p1)) {
					// ; intentionally do not add, this is a middle point that will be processed later.
				} else {
					newPoints.add(oldCurrent);
					if (!approxEquals(oldNextNext, p1)) {
						newPoints.add(p1);
					}
				}		
			} else {
				newPoints.add(oldCurrent);
			}
			oldPointsIndex++;
		}

		if (approxEquals(newPoints.get(0), newPoints.get(newPoints.size()-1))) {
			newPoints = newPoints.subList(1, newPoints.size()-1);
		}

		for (Point2D newPoint : newPoints) {
			boolean success = updatedPolygon.append(newPoint);
			if (!success) {
				throw new RuntimeException("Could not append a point to the updated polygon");
			}
		}

		validate(this.polygon, updatedPolygon, newPolygon);
		double expectedArea = this.polygon.area() + newPolygon.area();
		double actualArea = updatedPolygon.area();

		this.polygon = updatedPolygon;
		for (Point2D p : newPolygon.getPoints()) {
			this.seenPoints.add(p);
		}
		// System.out.println("After: " + this.polygon.toString());
	}

	private void validate(Polygon2D old, Polygon2D update, Polygon2D delta) {
		validateArea(old, update, delta);
		validatePoints(old, update, delta);
	}

	private void validatePoints(Polygon2D old, Polygon2D update, Polygon2D delta) {
		Set<Point2D> seenPoints = new HashSet<>();
		for(Point2D point : update.getPoints()) {
			if (seenPoints.contains(point)) {
				System.out.println("Current:");
				this.printListPoint2D(old.getPoints());
				System.out.println("Adding:");
				this.printListPoint2D(delta.getPoints());
				System.out.println("Updated:");
				this.printListPoint2D(update.getPoints());
				throw new RuntimeException("invalid update, polygon contains same point twice");
			}
			seenPoints.add(point);
		}
	}

	private void validateArea(Polygon2D old, Polygon2D update, Polygon2D delta) {
		double expectedArea = old.area() + delta.area();
		double actualArea = update.area();
		if (Math.abs(old.area() + delta.area() - update.area()) > 1e-7) {
			System.out.println("Current:");
			this.printListPoint2D(old.getPoints());
			System.out.println("Adding:");
			this.printListPoint2D(delta.getPoints());
			System.out.println("Updated:");
			this.printListPoint2D(update.getPoints());
			throw new RuntimeException("invalid update, polygon area did not increase as expected");
		}
	}

	private boolean approxEquals(Point2D p1, Point2D p2) {
		return (Math.abs(p1.getX()-p2.getX()) < EPSILON && Math.abs(p1.getY() - p2.getY()) < EPSILON);
	}

	private void printListPoint2D(List<Point2D> list) {
		for (Point2D point : list) {
			System.out.println(point);
		}
	}

	public Polygon2D getPolygon() {
		return polygon;
	}

	public boolean contains(Pair<Integer, Integer> coordinate) {
		return map.keySet().contains(coordinate);
	}

	public boolean containsAllVertices(Polygon2D newPolygon) {
		for(Point2D point: newPolygon.getPoints()) {
			if(!this.seenPoints.contains(point)) return false;
		}
		return true;
	}

	public List<Pair<Integer, Integer>> getNeighboringCoordinates() {
	List<Pair<Integer, Integer>> neighbors = new ArrayList<>();
		for (Pair<Integer, Integer> coordinate : this.map.keySet()) {
			List<Pair<Integer, Integer>> coordinateNeighbors = getNeighboringCoordinates(coordinate);
			neighbors.addAll(coordinateNeighbors);
		}
		neighbors.removeAll(this.map.keySet());
		return neighbors;
	}

	public static List<Pair<Integer, Integer>> getNeighboringCoordinates(Pair<Integer, Integer> coordinate) {
        List<Pair<Integer, Integer>> neighbors = new ArrayList<>();
        int x = coordinate.getKey();
        int y = coordinate.getValue();
        if (x % 2 == 0) {
            neighbors.add(new Pair<Integer, Integer>(x-1, y-1));
            neighbors.add(new Pair<Integer, Integer>(x-1, y));  
            neighbors.add(new Pair<Integer, Integer>(x+1, y));
        } else {
            neighbors.add(new Pair<Integer, Integer>(x+1, y+1));  
            neighbors.add(new Pair<Integer, Integer>(x+1, y));
            neighbors.add(new Pair<Integer, Integer>(x-1, y));
        }
        return neighbors;
    }

    public int getNumTriangles() {
    	return this.numTriangles;
    }

    public void print() {
    	System.out.println("Municipal:");
    	// for (Pair<Integer, Integer> coordinate : map.keySet()) {
    	// 	System.out.print(" x : " + coordinate.getKey() + " y: " + coordinate.getValue() + " ");
    	// 	System.out.println(map.get(coordinate));
    	// }
    	this.printListPoint2D(this.polygon.getPoints());
    	// System.out.println();
    }

}