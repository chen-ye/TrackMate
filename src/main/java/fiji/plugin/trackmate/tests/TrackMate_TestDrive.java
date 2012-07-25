package fiji.plugin.trackmate.tests;

import fiji.plugin.trackmate.TrackMate_;
import ij.IJ;
import ij.ImagePlus;

import java.io.File;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.RealType;

public class TrackMate_TestDrive {
	
	public static <T extends RealType<T> & NativeType<T>> void main(String[] args) {

//		System.out.println("Java3D version: "+Install_J3D.getJava3DVersion());
		
		File file;
		if (IJ.isWindows()) {
			file = new File("E:/Users/JeanYves/Desktop/Data/FakeTracks.tif");
		} else {
			file = new File("/Users/tinevez/Desktop/Data/FakeTracks.tif");
		}
		
		ij.ImageJ.main(args);
		ImagePlus imp = IJ.openImage(file.getAbsolutePath());
		imp.show();
		
		TrackMate_<T> st = new TrackMate_<T>();
		System.out.println("Running the plugin...");
		st.run(null); // launch the GUI;
	}
}