package novice;
///////////////////////////////////////////////////////////////////////////

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

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

	private Waypoints() {}

	public static double degToRad(double deg) {
		return (deg / 180 * PI);
	}

	public static double radToDeg(double rad) {
		return (rad / PI * 180);
	}

	public static ArrayList<MPoint> readUTMFromTSV(File f) {
		ArrayList<MPoint> _gps = new ArrayList<MPoint>(0);
		try {
			Scanner s = new Scanner(f);
			s.nextLine(); // header skip = 1
			while (s.hasNext()) {
				int stationID = s.nextInt();
				double x = s.nextDouble();
				double y = s.nextDouble();
				double z = s.nextDouble();
				MPoint p = new MPoint(stationID, x, y, z);
				_gps.add(p);
			}
			s.close();
		} catch (IOException ex) {
			System.out.println(ex);
		}
		return _gps;
	}

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

	public static void UTMToLatLong() {

	}

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
				MPoint p = new MPoint(stationID, x, y, z);
				_gps.add(p);
				s.close();
			}
		} catch (IOException ex) {
			System.out.println(ex);
		}
		return _gps;
	}

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

	protected static double atanh(double x) {
		return 0.5 * Math.log((1.0 + x) / (1.0 - x));
	}

}