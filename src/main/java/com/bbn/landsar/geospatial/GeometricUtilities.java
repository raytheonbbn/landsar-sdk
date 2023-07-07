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

package com.bbn.landsar.geospatial;

import com.metsci.glimpse.util.vector.Vector2d;
import java.util.ArrayList;

public class GeometricUtilities {

    public static Vector2d getCirleAvoidanceWaypoint(Vector2d a, Vector2d b, Vector2d c, double r) {
        Vector2d[] aTangents = tangentPoints(a, c, r);
        Vector2d[] bTangents = tangentPoints(b, c, r);

        if(aTangents == null || bTangents == null){
            return null;
        }

        ArrayList<Vector2d> candidates = new ArrayList<>();
        for (int aIndx = 0; aIndx < 2; aIndx++) {
            Vector2d at = aTangents[aIndx];
            for (int bIndx = 0; bIndx < 2; bIndx++) {
                Vector2d bt = bTangents[bIndx];
                Vector2d candidate = getPositiveIntersection(a, at, b, bt);
                if (candidate != null) {
                    candidates.add(candidate);
                }
            }
        }

        Vector2d waypoint = candidates.get(0);
        double minR = waypoint.norm();
        for (Vector2d candidate : candidates) {
            if (candidate.distance(c) < minR) {
                minR = candidate.distance(c);
                waypoint = candidate;
            }
        }

        return waypoint;
    }

    // Returns the two tangent points to the circle for lines through a
    public static Vector2d[] tangentPoints(Vector2d a, Vector2d c, double r) {

        // If a is not outside the circle, return null
        double d = a.distance(c);

        if (d <= r) return null;

        Vector2d[] tanPoints = new Vector2d[2];

        double x = r * r / d;
        double y = Math.sqrt(r * r - x * x);
        Vector2d u = a.minus(c);
        u = u.scaledBy(1 / u.norm());
        Vector2d uPerp = new Vector2d(-u.getY(), u.getX());
        u = u.scaledBy(x);
        uPerp = uPerp.scaledBy(y);

        tanPoints[0] = c.plus(u).plus(uPerp);
        tanPoints[1] = (c.plus(u)).minus(uPerp);


        return tanPoints;
    }


    public static Vector2d getPositiveIntersection(Vector2d a, Vector2d at, Vector2d b, Vector2d bt) {

        Vector2d aDelta = at.minus(a);
        Vector2d bDelta = bt.minus(b);

        // a + t * aDelta = b + s * bDelta;
        Vector2d aDeltaPerp = new Vector2d(-aDelta.getY(), aDelta.getX());
        Vector2d bDeltaPerp = new Vector2d(-bDelta.getY(), bDelta.getX());

        double s = (a.minus(b)).dotProduct(aDeltaPerp) / bDelta.dotProduct(aDeltaPerp);
        if (s < 0) return null;
        double t = (b.minus(a)).dotProduct(bDeltaPerp) / aDelta.dotProduct(bDeltaPerp);
        if (t < 0) return null;

        return a.plus(aDelta.scaledBy(t));
    }

}
