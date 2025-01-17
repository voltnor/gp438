package novice;

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

/**
 * The Class Waypoints.
 * 
 * <p> Tools for working with collection of MPoint objects.
 * 
 * @author Colton Kohnke, Colorado School of Mines
 * @version 1.0
 * @since April 13, 2014
 */
public class Waypoints {

  /*
   * public static void main(String[] args){
   * System.out.println("GPS TEST START"); //File input = new
   * File("/home/colton/Documents/School/SrDesign/PlotTest/gps_input_test1.txt"
   * ); File input = new File("./gps_input_test1.txt"); Waypoints w = new
   * Waypoints(input); //for(int i=0; i< w._gps.size(); ++i){ //MPoint p =
   * w._gps.get(i); //System.out.println("Station: " + p.stationID+ " lat: " +
   * p.lat + " lon: " + p.lon + " x: " + p.x + " y: " + p.y + " z: " + p.z);
   * //} for(int i=0; i< w._gps.size(); ++i){ MPoint p = w._gps.get(i);
   * System.out.println("Station: " + p.stationID+ " lat: " + p.lat + " lon: "
   * + p.lon + " x: " + p.x + " y: " + p.y + " z: " + p.z); }
   * 
   * System.out.println("GPS TEST FINISH"); }
   */

  /**
   * Instantiates a new waypoints.
   */
  private Waypoints() {}

  /**
   * Degree to radian conversion.
   *
   * @param deg the degree
   * @return the double in radians
   */
  public static double degToRad(double deg) {
    return (deg / 180 * PI);
  }

  /**
   * Radians to degree.
   *
   * @param rad the radians to convert
   * @return the double in decimal degree
   */
  public static double radToDeg(double rad) {
    return (rad / PI * 180);
  }

  /**
   * Read UTM data from Tab Separated Value file (TSV).
   * Assumes data has a header and is of format:
   * station#   Easting   Northing
   *
   * @param f the TSV file to read
   * @return the array list of MPoints
   */
  public static ArrayList<MPoint> readUTMFromTSV(File f) {
    ArrayList<MPoint> _gps = new ArrayList<MPoint>(0);
    try {
      Scanner s = new Scanner(f);
      s.nextLine(); // header skip = 1
      while (s.hasNext()) {
        int stationID = s.nextInt();
        double x = s.nextDouble();
        double y = s.nextDouble();
        //double z = s.nextDouble();
        MPoint p = new MPoint(stationID, x, y);
        _gps.add(p);
      }
      s.close();
    } catch (IOException ex) {
      System.out.println(ex);
    }
    return _gps;
  }

  /**
   * Read lat/lon data from Tab Separated Value file (TSV).
   * Assumes data has a header and is of format:
   * station#   Latitude   Longitude
   * 
   * @param f the TSV file
   * @return the array list of MPoints
   */
  public static ArrayList<MPoint> readLatLonFromTSV(File f) {
    ArrayList<MPoint> _gps = new ArrayList<MPoint>(0);
    try {
      Scanner s = new Scanner(f);
      s.nextLine(); // header skip=1;
      while (s.hasNext()) {
        int stationID = s.nextInt();
        double lat = s.nextDouble();
        double lon = s.nextDouble();
        MPoint p = new MPoint(stationID, lat, lon);
        _gps.add(p);
      }
      s.close();
    } catch (IOException ex) {
      System.out.println(ex);
    }
    return _gps;
  }

  /**
   * Read lat/lon data from Comma Separated Value file (CSV).
   * Assumes data has a header and is of format:
   * station#, Latitude, Longitude
   * 
   * @param f the CSV File
   * @return the array list of MPoints
   */
  public static ArrayList<MPoint> readLatLonFromCSV(File f) {
    ArrayList<MPoint> _gps = new ArrayList<MPoint>(0);
    try {
      BufferedReader br = new BufferedReader(new FileReader((f)));
      br.readLine(); // header skip = 1
      String line = "";
      while ((line=br.readLine()) != null) {
        String[] tmp = line.split(",");
        int stationID = Integer.parseInt(tmp[0]);
        //System.out.println("Station: "+stationID);
        double lat = Double.parseDouble(tmp[1]);
        //System.out.println("lat: "+ lat);
        double lon = Double.parseDouble(tmp[2]);
        //System.out.println("lon: "+lon);
        //double z = s.nextDouble(); //handled by Ned.java
        MPoint p = new MPoint(stationID, lat, lon);
        _gps.add(p);
      }
      br.close();
    } catch (IOException ex) {
      System.out.println(ex);
    }
    return _gps;
  }
  
