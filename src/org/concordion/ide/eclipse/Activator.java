package org.concordion.ide.eclipse;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.concordion.ide.eclipse"; //$NON-NLS-1$

	// Where images are located in the plugin
	public static final String IMAGE_PATH = "icons/";

	// The shared instance
	private static Activator plugin;

	// Shared image instances
	private static Image proposalImage;
	private static Image methodProposalImage;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		disposeImages();
		super.stop(context);
	}

	/**
	 * Free memory used by shared image instances
	 */
	private void disposeImages() {
		if (proposalImage != null) {
			proposalImage.dispose();
			proposalImage = null;
		}
		if (methodProposalImage != null) {
			methodProposalImage.dispose();
			methodProposalImage = null;
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * @return A 16x16 concordion icon 
	 */
	public static Image getConcordionProposalImage() {
		if (proposalImage == null) {
			proposalImage = EclipseUtils.createImage("proposal.gif");
		}
		return proposalImage;
	}
	
	/**
	 * @return A 16x61 icon for methods (public method icon from JDT)
	 */
	public static Image getMethodProposalImage() {
		if (methodProposalImage == null) {
			methodProposalImage = EclipseUtils.createImage("methodproposal.png");
		}
		return methodProposalImage;
	}
}
