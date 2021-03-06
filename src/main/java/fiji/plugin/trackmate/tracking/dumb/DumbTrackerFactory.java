package fiji.plugin.trackmate.tracking.dumb;

import java.util.Map;

import javax.swing.ImageIcon;

import org.jdom2.Element;
import org.scijava.plugin.Plugin;

import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.SpotCollection;
import fiji.plugin.trackmate.gui.ConfigurationPanel;
import fiji.plugin.trackmate.tracking.SpotTracker;
import fiji.plugin.trackmate.tracking.SpotTrackerFactory;

@Plugin( type = SpotTrackerFactory.class, visible = false )
public class DumbTrackerFactory implements SpotTrackerFactory
{
	public static final String KEY = "DUMB_TRACKER";

	public static final String NAME = "Dumb tracker";

	@Override
	public String getInfoText()
	{
		return null;
	}

	@Override
	public ImageIcon getIcon()
	{
		return null;
	}

	@Override
	public String getKey()
	{
		return KEY;
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public SpotTracker create( final SpotCollection spots, final Map< String, Object > settings )
	{
		return new DumbTracker( spots );
	}

	@Override
	public ConfigurationPanel getTrackerConfigurationPanel( final Model model )
	{
		return null;
	}

	@Override
	public boolean marshall( final Map< String, Object > settings, final Element element )
	{
		return false;
	}

	@Override
	public boolean unmarshall( final Element element, final Map< String, Object > settings )
	{
		return false;
	}

	@Override
	public String toString( final Map< String, Object > sm )
	{
		return null;
	}

	@Override
	public Map< String, Object > getDefaultSettings()
	{
		return null;
	}

	@Override
	public boolean checkSettingsValidity( final Map< String, Object > settings )
	{
		return false;
	}

	@Override
	public String getErrorMessage()
	{
		return null;
	}

}
