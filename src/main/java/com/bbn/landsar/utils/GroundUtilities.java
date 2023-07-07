/*
LandSAR Motion Model Software Development Kit
Copyright (c) 2023 Raytheon Technologies 

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
https://github.com/atapas/add-copyright.git
*/

package com.bbn.landsar.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.bbn.landsar.geospatial.ExclusionZone;
import com.metsci.glimpse.util.geo.LatLonGeo;
import com.metsci.glimpse.util.vector.Vector2d;

import static com.bbn.landsar.geospatial.GeometricUtilities.getCirleAvoidanceWaypoint;
import static com.bbn.landsar.geospatial.GeometricUtilities.tangentPoints;

/**
 * Utilities used by path-based models to determine turns and go around exclusion zones
 * TODO move to geospatial package
 *
 */
public class GroundUtilities {

	public static void main(String[] arg) {

		//		testTurnPoints();

		//		testTangentPoints();

		//		testCircleAvoidanceWaypoint();

		//		testIntersectedArc();

		//      testCircleDirectedSegmentIntesection();
		
		testMinimalCircle();
	}
	
	public static boolean exclusionZonesContainsPoint(List<ExclusionZone> exclusionZones, LatLonGeo pt) {
		if (exclusionZones == null || exclusionZones.isEmpty()) {
			return false;
		}
		for (ExclusionZone zone : exclusionZones) {
			if (zone.isInside(pt)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param a
	 * @param b
	 * @param maxTurn - in radians
	 * @param rs
	 * @return
	 */
	public static Vector2d getTurnPoint(Vector2d a, Vector2d b, double maxTurn, Random rs) {

		Vector2d a0 = new Vector2d(a.getX(), a.getY());
		Vector2d b0 = new Vector2d(b.getX(), b.getY());

		// Find midpoint of segment
		Vector2d m = Vector2d.linearCombination(0.5, a0, 0.5, b0);

		double d1 = 0.5 * a0.distance(b0);

		// tan(maxTurn) = d1 / c1;
		double c1 = d1 / Math.tan(maxTurn);

		if (rs.nextBoolean()) c1 = -c1;

		Vector2d uPerp = new Vector2d(a0.getY() - b0.getY(), b0.getX() - a0.getX());
		uPerp = new Vector2d(uPerp.getX() / uPerp.norm(), uPerp.getY() / uPerp.norm());

		Vector2d c = Vector2d.linearCombination(1, m, c1, uPerp);

		//		double r = d1 / Math.sin(maxTurn);

		Vector2d[] w = {a0.minus(c), b0.minus(c), c};
		double alpha = 2 * maxTurn;
		//		double theta = alpha * rs.nextDouble();
		// Only points in the "middle" of the arc
		double theta = alpha/4 + (alpha/2) * rs.nextDouble();  
		double[] k = new double[3];
		k[0] = Math.sin(alpha - theta) / Math.sin(alpha);
		k[1] = Math.sin(theta) / Math.sin(alpha);
		k[2] = 1;

		Vector2d t = Vector2d.linearCombination(k, w);

		return t;	
	}

	/*
	 * S is the directed segment from a0 to a1.  C is the circle centered at c with radius r.
	 * This method returns the intersection point farthest along the segment.  If the circle
	 * does not intersect the segment, it returns null.
	 */
	public static Vector2d getCircleDirectedSegmentIntersect(Vector2d a0, 
			Vector2d a1, Vector2d c, double r) {		
		/*
		 * ||(1-alpha)*a0+alpha*a1 -c||^2 = r^2.  Find the largest real root in [0,1].
		 * ||alpha*(a1-a0)+(a0-c)||^2=r^2
		 * ||a1-a0||^2*alpha^2 + 2*<a1-a0,a0-c>*alpha + ||a0-c||^2-r^2 = 0
		 */

		double A = a1.minus(a0).normSquared();
		double B = 2 * (a1.minus(a0)).dotProduct(a0.minus(c));
		double C = a0.minus(c).normSquared() - r * r;

		double dSq = B * B - 4 * A * C;
		double alpha = Double.NaN;
		if (dSq >= 0) {
			double alpha0 = (-B - Math.sqrt(dSq)) / (2 * A);
			double alpha1 = (-B + Math.sqrt(dSq)) / (2 * A);

			if ((alpha1 >= 0) && (alpha1 <= 1)) {
				alpha = alpha1;
			}
			else if ((alpha0 >= 0) && (alpha0 <= 1)) {
				alpha = alpha0;
			}
			else {
				return null;
			}
			return Vector2d.linearCombination(1- alpha, a0, alpha, a1);
		}
		return null;
	}
	
	public static double[] getMinimalCircle(List<Vector2d> pts) {
		double[] centerAndRadius = new double[3];
		
		if (pts.size() <= 0) return centerAndRadius;
		
		if (pts.size() == 2) {
			centerAndRadius[0] = (pts.get(0).getX() + pts.get(1).getX()) / 2;
			centerAndRadius[1] = (pts.get(0).getY() + pts.get(1).getY()) / 2;
			centerAndRadius[2] = pts.get(0).distance(pts.get(1)) / 2;
			return centerAndRadius;
		}
		
		double minRadius = Double.POSITIVE_INFINITY;
		Vector2d minCenter = null;
		
		// Loop over all pairs
		for (int i = 0; i < pts.size(); i++) {
			Vector2d v0 = pts.get(i);
			for (int j = i + 1; j < pts.size(); j++) {
				Vector2d v1 = pts.get(j);
				Vector2d c = v1.plus(v0).scalarProduct(0.5);
				double r = c.distance(v1) * 1.0001; // Allow for discretization
//				System.out.println(i + "\t" + j + "\t" + r + "\t" + getMaxDist(c, r, pts) +
//						"\t" + allInCircle(c, r, pts));
				if (allInCircle(c, r, pts)) {
					if (r < minRadius) {
						minRadius = r;
						minCenter = c;
					}
				}
//				System.out.println(minRadius);
			}
		}
		
		// Loop over all triples
		for (int i = 0; i < pts.size(); i++) {
			Vector2d v0 = pts.get(i);
			for (int j = i + 1; j < pts.size(); j++) {
				Vector2d v1 = pts.get(j);
				for (int k = j + 1; k < pts.size(); k++) {
					Vector2d v2 = pts.get(k);
					
					Circle circle = getCircleForPoints(v0, v1, v2);
					double r = circle.r * 1.0001;
//					System.out.println(i + "\t" + j + "\t" + k + "\t" + r + "\t" + 
//					getMaxDist(circle.center, r, pts) + "\t" + allInCircle(circle.center, r, pts));
					if (allInCircle(circle.center, r, pts)) {
//						if (circle.r < minRadius) {
						if (r < minRadius) {
							minRadius = r;
							minCenter = circle.center;
						}
					}
//					System.out.println(minRadius);
				}
			}
		}
		
		if (minCenter == null) {
			System.out.println("Problem");
			System.out.println("==============");
			for (Vector2d pt : pts) {
				System.out.println(pt.getX() + "," + pt.getY());
			}
			System.out.println("==============");
			
			double delta = 0.001;
			List<Vector2d> newPts = new ArrayList<Vector2d>();
			for (Vector2d pt : pts) {
				newPts.add(new Vector2d(pt.getX() + delta * Math.random(), 
						                pt.getY() + delta * Math.random()));
			}
			getMinimalCircle(newPts);
		}
		else {
			centerAndRadius[0] = minCenter.getX();
			centerAndRadius[1] = minCenter.getY();
			centerAndRadius[2] = minRadius;
		}
		return centerAndRadius;
	}
	
	public static Circle getCircleForPoints(Vector2d v0, Vector2d v1, Vector2d v2) {
		
		Vector2d w1 = v1.minus(v0).scalarProduct(0.5);
		Vector2d w2 = v2.minus(v0).scalarProduct(0.5);
		
		double alpha = w2.dotProduct(w2.minus(w1)) / (w1.perpendicularVector().dotProduct(w2));
		
		Vector2d c = w1.plus(w1.perpendicularVector().scalarProduct(alpha));
		double r = c.distance(w1.scalarProduct(2));
		
		c = c.plus(v0);
		return new Circle(c, r);
	}
	
	private static class Circle {
		
		public Circle(Vector2d center, double r) {
			super();
			this.center = center;
			this.r = r;
		}
		
		Vector2d center;
		double r;	
	}
	
	public static boolean allInCircle(Vector2d c, double r, List<Vector2d> pts) {	
		for (Vector2d pt : pts) {
			if (c.distance(pt) > r) return false;
		}
		return true;
	}
	
	public static double getMaxDist(Vector2d c, double r, List<Vector2d> pts) {
		double maxDist = 0;
		for (Vector2d pt : pts) maxDist = Math.max(maxDist, c.distance(pt));
		return maxDist;
	}
	
	public static double maxDistRatio(Vector2d c, double r, List<Vector2d> pts) {
		double maxDist = 0.0;
		for (Vector2d pt : pts) {
			maxDist = Math.max(maxDist, c.distance(pt));
		}
		return maxDist / r;
	}

	/*
	 * Given a circle C1 with center c1 and radius r1 and a circle C2 with 
	 * center c2 and radius r2 return a parameterization of the arc of C2
	 * contained in C1 in the form of list of points.  If there is no
	 * intersection null will be returned.
	 */
	public static List<Vector2d> getIntersectedArc(Vector2d c1, double r1, 
			Vector2d c2, double r2, int numPts) {
		/*
		 * Let u be the unit vector in the direction from c2 to c1.  Let v be an 
		 * orthogonal unit vector to u.  
		 * Let r be the distance from c1 to c2.  By the law of cosines 
		 * r1*r1 = r2*r2 + r*r - 2 *r2*r*cos(A).   The desired arc is parameterized by
		 * c2 + r2 * (cos(theta) * u + sin(theta) * v) where theta is between -A and A.
		 */
		Vector2d u = c1.minus(c2);
		double r = u.norm();
		u = u.scaledBy(1 / r);
		Vector2d v = new Vector2d(-u.getY(), u.getX());

		double cosA = (r2 * r2 + r * r - r1 * r1) / (2 * r2 * r);

		if (Math.abs(cosA) > 1) return null;

		List<Vector2d> arc = new ArrayList<Vector2d>(numPts);
		double A = Math.acos(cosA);

		double deltaT = (2 * A) / (numPts - 1);
		for (int i = 0; i < numPts; i++) {
			double theta = -A + i * deltaT;
			Vector2d w = Vector2d.linearCombination(Math.cos(theta), u, Math.sin(theta), v);
			arc.add(w.scaledBy(r2).plus(c2));
		}
		return arc;
	}

	@SuppressWarnings("unused")
	private static void testCircleDirectedSegmentIntesection() {
		
		// a0 is in the third quadrant, a1 in the first, the circle is the unit circle
		Vector2d a0 = new Vector2d(-1,-1);
		Vector2d a1 = new Vector2d(1,1);
		Vector2d c = new Vector2d(0,0);
		double r = 1;

		Random rs = new Random(1234);
		int numReps = 20;
	
		for (int i = 0; i < numReps; i++) {
			a0 = new Vector2d(-rs.nextDouble(), -rs.nextDouble());
			a1 = new Vector2d(rs.nextDouble(), rs.nextDouble());
			
			// scale and offset the problem
			Vector2d offset = new Vector2d(rs.nextDouble(), rs.nextDouble());
			double scale = 2 * rs.nextDouble();
			Vector2d v = getCircleDirectedSegmentIntersect(a0.scaledBy(scale).plus(offset), 
					a1.scaledBy(scale).plus(offset), c.scaledBy(scale).plus(offset), r * scale);
			if (v == null) {
				System.out.println((a0.norm() > 1) + "\t" + (a1.norm() > 1) + "\t" + v);
			}
			else {
				// reverse the transformation to get back to unit circle in unit square
				v = v.minus(offset).scaledBy(1 / scale);
				double angle = v.minus(a0).angleWith(a1.minus(v));
				System.out.println((a0.norm() > 1) + "\t" + (a1.norm() > 1) + "\t" + v + "\t" + angle + 
						"\t" + v.norm());
			}
		}
		/* Output should look something like this
true	false	(-0.56147, -0.82750)	1.4901161193847656E-8	1.0000000000000002
false	false	null
false	true	(0.79101, 0.61181)	0.0	1.0
true	true	(0.80350, 0.59530)	0.0	1.0000000000000002
	 */
	}

	@SuppressWarnings("unused")
	private static void testIntersectedArc() {
		Vector2d c1 = new Vector2d(1,-1);
		double r1 = 3;
		Vector2d c2 = new Vector2d(2,3);
		double r2 = 2;
		int numPts = 10;
		List<Vector2d> arc = getIntersectedArc(c1, r1, c2, r2, numPts);
		for (Vector2d v : arc) System.out.println(v.getX() + "\t" + v.getY() + "\t3");
		double delta = 0.1;
		for (double theta = 0; theta < 2 * Math.PI; theta += delta) {
			Vector2d u = new Vector2d(Math.cos(theta), Math.sin(theta));
			Vector2d d1 = c1.plus(u.scalarProduct(r1));
			Vector2d d2 = c2.plus(u.scalarProduct(r2));
			System.out.println(d1.getX() + "\t" + d1.getY() + "\t1");
			System.out.println(d2.getX() + "\t" + d2.getY() + "\t2");
		}
		/*
		 * Paste the output from this method into a Matlab array A.  Then execute the following
		 * 
		 *  C1 = A(A(:,3) == 1, :);C2 = A(A(:,3) == 2, :); Arc = A(A(:,3) == 3, :);
		 *  plot(C1(:,1),C1(:,2),C2(:,1),C2(:,2));hold on;
		 *  scatter(Arc(:,1),Arc(:,2)); daspect([1 1 1]); hold off
		 */
	}

	@SuppressWarnings("unused")
	private static void testCircleAvoidanceWaypoint() {
		Random rs = new Random(124958);

		double r = 1 + rs.nextDouble();
		Vector2d c = new Vector2d(rs.nextGaussian(), rs.nextGaussian());
		double theta = 2 * Math.PI * rs.nextDouble();
		Vector2d a = new Vector2d(Math.cos(theta), Math.sin(theta));
		a = a.scalarProduct(r + 0.5 * rs.nextDouble());
		Vector2d b = c.minus(a);
		a = a.plus(c);
		Vector2d d = (new Vector2d(rs.nextDouble(), rs.nextDouble())).scalarProduct(0.3);
		b = b.plus(d);

		Vector2d w = getCirleAvoidanceWaypoint(a, b, c, r);
		System.out.println("a = [" + a.getX() + "," + a.getY() + "];");
		System.out.println("b = [" + b.getX() + "," + b.getY() + "];");
		System.out.println("c = [" + c.getX() + "," + c.getY() + "];");
		System.out.println("w = [" + w.getX() + "," + w.getY() + "];");
		System.out.println("r = " + r + ";");

		/*
		 * The output from this should be pasted into the Matlab script below.  Running the
		 * script should produce a picture of a circle and a triangle.  Two sides of the triangle
		 * should be tangent to the circle.  The triangle should not enclose the center of the
		 * circle.
		 * 
a = [-1.407744188686088,1.5001484619463377];
b = [0.18609220038616134,-1.6888807646929422];
c = [-0.7326646083358579,-0.23148127913194286];
w = [2.5760327961256335,1.0684284408442584];
r = 1.6488181252381198;

X = [a;b;w;a];
plot(X(:,1),X(:,2));

theta = linspace(0,2 * pi, 200);
hold on;
plot(r * cos(theta) + c(1),r * sin(theta) + c(2));
daspect([1 1 1]);
scatter(c(1),c(2));
hold off;
		 */
	}

	@SuppressWarnings("unused")
	private static void testTangentPoints() {
		Vector2d a = new Vector2d(2,0);
		Vector2d c = new Vector2d(0,0);
		double r = 1;
		Vector2d[] tangentPoints = tangentPoints(a, c, r);

		for (Vector2d v : tangentPoints) {
			System.out.println(v);
		}

		Random rs = new Random(1234);
		for (int i = 0; i < 10; i++) {
			c = new Vector2d(rs.nextDouble(), rs.nextDouble());
			a = new Vector2d(rs.nextDouble() + 2, rs.nextDouble() + 3);
			r = 0.5 + 0.5 * rs.nextDouble();
			tangentPoints = tangentPoints(a, c, r);
			for (Vector2d v : tangentPoints) {
				double r1 = v.distance(c);
				System.out.println(r1 - r);
				Vector2d w = v.minus(c);
				Vector2d u = v.minus(a);
				double theta = u.angleWith(w) - (Math.PI / 2);
				System.out.println(theta);
			}
		}
	}

	@SuppressWarnings("unused")
	private static void testTurnPoints() {
		Random rs = new Random();

		Vector2d a = new Vector2d(1,0);
		Vector2d b = new Vector2d(0,1);
//		Vector2d c = getTurnPoint(a, b, Math.PI / 4, rs);
		//		System.out.println(a + " " + b + " "  + c);

		for (int i = 0; i < 5; i++) {
			a = new Vector2d(rs.nextDouble(), rs.nextDouble());
			b = new Vector2d(rs.nextGaussian(), rs.nextGaussian());
			double maxTurn = Math.toRadians(10 *(rs.nextInt(3) + 1));

			Vector2d t = getTurnPoint(a, b, maxTurn, rs);

			double turn = (t.minus(a)).angleWith(b.minus(t));

			String tag = "b";
			if (t.distance(a) < t.distance(b)) {
				tag = "a";
			}
			System.out.println(Math.toDegrees(maxTurn) + " " + Math.toDegrees(turn) + " " + tag);
		}
	}

	private static void testMinimalCircle() {
		
		List<Vector2d> pts = new ArrayList<Vector2d>();
//		pts.add(new Vector2d(0,0));
//		pts.add(new Vector2d(-86.88452146333488,0.4349468755240474));
//		pts.add(new Vector2d(-81.3339525762541,81.67398405786861));
//		pts.add(new Vector2d(-8.263113868560023,81.75852793026569));
		for (int n = 0; n < 100000; n++){
			pts = new ArrayList<Vector2d>();
			for (int i = 0; i < 4; i++) {
				pts.add(new Vector2d(Math.random(), Math.random()));
			}
		}
//		pts.add(new Vector2d(0,0));
//		pts.add(new Vector2d(2,0));
//		pts.add(new Vector2d(1,Math.sqrt(3)));
		
//		double[] centerAndRadius = getMinimalCircle(pts);
//		System.out.println("c = [" +  + centerAndRadius[0] + "," + centerAndRadius[1] + "];");
//		System.out.println("r = " + centerAndRadius[2] + ";");
//		
//		System.out.print("a = [");
//		Vector2d c = new Vector2d(centerAndRadius[0], centerAndRadius[1]);
//		for (Vector2d pt : pts) {
//			System.out.println(pt.getX() + "," + pt.getY());
//		}
//		System.out.println("];");
//		System.out.println("JavaTest;");
	}
}
