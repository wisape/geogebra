package geogebra.mobile.activity;

import geogebra.mobile.ClientFactory;
import geogebra.mobile.gui.TabletGUI;
import geogebra.mobile.gui.TubeSearchUI;
import geogebra.mobile.place.TabletGuiPlace;
import geogebra.mobile.place.TubeSearchPlace;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class TubeSearchActivity extends AbstractActivity
{

	// Used to obtain views, eventBus, placeController
	// Alternatively, could be injected via GIN
	private ClientFactory clientFactory;
	// Name that will be appended to "Hello,"
	private String name;

	public TubeSearchActivity(TubeSearchPlace place, ClientFactory clientFactory)
	{
		this.name = place.getHelloName();
		this.clientFactory = clientFactory;
	}

	/**
	 * Invoked by the ActivityManager to start a new Activity
	 */
	@Override
	public void start(AcceptsOneWidget containerWidget, EventBus eventBus)
	{
		TubeSearchUI gui = clientFactory.getTubeSearchUI();
		// gui.setName(name);
		// gui.setPresenter(this);
		containerWidget.setWidget(gui.asWidget());
	}

	/**
	 * Ask user before stopping this activity
	 */
	@Override
	public String mayStop()
	{
		return null;
	}

	/**
	 * Navigate to a new Place in the browser
	 */
	public void goTo(Place place)
	{
		clientFactory.getPlaceController().goTo(place);
	}
}
