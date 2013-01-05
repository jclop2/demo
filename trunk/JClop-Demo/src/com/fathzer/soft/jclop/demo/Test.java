package com.fathzer.soft.jclop.demo;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
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

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.Session.AccessType;
import com.fathzer.soft.jclop.dropbox.DropboxService;
import com.fathzer.soft.jclop.dropbox.swing.DropboxURIChooser;
import com.fathzer.soft.jclop.swing.URIChooser;
import com.fathzer.soft.jclop.swing.FileChooserPanel;
import com.fathzer.soft.jclop.swing.URIChooserDialog;
import com.fathzer.soft.jclop.swing.AbstractURIChooserPanel;

import net.astesana.ajlib.swing.Utils;
import net.astesana.ajlib.swing.framework.Application;
import net.astesana.ajlib.utilities.FileUtils;
import net.astesana.ajlib.utilities.LocalizationData;

public class Test extends Application {
	private URI lastSelected = null;
	private static DropboxAPI<WebAuthSession> API;
	private URIChooser dbChooser;
	private URIChooser fileChooser;
	private DropboxService service;
	
	private Test() {
		service = new DropboxService(new File("cache"), getAPI());
	}
	
	DropboxAPI<WebAuthSession> getAPI() {
		if (API==null) {
			try {
				// For obvious reasons, your application keys and secret are not released with the source files.
				// You should edit keys.properties in order to run this demo
				ResourceBundle bundle = ResourceBundle.getBundle(Test.class.getPackage().getName()+".keys"); //$NON-NLS-1$
				String key = bundle.getString("appKey");
				String secret = bundle.getString("appSecret");
				if (key.length()==0 || secret.length()==0) throw new MissingResourceException("App key and secret not provided","","");
				boolean appAccess = bundle.containsKey("accessType")?bundle.getString("accessType").equalsIgnoreCase("DROPBOX"):false;
				API = new DropboxAPI<WebAuthSession>(new WebAuthSession(new AppKeyPair(key, secret), appAccess?AccessType.DROPBOX:AccessType.APP_FOLDER));
			} catch (MissingResourceException e) {
				AbstractURIChooserPanel.showError(null, "You must enter valid application keys in keys.properties file.",Locale.getDefault());
				System.exit(-1);
			}
		}
		return API;
	}

	@Override
	protected Container buildMainPanel() {
		JPanel panel = new JPanel();
		dbChooser = new DropboxURIChooser(service);
		fileChooser = new FileChooserPanel();
		JButton btn = new JButton("Open");
		panel.add(btn);
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doDialog(false);
				if ((lastSelected!=null) && lastSelected.getScheme().equalsIgnoreCase("file"))  {
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
				if ((lastSelected!=null) && lastSelected.getScheme().equalsIgnoreCase("file"))  {
					System.out.println ("trying to read file");
					try {
						File file = FileUtils.getCanonical(new File(lastSelected));
						OutputStream out = new FileOutputStream(file, true);
						System.out.println ("file opened for writing");
						out.close();
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
				dbChooser.setSelectedURI(lastSelected);
				lastSelected = (new DropboxURIChooser(service)).showOpenDialog(Utils.getOwnerWindow(btnOnly), "Open Dropbox");
				System.out.println (lastSelected);
			}
		});

		return panel;
	}

	private void doDialog(boolean save) {
		Locale.setDefault(Locale.getDefault().equals(Locale.US)?Locale.FRANCE:Locale.US);
		
		JComponent.setDefaultLocale(Locale.getDefault());
		System.out.println ("Locale is set to "+Locale.getDefault());
//		Application.LOCALIZATION = new LocalizationData(LocalizationData.DEFAULT_BUNDLE_NAME);
		((Component)fileChooser).setLocale(Locale.getDefault());
		DropboxURIChooser dropboxChooser = new DropboxURIChooser(service);
		System.out.println ("DropboxChooser is "+dropboxChooser.getLocale());
		try {
			URIChooserDialog dialog = new URIChooserDialog(getJFrame(), save?"Save":"Open", new URIChooser[]{fileChooser,dropboxChooser});
			dialog.setSaveDialog(save);
//			URI lastSelected;
			URI totolastSelected = new URI("Dropbox://20989652:p0wgsfpjc6ty73b-klm3j0m4hn2c0l1@cloud.astesana.net/Jean-Marc+Astesana/Comptes");
//			lastSelected = new URI("Dropbox://20989654:p0wgsfpjc6sf73b-klm3j0m4hn2c0l1@cloud.astesana.net/blabla/Comptes");
			dialog.setSelectedURI(lastSelected);
			lastSelected = dialog.showDialog();
			System.out.println (lastSelected);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Test().launch();
	}
}
