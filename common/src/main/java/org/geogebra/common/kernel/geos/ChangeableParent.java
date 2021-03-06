package org.geogebra.common.kernel.geos;

import java.util.ArrayList;

import org.geogebra.common.euclidian.EuclidianView;
import org.geogebra.common.geogebra3D.euclidian3D.EuclidianView3D;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.Matrix.Coords;
import org.geogebra.common.kernel.arithmetic.MyDouble;
import org.geogebra.common.kernel.arithmetic.NumberValue;
import org.geogebra.common.kernel.kernelND.GeoPointND;
import org.geogebra.common.kernel.kernelND.GeoPolyhedronInterface;
import org.geogebra.common.kernel.kernelND.GeoSegmentND;
import org.geogebra.common.main.Feature;
import org.geogebra.common.plugin.EuclidianStyleConstants;
import org.geogebra.common.util.DoubleUtil;

/**
 * Parent (number+direction) for changing prism, cylinder, etc.
 * 
 * @author Mathieu
 *
 */
public class ChangeableParent {

	private GeoNumeric changeableNumber = null;
	private GeoElement directorGeo = null;
	private double startValue;
	private Coords direction;
	private Coords direction2;
	private Coords centroid;
	private boolean forPolyhedronNet = false;
	private GeoPolyhedronInterface parent;
	private double lengthDirection;

	/**
	 * 
	 * @param v
	 *            value
	 * @return v as GeoNumeric if instance of and independent (return null
	 *         otherwise)
	 */
	static public GeoNumeric getGeoNumeric(NumberValue v) {

		if (v instanceof GeoNumeric) {
			GeoNumeric geo = (GeoNumeric) v;
			if (geo.isIndependent()) {
				return geo;
			}
		}
		return null;
	}

	/**
	 * set changeable parent to the polygon as part of polyhedron net (check
	 * first if num is not null)
	 * 
	 * @param polygon
	 *            polyhedron net face
	 * @param num
	 *            value that fold/unfold the net
	 * @param polyhedron
	 *            polyhedron parent
	 */
	static public void setPolyhedronNet(GeoPolygon polygon, GeoNumeric num,
			GeoPolyhedronInterface polyhedron) {
		if (num != null) {
			ChangeableParent cp = new ChangeableParent(polygon, num,
					polyhedron);
			polygon.setChangeableParent(cp);

			// set segments (if not already done)
			for (GeoSegmentND segment : polygon.getSegments()) {
				segment.setChangeableParentIfNull(cp);
			}

			// set points (if not already done)
			for (GeoPointND point : polygon.getPointsND()) {
				point.setChangeableParentIfNull(cp);
			}
		}
	}

	/**
	 * constructor
	 * 
	 * @param number
	 *            number
	 * @param director
	 *            director
	 */
	public ChangeableParent(GeoNumeric number, GeoElement director) {
		changeableNumber = number;
		directorGeo = director;
		forPolyhedronNet = false;
	}

	/**
	 * constructor
	 * 
	 * @param child
	 *            child
	 * @param number
	 *            number
	 * @param parent
	 *            parent polyhedron
	 */
	public ChangeableParent(GeoElement child, GeoNumeric number,
			GeoPolyhedronInterface parent) {
		changeableNumber = number;
		directorGeo = child;
		forPolyhedronNet = true;
		this.parent = parent;
	}

	/**
	 * 
	 * @return number
	 */
	final public GeoNumeric getNumber() {
		return changeableNumber;
	}

	/**
	 * 
	 * @return value of the number
	 */
	final public double getValue() {
		return changeableNumber.getValue();
	}

	/**
	 * 
	 * @return director
	 */
	final public GeoElement getDirector() {
		return directorGeo;
	}

	/**
	 * record number value and direction
	 * 
	 * @param view
	 *            view calling
	 */
	final public void record(EuclidianView view, Coords startPoint) {
		startValue = getValue();
		if (direction == null) {
			direction = new Coords(3);
		}
		if (forPolyhedronNet) {
			if (view instanceof EuclidianView3D) {
				if (centroid == null) {
					centroid = new Coords(3);
				}
				parent.pseudoCentroid(centroid);
				if (view.getApplication().has(Feature.G3D_AR_EXTRUSION_TOOL)) {
					direction.setSub3(startPoint, centroid);
					lengthDirection = direction.calcNorm();
					direction.normalize();
				} else {
					direction.setSub3(((EuclidianView3D) view).getCursor3D()
							.getInhomCoordsInD3(), centroid);
				}
			} else {
				direction.set(0, 0, 0);
			}
		} else {
			direction.set3(directorGeo.getMainDirection());
		}
	}

