package org.geogebra.web.geogebra3D.web.main;

import org.geogebra.common.euclidian.EuclidianController;
import org.geogebra.common.factories.AwtFactory;
import org.geogebra.common.geogebra3D.input3D.Input3D;
import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.main.settings.EuclidianSettings;
import org.geogebra.common.util.debug.Log;
import org.geogebra.web.geogebra3D.web.euclidian3D.EuclidianController3DW;
import org.geogebra.web.geogebra3D.web.euclidian3D.EuclidianView3DW;
import org.geogebra.web.geogebra3D.web.euclidian3DnoWebGL.EuclidianController3DWnoWebGL;
import org.geogebra.web.geogebra3D.web.euclidian3DnoWebGL.EuclidianView3DWnoWebGL;
import org.geogebra.web.geogebra3D.web.euclidianFor3D.EuclidianControllerFor3DW;
import org.geogebra.web.geogebra3D.web.euclidianFor3D.EuclidianViewFor3DW;
import org.geogebra.web.geogebra3D.web.gui.GuiManager3DW;
import org.geogebra.web.geogebra3D.web.input3D.EuclidianControllerInput3DW;
import org.geogebra.web.geogebra3D.web.input3D.EuclidianViewInput3DW;
import org.geogebra.web.geogebra3D.web.input3D.InputZSpace3DW;
import org.geogebra.web.geogebra3D.web.input3D.ZSpaceGwt;
import org.geogebra.web.html5.Browser;
import org.geogebra.web.html5.euclidian.EuclidianPanelWAbstract;
import org.geogebra.web.html5.euclidian.EuclidianViewW;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.web.gui.GuiManagerW;
import org.geogebra.web.web.main.GDevice;

/**
 * @author mathieu
 *
 */
public class App3DW {

	static private boolean useZSpace = false;


	/**
	 * 
	 * @param this_app
	 * @return new Gui manager
	 */
	static final protected GuiManagerW newGuiManager(AppW this_app,
	        GDevice device) {
		return new GuiManager3DW(this_app, device);
	}

	/**
	 * 
	 * @param evPanel
	 * @param ec
	 * @param id
	 * @param settings
	 * @return new euclidian view
	 */
	static final public EuclidianViewW newEuclidianView(
	        EuclidianPanelWAbstract evPanel, EuclidianController ec,
			int id,
	        EuclidianSettings settings) {
		return new EuclidianViewFor3DW(evPanel, ec, id,
		        settings);
	}

	/**
	 * 
	 * @param kernel
	 * @return new euclidian controller
	 */
	static final public EuclidianController newEuclidianController(Kernel kernel) {
		return new EuclidianControllerFor3DW(kernel);

	}

	/**
	 * 
	 * @param kernel
	 *            kernel
	 * @return new controller for 3D view
	 */
	static final public EuclidianController3DW newEuclidianController3DW(
	        Kernel kernel) {
		if (Browser.supportsWebGL()) {

			useZSpace = ZSpaceGwt.zspaceIsAvailable();
			Log.debug("useZSpace: " + useZSpace);
			if (useZSpace) {
				Input3D input = new InputZSpace3DW();
				return new EuclidianControllerInput3DW(kernel, input);
			}

			return new EuclidianController3DW(kernel);
		}

		return new EuclidianController3DWnoWebGL(kernel);
	}

	/**
	 * 
	 * @param ec
	 *            controller for 3D view
	 * @return new 3D view
	 */
	static final public EuclidianView3DW newEuclidianView3DW(
	        EuclidianController3DW ec, EuclidianSettings settings) {
		if (Browser.supportsWebGL()) {
			if (useZSpace) {
				return new EuclidianViewInput3DW(ec, settings);
			}

			return new EuclidianView3DW(ec, settings);
		}

		return new EuclidianView3DWnoWebGL(ec, settings);
	}

	/**
	 * Resets the width of the Canvas converning the Width of its wrapper
	 * (splitlayoutpanel center)
	 * 
	 * @param app
	 *            application instance
	 *
	 * @param width
	 *            new width
	 * 
	 * @param height
	 *            new height
	 */
	static final public void ggwGraphicsView3DDimChanged(AppW app, int width,
			int height) {
		app.getSettings().getEuclidian(3).setPreferredSize(
				AwtFactory.getPrototype().newDimension(width, height));

		EuclidianView3DW view = (EuclidianView3DW) app.getEuclidianView3D();
		view.setCoordinateSpaceSize(width, height);
		view.doRepaint2();
		app.stopCollectingRepaints();
	}

}
