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

	public static final String IMAGE_PATH = "icons/";

	// The shared instance
	private static Activator plugin;

	private static Image proposalImage;
	private static Image methodProposalImage;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

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

	private void disposeImages() {
		if (proposalImage != null) {
			proposalImage.dispose();
			proposalImage = null;
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
	
	public static Image getConcordionProposalImage() {
		if (proposalImage == null) {
			proposalImage = EclipseUtils.createImage("proposal.gif");
		}
		return proposalImage;
	}
	
	public static Image getMethodProposalImage() {
		if (methodProposalImage == null) {
			methodProposalImage = EclipseUtils.createImage("methodproposal.png");
		}
		return methodProposalImage;
	}
}
