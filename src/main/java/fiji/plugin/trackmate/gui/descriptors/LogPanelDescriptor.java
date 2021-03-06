package fiji.plugin.trackmate.gui.descriptors;

import java.awt.Component;

import fiji.plugin.trackmate.gui.LogPanel;

public class LogPanelDescriptor implements WizardPanelDescriptor
{

	public static final String KEY = "LogPanel";

	private final LogPanel logPanel;

	public LogPanelDescriptor( final LogPanel logPanel )
	{
		this.logPanel = logPanel;
	}

	@Override
	public Component getComponent()
	{
		return logPanel;
	}

	@Override
	public void aboutToDisplayPanel()
	{}

	@Override
	public void displayingPanel()
	{}

	@Override
	public void aboutToHidePanel()
	{}

	@Override
	public void comingBackToPanel()
	{}

	@Override
	public String getKey()
	{
		return KEY;
	}

}
