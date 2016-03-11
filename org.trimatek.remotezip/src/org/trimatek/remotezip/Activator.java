package org.trimatek.remotezip;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.trimatek.remotezip.service.RemoteZipService;
import org.trimatek.remotezip.service.impl.RemoteZipServiceImpl;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		context.registerService(RemoteZipService.class.getName(),
				new RemoteZipServiceImpl(), new Hashtable());
	}

	public void stop(BundleContext context) throws Exception {

	}

}