  /**
   * Read lat/lon from Garmin XML file.
   *
   * @param f the XML file.
   * @return the array list of MPoints.
   */
  public static ArrayList<MPoint> readLatLonFromXML(File f) {
    ArrayList<MPoint> _gps = new ArrayList<MPoint>(0);
    try {
      Scanner s = new Scanner(f);
      String current = "";
      String[] c = null;
      while (s.hasNext()) {
        while (!current.contains("lat")) {
          current = s.next();
        }
        c = current.split("\"");
        double lat = Double.parseDouble(c[1]);
        current = s.next();
        c = current.split("\"");
        double lon = Double.parseDouble(c[1]);
        c = current.split("[><]");
        double elev = Double.parseDouble(c[3]);
        int name = Integer.parseInt(c[11]);
        MPoint p = new MPoint(name, lat, lon, elev);
        _gps.add(p);
        s.next();
      }
      s.close();
    } catch (IOException ex) {
      System.out.println(ex);
    }
    return _gps;
  }

  /**
   * Converts Lat/Lon data to UTM data. Fills the Easting/Northing fields 
   * in the input ArrayList of MPoints
   *
   * @param _gps the ArrayList of MPoints to convert.
   */
  public static void latLonToUTM(ArrayList<MPoint> _gps) {
    for (int i = 0; i < _gps.size(); ++i) {
      MPoint p = _gps.get(i);
      double lat = p.getLat();
      double lon = p.getLon();
      int UTMzone = (int) (Math.floor((lon + 180.0) / 6) + 1);
      double a = 6378.137;
      double f = 1.0 / 298.257223563;
      double n = f / (2.0 - f);
      double e0 = 500.0;
      double n0 = 0;
      double k0 = 0.9996;
      double aa = a / (1.0 + n)
          * (1.0 + n * n * (1.0 / 4.0 + n * n / 64.0));
      double a1 = n * (0.5 - n * (2.0 / 3.0 - n * 5.0 / 16.0));
      double a2 = n * n * (13.0 / 48.0 - n * 3.0 / 5.0);
      double a3 = n * n * n * 61.0 / 240.0;
      double st = 2.0 * Math.sqrt(n) / (1.0 + n);
      double lon0 = -183.0 + (UTMzone * 6.0); // reference longitude for
                          // arbitrary UTM Zone
      lon -= lon0;
      lat *= PI / 180.0;
      lon *= PI / 180.0;
      double t = Math.sinh(atanh(Math.sin(lat)) - st
          * atanh(st * Math.sin(lat)));
      double ep = Math.atan(t / Math.cos(lon));
      double np = atanh(Math.sin(lon) / Math.sqrt(1.0 + t * t));
      double sx = a1 * Math.cos(2.0 * ep) * Math.sinh(2.0 * np);
      sx += a2 * Math.cos(4.0 * ep) * Math.sinh(4.0 * np);
      sx += a3 * Math.cos(6.0 * ep) * Math.sinh(6.0 * np);
      double sy = a1 * Math.sin(2.0 * ep) * Math.cosh(2.0 * np);
      sy += a2 * Math.sin(4.0 * ep) * Math.cosh(4.0 * np);
      sy += a3 * Math.sin(6.0 * ep) * Math.cosh(6.0 * np);
      double x = e0 + k0 * aa * (np + sx);
      double y = n0 + k0 * aa * (ep + sy);
      x *= 1000.0;
      y *= 1000.0;
      p.setUTMX(x);
      p.setUTMY(y);
      p.setZone(UTMzone);
    }
  }

  /**
   * Gets the minimum station number.
   *
   * @param s the ArrayList of MPoints to check
   * @return the min station number
   */
  public static int getMinStationID(ArrayList<MPoint> s){
    int station = s.get(0).getStation();
    for(MPoint tmp:s){
      if(tmp.getStation()<station){
        station = tmp.getStation();
      }
    }
    return station; 
  }

