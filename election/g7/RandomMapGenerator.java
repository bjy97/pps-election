package election.g7;

import java.awt.geom.*;
import java.io.*;
import java.util.*;
import election.sim.*;

public class RandomMapGenerator implements election.sim.MapGenerator {
    private static double scale = 1000.0;
    private int city = 10;

    @Override
    public List<Voter> getVoters(int numVoters, int numParties, long seed) {
        List<Voter> ret = new ArrayList<Voter>();
        Random random = new Random(seed);

        Path2D triangle = new Path2D.Double();
        triangle.moveTo(0., 0.);
        triangle.lineTo(1000., 0.);
        triangle.lineTo(500., 500. * Math.sqrt(3));
        triangle.closePath();
        Double[][] preference = null;
        Double[][] population = null;
        try {
            String path = new File("").getAbsolutePath();
            preference = readCSVFile(path + "/election/g7/preference.csv");
        } catch (Exception e) {
            String path = new File("").getAbsolutePath();
            System.out.println("Cannot read csv file in " + path);
        }
        int row = preference.length;
        int col = preference[0].length;
        System.out.println("row: " + row + ", col: "+ col);

        for (int i = 0; i < city; i++) {
            double x,y;
            do {
                x = random.nextDouble() * 1000.0;
                y = random.nextDouble() * 900.0;
            } while (!triangle.contains(x, y));
            citys.add(new double[] {x, y});
        }

        for (int i = 0; i < numVoters; ++ i) {
            double x, y;
            int cityId = random.nextInt(city - 1);
            double cityX = citys.get(cityId)[0];
            double cityY = citys.get(cityId)[1];
            do {
                double distance = 1 / random.nextDouble();
                double angle = -Math.PI/2 + Math.PI*random.nextDouble();
                x = cityX + Math.cos(angle)*distance;
                y = cityY + Math.sin(angle)*distance;
            } while (!triangle.contains(x, y));
            List<Double> pref = new ArrayList<Double>();
            for (int j = 0; j < numParties; ++ j)
                pref.add(random.nextDouble());
            ret.add(new Voter(new Point2D.Double(x, y), pref));
        }

        return ret;
    }


    private Double[][] readCSVFile(String csvFileName) throws IOException {

        String line = null;
        BufferedReader stream = null;
        List<List<Double>> csvData = new ArrayList<List<Double>>();

        try {
            stream = new BufferedReader(new FileReader(csvFileName));
            while ((line = stream.readLine()) != null) {
                String[] cells = line.split(",");
                List<Double> dataLine = new ArrayList<Double>(cells.length);
                for (String data : cells)
                    dataLine.add(Double.valueOf(data));
                csvData.add(dataLine);
            }
        } finally {
            if (stream != null)
                stream.close();
        }

        Double[][] mapData = new Double[csvData.size()][csvData.get(0).size()];
        int i = 0;

        for (List<Double> dataLine : csvData) {
            mapData[i++] = dataLine.toArray(new Double[dataLine.size()]);
        }
        return mapData;
    }


}
