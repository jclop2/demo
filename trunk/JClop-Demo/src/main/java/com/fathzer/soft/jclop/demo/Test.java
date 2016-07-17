package com.fathzer.soft.jclop.demo;

import java.awt.Container;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxRequestConfig;
import com.fathzer.soft.jclop.Service;
import com.fathzer.soft.jclop.SynchronizationState;
import com.fathzer.soft.jclop.dropbox.DbxConnectionData;
import com.fathzer.soft.jclop.dropbox.DropboxService;
import com.fathzer.soft.jclop.dropbox.swing.DropboxURIChooser;
import com.fathzer.soft.jclop.swing.URIChooser;
import com.fathzer.soft.jclop.swing.FileChooserPanel;
import com.fathzer.soft.jclop.swing.AbstractURIChooserPanel;
import com.fathzer.soft.ajlib.swing.framework.Application;

public class Test extends Application implements DemoPanel.DemoActions {
	protected URIChooser[] choosers;
	
	protected Test(DbxAppInfo appInfo) throws IOException {
		DbxConnectionData data = new DbxConnectionData("Test application", new DbxRequestConfig("Test", "fr"), appInfo);
		choosers = new URIChooser[]{new FileChooserPanel(),new DropboxURIChooser(new DropboxService(new File("cache"), data))};
	}
	
	protected static DbxAppInfo getAppInfo() throws MissingResourceException {
		// For obvious reasons, your application keys and secret are not released with the source files.
		// You should edit keys.properties in order to run this demo
		ResourceBundle bundle = ResourceBundle.getBundle(Test.class.getPackage().getName()+".keys"); //$NON-NLS-1$
		String key = bundle.getString("appKey");
		String secret = bundle.getString("appSecret");
		if (key.length()==0 || secret.length()==0) {
			throw new MissingResourceException("App key and secret not provided","","");
		}
//		boolean appAccess = bundle.containsKey("accessType")?"DROPBOX".equalsIgnoreCase(bundle.getString("accessType")):false;
		return new DbxAppInfo(key, secret);
	}

	@Override
	protected Container buildMainPanel() {
		return new DemoPanel(this, choosers);
	}

	public void doRead(URI uri) throws IOException {
		Service service = getService(uri, choosers);
		SynchronizationState state = service.synchronize(uri, null, Locale.getDefault());
		if (SynchronizationState.REMOTE_DELETED.equals(state)) {
			System.out.println("Remote file has been deleted");
		} else if (SynchronizationState.CONFLICT.equals(state)) {
			System.out.println("There's a conflict between remote and local file");
		} else {
			File file = service.getLocalFile(uri);
			System.out.println ("Local file is stored in "+file);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				StringBuilder content = new StringBuilder();
				for (String line=reader.readLine(); line!=null; line=reader.readLine()) {
					if (content.length()!=0) {
						content.append('\n');
					}
					content.append(line);
					System.out.println ("Content is:\n"+content);
				}
			} finally {
				reader.close();
			}
		}
	}

	public void doWrite(URI uri) throws IOException {
		Service service = getService(uri, choosers);
		File file = service.getLocalFileForWriting(uri);
		BufferedWriter out = new BufferedWriter(new FileWriter(file));
		try {
			out.write("Hello world "+System.currentTimeMillis());
		} finally {
			out.close();
		}
		SynchronizationState state = service.synchronize(uri, null, Locale.getDefault());
		if (SynchronizationState.REMOTE_DELETED.equals(state)) {
			System.out.println("Remote file has been deleted");
		} else if (SynchronizationState.CONFLICT.equals(state)) {
			System.out.println("There's a conflict between remote and local file");
		} else {
			System.out.println ("File was sent to Dropbox");
		}
	}
	
	public static Service getService(URI uri, URIChooser[] choosers) {
		String scheme = uri.getScheme();
		for (URIChooser uriChooser : choosers) {
			if (uriChooser.getScheme().equals(scheme)) {
				return uriChooser.getService();
			}
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new Test(getAppInfo()).launch();
		} catch (MissingResourceException e) {
			AbstractURIChooserPanel.showError(null, "You must enter valid application keys in keys.properties file.",Locale.getDefault());
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
