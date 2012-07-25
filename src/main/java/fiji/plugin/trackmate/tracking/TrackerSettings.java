package fiji.plugin.trackmate.tracking;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

import org.jdom.Attribute;
import org.jdom.Element;

import fiji.plugin.trackmate.io.TmXmlKeys;

public interface TrackerSettings <T extends RealType<T> & NativeType<T>> {

	/**
	 * Marshall this concrete instance to a JDom element, ready for saving to XML.
	 * <p>
	 * Marshalling should be done by adding {@link Attribute}s to the given element, 
	 * and/or child {@link Element}s. In the XML file, the mother element will have the
	 * name {@link TmXmlKeys#TRACKER_SETTINGS_ELEMENT_KEY} and at least one attribute
	 * with name {@link TmXmlKeys#TRACKER_SETTINGS_CLASS_ATTRIBUTE_NAME} and 
	 * value the name of the concrete settings class, to allow for unmarshsalling.
	 * 
	 * @return  the JDom element
	 */
	public void marshall(Element element);
	
	/**
	 * Load the field values stored in the JDom element to this instance.
	 */
	public void unmarshall(Element element);
	
}