	/**
	 * 
	 * @return start value
	 */
	final public double getStartValue() {
		return startValue;
	}

	/**
	 * @param rwTransVec
	 *            real world translation vector
	 * @param endPosition
	 *            end position
	 * @param viewDirection
	 *            view direction
	 * @param updateGeos
	 *            list of geos
	 * @param tempMoveObjectList
	 *            temporary list
	 * @param view
	 *            view where the move occurs (if not keyboard)
	 * @return true on success
	 */
	final public boolean move(Coords rwTransVec, Coords endPosition,
			Coords viewDirection, ArrayList<GeoElement> updateGeos,
			ArrayList<GeoElement> tempMoveObjectList, EuclidianView view) {

		GeoNumeric var = getNumber();

		if (var == null) {
			return false;
		}

		if (endPosition == null) { // comes from arrows keys -- all is added
			var.setValue(var.getValue() + rwTransVec.getX() + rwTransVec.getY()
					+ rwTransVec.getZ());
			GeoElement.addParentToUpdateList(var,
					updateGeos, tempMoveObjectList);
			return true;
		}

		if (viewDirection == null) { // may come from 2D view, e.g.
										// EuclidianController.moveDependent()
			// see
			// https://play.google.com/apps/publish/?dev_acc=05873811091523087820#ErrorClusterDetailsPlace:p=org.geogebra.android&et=CRASH&sh=false&lr=LAST_7_DAYS&ecn=java.lang.NullPointerException:+Attempt+to+invoke+virtual+method+'double+org.geogebra.a.m.a.j.e(org.geogebra.a.m.a.j)'+on+a+null+object+reference&tf=SourceFile&tc=%2509at+org.geogebra.common.kernel.geos.ChangeableCoordParent.move(ChangeableCoordParent.java:202)&tm=a&nid&an&c&s=new_status_desc&ed=1480452507515
			return false;
		}

		// else: comes from mouse
		double shift;
		if (changeableNumber.getConstruction().getApplication()
				.has(Feature.G3D_AR_EXTRUSION_TOOL)) {
			shift = direction.dotproduct3(rwTransVec);
			if (forPolyhedronNet) {
				shift = shift / lengthDirection;
			}
		} else {
			if (direction2 == null) {
				direction2 = new Coords(3);
			}
			direction2.setAdd3(direction, direction2.setMul(viewDirection,
					-viewDirection.dotproduct3(direction)));
			double ld = direction2.dotproduct3(direction2);

			if (DoubleUtil.isZero(ld)) {
				return false;
			}

			shift = direction2.dotproduct3(rwTransVec) / ld;
		}

		if (!MyDouble.isFinite(shift)) {
			return false;
		}
		double val = getStartValue() + shift;

		if (!forPolyhedronNet) {
			switch (view.getPointCapturingMode()) {
			case EuclidianStyleConstants.POINT_CAPTURING_STICKY_POINTS:
				// TODO
				break;
			default:
			case EuclidianStyleConstants.POINT_CAPTURING_AUTOMATIC:
				if (!view.isGridOrAxesShown()) {
					break;
				}
			case EuclidianStyleConstants.POINT_CAPTURING_ON:
			case EuclidianStyleConstants.POINT_CAPTURING_ON_GRID:
				double g = view.getGridDistances(0);
				double valRound = Kernel.roundToScale(val, g);
				if (view.getPointCapturingMode() == EuclidianStyleConstants.POINT_CAPTURING_ON_GRID
						|| (Math.abs(valRound - val) < g
								* view.getEuclidianController()
										.getPointCapturingPercentage())) {
					val = valRound;
				}
				break;
			}
		}

		var.setValue(val);
		GeoElement.addParentToUpdateList(var, updateGeos, tempMoveObjectList);

		return true;

	}

	/**
	 * 
	 * @return current move direction
	 */
	public Coords getDirection() {
		return direction;
	}

}
