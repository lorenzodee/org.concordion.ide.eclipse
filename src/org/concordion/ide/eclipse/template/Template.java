package org.concordion.ide.eclipse.template;

import java.io.InputStream;

public interface Template {

	public abstract InputStream generateToStream(String charSetName);

}