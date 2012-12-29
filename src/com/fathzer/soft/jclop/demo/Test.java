package com.fathzer.soft.jclop.demo;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.JButton;
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

public class Test extends Application {
	private URI lastSelected = null;
	private static DropboxAPI<WebAuthSession> API;
	
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
				AbstractURIChooserPanel.showError(null, "You must enter valid application keys in keys.properties file.");
				System.exit(-1);
			}
		}
		return API;
	}

	@Override
	protected Container buildMainPanel() {
		JPanel panel = new JPanel();
		final DropboxService service = new DropboxService(new File("cache"), getAPI());
		final URIChooser dbChooser = new DropboxURIChooser(service);
		final URIChooser fileChooser = new FileChooserPanel();
		final JButton btn = new JButton("Open");
		panel.add(btn);
		btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URIChooserDialog dialog = new URIChooserDialog(Utils.getOwnerWindow(btn), "Open", new URIChooser[]{fileChooser,dbChooser});
				dialog.setSelectedURI(lastSelected);
				lastSelected = dialog.showDialog();
				System.out.println (lastSelected);
			}
		});
		
		final JButton btnSave = new JButton("Save");
		panel.add(btnSave);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				URIChooserDialog dialog = new URIChooserDialog(Utils.getOwnerWindow(btn), "Save", new URIChooser[]{fileChooser,dbChooser});
				dialog.setSaveDialog(true);
				dialog.setSelectedURI(lastSelected);
				lastSelected = dialog.showDialog();
				System.out.println (lastSelected);
			}
		});
		
		final JButton btnOnly = new JButton("Open Dropbox");
		panel.add(btnOnly);
		btnOnly.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dbChooser.setSelectedURI(lastSelected);
				lastSelected = (new DropboxURIChooser(service)).showOpenDialog(Utils.getOwnerWindow(btn), "Open Dropbox");
				System.out.println (lastSelected);
			}
		});

		return panel;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Test().launch();
	}
}
