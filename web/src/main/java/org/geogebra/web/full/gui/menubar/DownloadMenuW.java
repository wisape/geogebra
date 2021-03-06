package org.geogebra.web.full.gui.menubar;

import org.geogebra.web.full.css.MaterialDesignResources;
import org.geogebra.web.html5.main.AppW;
import org.geogebra.web.resources.SVGResource;

/**
 * Help menu
 */
public class DownloadMenuW extends Submenu implements MenuBarI {
	/**
	 * @param app
	 *            application
	 */
	public DownloadMenuW(final AppW app) {
		super("DownloadAs", app);
		addExpandableStyleWithColor(false);
		ExportMenuW.initActions(this, app);
	}

	@Override
	public void hide() {
		// no hiding needed
	}

	@Override
	public SVGResource getImage() {
		return getApp().isWhiteboardActive() ? MaterialDesignResources.INSTANCE.download()
				: MaterialDesignResources.INSTANCE.file_download_black();
	}

	@Override
	protected String getTitleTranslationKey() {
		return "DownloadAs";
	}
}