  /**
   * Read UTM Easting/Northing from Comma Seperated Value (CSV) File.
   * Assumes data has a header and is of format:
   * station#, Easting, Northing
   *
   * @param f the CSV File
   * @return the array list of MPoints from file
   */
  public static ArrayList<MPoint> readUTMFromCSV(File f) {
    ArrayList<MPoint> _gps = new ArrayList<MPoint>(0);
    try {
      Scanner s = new Scanner(f);
      s.useDelimiter(",");
      s.nextLine(); // header skip = 1
      while (s.hasNext()) {
        int stationID = s.nextInt();
        double x = s.nextDouble();
        double y = s.nextDouble();
        double z = s.nextDouble();
        MPoint p = new MPoint(stationID, x, y, z, true);
        _gps.add(p);
        s.close();
      }
    } catch (IOException ex) {
      System.out.println(ex);
    }
    return _gps;
  }

  /**
   * Export ArrayList of MPoints to CSV File.
   * File is written with a header with a format:
   * Station, Easting, Northing, Elevation
   *
   * @param _gps the MPoint ArrayList to export.
   * @param f the file to write.
   */
  public static void exportToCSV(ArrayList<MPoint> _gps, File f) {
    try {
      if (f != null) {
        BufferedWriter w = new BufferedWriter(new FileWriter(f));
        w.write("Station,Easting,Northing,Elevation");
        w.newLine();
        for (int i = 0; i < _gps.size(); ++i) {
          MPoint p = _gps.get(i);
          w.write(p.getStation() + "," + p.getUTMX() + "," + p.getUTMY() + "," + p.getElev());
          w.newLine();
        }
        w.close();
      }
    } catch (IOException ex) {
      System.out.println(ex);
    }
  }

  /**
   * Gets the maximum station number.
   *
   * @param gps the ArrayList of MPoints to check
   * @return the max station number
   */
  public static int maxStation(ArrayList<MPoint> gps){
    int max = 0;
    for(MPoint g:gps){
      if(g.getStation()>max){
        max = g.getStation();
      }
    }
    return max;
  }

  /**
   * Gets the minimum station number.
   *
   * @param gps the ArrayList of MPoints to search through.
   * @return the lowest station number.
   */
  public static int minStation(ArrayList<MPoint> gps){
    int min = gps.get(0).getStation();
    for(MPoint g:gps){
      if(g.getStation()<min){
        min = g.getStation();
      }
    }
    return min;
  }

  /**
   * Gets the MPoint with corresponding station number.
   *
   * @param gps the list of MPoint values to check for station.
   * @param station the station number to search for.
   * @return the MPoint with the correct station number (or null if not found)
   */
  public static MPoint getByStation(ArrayList<MPoint> gps, int station){
    for(MPoint p:gps){
      if(p.getStation() == station){
        return p;
      }
    }
    return null;
  }

  /**
   * Extrapolate MPoints between MPoint.
   *
   * @param _gps the ArrayList of MPoints to extrapolate.
   */
  public static void extrapolateGPS(ArrayList<MPoint> _gps) { // assumes
    Collections.sort(_gps, new MPointComp());
    int start, end, dn;
    double dx, dy, dz;
    double x, y, z;
    ArrayList<MPoint> gnew = new ArrayList<MPoint>(0);
    for (int i = 0; i < _gps.size() - 1; ++i) {
      MPoint p1 = _gps.get(i);
      MPoint p2 = _gps.get(i + 1);
      start = p1.getStation();
      end = p2.getStation();
      dx = p1.xDist(p2);
      dy = p1.yDist(p2);
      dz = p1.zDist(p2);
      dn = end - start - 1;
      for (int m = 1; m <= dn; ++m) {
        x = p1.getUTMX() + dx * m / dn;
        y = p1.getUTMY() + dy * m / dn;
        z = p1.getElev() + dz * m / dn;
        MPoint a = new MPoint(start + m, x, y, z, true);
        gnew.add(a);
      }
    }
    for (int i = 0; i < gnew.size(); ++i)
      _gps.add(gnew.get(i));
    Collections.sort(_gps, new MPointComp());
  }

  /**
   * Arc-tangent function.
   *
   * @param x the x in degrees
   * @return the result
   */
  protected static double atanh(double x) {
    return 0.5 * Math.log((1.0 + x) / (1.0 - x));
  }

}
