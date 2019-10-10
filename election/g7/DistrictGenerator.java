package election.g7;

import java.util.*;
import election.sim.*;
import java.awt.geom.*;

public class DistrictGenerator implements election.sim.DistrictGenerator {
    private double scale = 1000.0;
    private Random random;
    private int numVoters, numParties, numDistricts;
    private double eps = 1E-7;
    private List<Map<Polygon2D, List<Voter>>> polyganList = new ArrayList<>();
    private Map<Integer, Polygon2D> polygonMap = new HashMap<>();
    private Map<Integer, List<Voter>> voterMap = new HashMap<>();
    private Map<Integer, Boolean> checkMap = new HashMap<>();
    private int partyToWin = 1;

    public List<Voter> sortByXCoordinate(List<Voter>voters){
        Collections.sort(voters, new Comparator<Voter>() {
            @Override
            public int compare(Voter v1, Voter v2) {
                return Double.compare(v1.getLocation().getX(), v2.getLocation().getX());
            }
        });
        return voters;
    }

    @Override
    public List<Polygon2D> getDistricts(List<Voter> voters, int repPerDistrict, long seed) {
        numVoters = voters.size();
        numParties = voters.get(0).getPreference().size();
        List<Polygon2D> result = new ArrayList<Polygon2D>();
        numDistricts = 243 / repPerDistrict;
        double height = scale / 2.0 * Math.sqrt(3);
        int numStripes = 81;
        //Can contribute deviation
        int peopleInBlock = numVoters / numDistricts;
        int blockEachStripe =  numDistricts / numStripes;
        Collections.sort(voters, new Comparator<Voter>() {
            @Override
            public int compare(Voter v1, Voter v2) {
                return Double.compare(v2.getLocation().getY(), v1.getLocation().getY());
            }
        });
        // From top to bottom
        List<List<Voter>> votersInStripe = new ArrayList<>();
        int from = 0;
        double btm = 500*Math.sqrt(3);
        for (int i = 0; i < numStripes; i++) {
            int to = blockEachStripe*peopleInBlock*(i + 1) - 1;
            if (i == numStripes - 1) {
                blockEachStripe = numDistricts - blockEachStripe * (numStripes - 1);
                to = numVoters - 1;
            }
            while (to + 1 < numVoters && voters.get(to) == voters.get(to + 1))
                to++;
            List<Voter> voter_by_y = voters.subList(from, to + 1);
            from = to + 1;
            double top = btm;
            btm = (i == numStripes - 1) ? 0 : voter_by_y.get(voter_by_y.size() - 1).getLocation().getY() - eps;
            double preX = btm / Math.sqrt(3);
            double btmWidth = 1000 - 2*preX;
            if (i == 0) {
                Polygon2D polygon = new Polygon2D();
                polygon.append(500., 500*Math.sqrt(3));
                polygon.append(preX, btm);
                polygon.append(btmWidth + preX, btm);
                result.add(polygon);
            }
            else {
                double preX1 = top / Math.sqrt(3);
                double topWidth = 1000 - 2*preX1;
                Polygon2D polygon = new Polygon2D();
                polygon.append(preX1, top);
                polygon.append(topWidth + preX1, top);
                polygon.append(btmWidth + preX, btm);
                polygon.append(preX, btm);
                result.add(polygon);
            }
        }
        int i = 0;
        for (Map<Polygon2D, List<Voter>> map : polyganList) {
            for (Map.Entry<Polygon2D, List<Voter>> entry: map.entrySet()){
                polygonMap.put(i, entry.getKey());
                voterMap.put(i++, entry.getValue());
                checkMap.put(i, false);
            }
        }

        for (Map.Entry<Integer, Polygon2D> entry : polygonMap.entrySet()) {
            int id = entry.getKey();
            if (!checkMap.get(id) && !isSwingState(id)) {
                checkMap.put(id, true);
                Map<Integer, double[]> adjacentDistricts = getAdjacentDistricts(id);
                Polygon2D swing = entry.getValue();
                for (Map.Entry<Integer, double[]> adjacentDistrict : adjacentDistricts.entrySet()) {
                    int otherId = adjacentDistrict.getKey();
                    double[] edge = adjacentDistrict.getValue();
                    double x1 = edge[0], y1 = edge[1], x2 = edge[2], y2 = edge[3];
                    double len = Math.sqrt((x1 - x2)*(x1 - x2) + (y1 - y2)*(y1 - y2));
                    double width = getWidth(len);

                }
            }
        }

        System.out.println(result.size());
        return result;
    }

        private double getWidth(double len) {
            return 0.0;
        }

        //Return the index of the matrix with the a double array representting the vertex for the edge.
        // [0] x1, [1] y1, [2] x2, [3] y2
        private Map<Integer, double[]> getAdjacentDistricts(int id) {
            Map<Integer, double[]> list = new HashMap<>();
            return list;
        }

        // partyToWin is global variable now
        private boolean isSwingState(int id) {
            return false;
        }

        //Check population is valid for two polygon2 and if how beneficial it is for digging.
        private boolean isValidGerrymander(int swingId, int otherId, Polygon2D swing, Polygon2D other) {
            return false;
        }
}
