package com.fathzer.soft.jclop.demo;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxRequestConfig;
import com.fathzer.soft.jclop.JClopException;
import com.fathzer.soft.jclop.SynchronizationState;
import com.fathzer.soft.jclop.dropbox.DbxConnectionData;
import com.fathzer.soft.jclop.dropbox.DropboxService;
import com.fathzer.soft.jclop.dropbox.swing.DropboxURIChooser;
import com.fathzer.soft.jclop.swing.URIChooser;
import com.fathzer.soft.jclop.swing.FileChooserPanel;
import com.fathzer.soft.jclop.swing.URIChooserDialog;
import com.fathzer.soft.jclop.swing.AbstractURIChooserPanel;
import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.swing.framework.Application;
import com.fathzer.soft.ajlib.utilities.FileUtils;

public class Test extends Application {
	private URI lastSelected = null;
	private DropboxService service;
	private URIChooserDialog dialog;
	
	private Test(DbxAppInfo appInfo) throws IOException {
		DbxConnectionData data = new DbxConnectionData(new DbxRequestConfig("Test", "fr"), appInfo);
		service = new DropboxService(new File("cache"), data);
	}
	
	private static DbxAppInfo getAppInfo() throws MissingResourceException {
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
		JPanel panel = new JPanel();
		dialog = new URIChooserDialog(getJFrame(), "", new URIChooser[]{new FileChooserPanel(),new DropboxURIChooser(service)});

		JButton btn = new JButton("Open");
		panel.add(btn);
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDialog(false);
				if ((lastSelected!=null) && "file".equalsIgnoreCase(lastSelected.getScheme()))  {
					System.out.println ("trying to read file");
					try {
						File file = FileUtils.getCanonical(new File(lastSelected));
						BufferedReader reader = new BufferedReader(new FileReader(file));
						try {
							System.out.println ("first line="+reader.readLine());
						} finally {
							reader.close();
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		JButton btnSave = new JButton("Save");
		panel.add(btnSave);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDialog(true);
				if ((lastSelected!=null) && "file".equalsIgnoreCase(lastSelected.getScheme()))  {
					System.out.println ("trying to read file");
					try {
						File file = FileUtils.getCanonical(new File(lastSelected));
						boolean isNew = !file.exists();
						OutputStream out = new FileOutputStream(file, true);
						System.out.println ("file opened for writing");
						out.close();
						if (isNew) {
							file.delete();
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		});
		
		final JButton btnOnly = new JButton("Open Dropbox");
		panel.add(btnOnly);
		btnOnly.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractURIChooserPanel dbChooser = new DropboxURIChooser(service);
				if ((lastSelected!=null) && service.getScheme().equals(lastSelected.getScheme())) {
					dbChooser.setSelectedURI(lastSelected);
				}
				lastSelected = (new DropboxURIChooser(service)).showOpenDialog(Utils.getOwnerWindow(btnOnly), "Open Dropbox");
				System.out.println (lastSelected);
			}
		});

		return panel;
	}

	private void doDialog(boolean save) {
		dialog.setTitle(save?"Save":"Open");
		Locale.setDefault(Locale.getDefault().equals(Locale.US)?Locale.FRANCE:Locale.US);
		JComponent.setDefaultLocale(Locale.getDefault());
		System.out.println ("Locale is set to "+Locale.getDefault());
		dialog.setLocale(Locale.getDefault());
		DropboxURIChooser dropboxChooser = new DropboxURIChooser(service);
		System.out.println ("DropboxChooser is "+dropboxChooser.getLocale());
		try {
			dialog.setSaveDialog(save);
//			URI lastSelected;
			URI totolastSelected = new URI("Dropbox://20989652:p0wgsfpjc6ty73b-klm3j0m4hn2c0l1@cloud.astesana.net/Jean-Marc+Astesana/Comptes");
//			lastSelected = new URI("Dropbox://20989654:p0wgsfpjc6sf73b-klm3j0m4hn2c0l1@cloud.astesana.net/blabla/Comptes");
			dialog.setSelectedURI(lastSelected);
			dialog.pack();
			lastSelected = dialog.showDialog();
			if (lastSelected!=null) {
				System.out.println ("You selected "+lastSelected);
				try {
					if (save) {
						doWrite(lastSelected);
					} else {
						doRead(lastSelected);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void doRead(URI uri) throws IOException {
		SynchronizationState state = service.synchronize(uri, null, Locale.getDefault());
		if (SynchronizationState.REMOTE_DELETED.equals(state)) {
			System.out.println("Remote file has been deleted");
		} else if (SynchronizationState.REMOTE_DELETED.equals(state)) {
			System.out.println("There's a conflict between remote and local file");
		} else {
			File file = service.getLocalFile(uri);
			System.out.println ("Local file is stored in "+file);
		}
	}

	private void doWrite(URI uri) throws IOException {
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
		} else if (SynchronizationState.REMOTE_DELETED.equals(state)) {
			System.out.println("There's a conflict between remote and local file");
		} else {
			System.out.println ("File was sent to Dropbox");
		}
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
