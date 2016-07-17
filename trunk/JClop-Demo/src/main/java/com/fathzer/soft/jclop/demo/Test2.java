package com.fathzer.soft.jclop.demo;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.dropbox.core.DbxAppInfo;
import com.fathzer.soft.jclop.dropbox.DropboxService;
import com.fathzer.soft.jclop.dropbox.swing.DropboxURIChooser;
import com.fathzer.soft.jclop.swing.URIChooserDialog;
import com.fathzer.soft.jclop.swing.AbstractURIChooserPanel;
import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.utilities.FileUtils;

public class Test2 extends Test {
	private URI lastSelected = null;
	private URIChooserDialog dialog;
	
	private DropboxService dbxService;

	Test2(DbxAppInfo appInfo) throws IOException {
		super(appInfo);
	}
	
	@Override
	protected Container buildMainPanel() {
		JPanel panel = new JPanel();
		dialog = new URIChooserDialog(getJFrame(), "", choosers);
		try {
			dbxService = (DropboxService) Test.getService(new URI("Dropbox://test"), choosers);
		} catch (URISyntaxException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

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
				AbstractURIChooserPanel dbChooser = new DropboxURIChooser(dbxService);
				if ((lastSelected!=null) && dbxService.getScheme().equals(lastSelected.getScheme())) {
					dbChooser.setSelectedURI(lastSelected);
				}
				lastSelected = (new DropboxURIChooser(dbxService)).showOpenDialog(Utils.getOwnerWindow(btnOnly), "Open Dropbox");
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
		DropboxURIChooser dropboxChooser = new DropboxURIChooser(dbxService);
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
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			new Test2(Test.getAppInfo()).launch();
		} catch (MissingResourceException e) {
			AbstractURIChooserPanel.showError(null, "You must enter valid application keys in keys.properties file.",Locale.getDefault());
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
