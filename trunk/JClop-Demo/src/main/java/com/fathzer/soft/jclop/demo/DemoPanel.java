package com.fathzer.soft.jclop.demo;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.fathzer.soft.ajlib.swing.Utils;
import com.fathzer.soft.ajlib.utilities.NullUtils;
import com.fathzer.soft.jclop.Service;
import com.fathzer.soft.jclop.swing.URIChooser;
import com.fathzer.soft.jclop.swing.URIChooserDialog;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class DemoPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	protected URI lastSelected = null;
	private DemoActions actions;
	protected URIChooserDialog dialog;

	private JButton readBtn;
	private JButton writeBtn;

	private JLabel selectedFile;
	private URIChooser[] choosers;
	
	private final class URISelector implements ActionListener {
		private final boolean forWriting;

		private URISelector(boolean forWriting) {
			this.forWriting = forWriting;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			getDialog().setSaveDialog(forWriting);
			getDialog().setSelectedURI(lastSelected);
			getDialog().pack();
			URI selected = getDialog().showDialog();
			if (selected!=null) {
				setSelected(selected);
			}
		}
	}

	public interface DemoActions {
		void doRead(URI uri) throws IOException;
		void doWrite(URI uri) throws IOException;
	}
	
	public DemoPanel(DemoActions actions, URIChooser[] choosers) {
		super();
		this.actions = actions;
		this.choosers = choosers;
		initialize();
	}

	public DemoPanel() {
		this(null, null);
	}
	
	private void initialize() {
		GridBagLayout gridBagLayout = new GridBagLayout();
		setLayout(gridBagLayout);
		selectedFile = new JLabel("No file selected"); 
		GridBagConstraints gbcSelectedFile = new GridBagConstraints();
		gbcSelectedFile.gridwidth = 0;
		gbcSelectedFile.insets = new Insets(0, 0, 5, 0);
		gbcSelectedFile.gridx = 0;
		gbcSelectedFile.gridy = 0;
		add(selectedFile, gbcSelectedFile);
		final JButton btnSelectForReading = new JButton("Select for reading");
		btnSelectForReading.addActionListener(new URISelector(false));
		GridBagConstraints gbcBtnSelectForReading = new GridBagConstraints();
		gbcBtnSelectForReading.weightx = 1.0;
		gbcBtnSelectForReading.insets = new Insets(0, 0, 5, 5);
		gbcBtnSelectForReading.gridx = 0;
		gbcBtnSelectForReading.gridy = 1;
		add(btnSelectForReading, gbcBtnSelectForReading);
		
		JButton btnSelectForWriting = new JButton("Select for writing");
		btnSelectForWriting.addActionListener(new URISelector(true));
		GridBagConstraints gbcBtnSelectForWriting = new GridBagConstraints();
		gbcBtnSelectForWriting.weightx = 1.0;
		gbcBtnSelectForWriting.insets = new Insets(0, 0, 5, 0);
		gbcBtnSelectForWriting.gridx = 1;
		gbcBtnSelectForWriting.gridy = 1;
		add(btnSelectForWriting, gbcBtnSelectForWriting);
		GridBagConstraints gbcReadBtn = new GridBagConstraints();
		gbcReadBtn.weightx = 1.0;
		gbcReadBtn.insets = new Insets(0, 0, 0, 5);
		gbcReadBtn.gridx = 0;
		gbcReadBtn.gridy = 2;
		add(getReadBtn(), gbcReadBtn);
		GridBagConstraints gbcWriteBtn = new GridBagConstraints();
		gbcWriteBtn.weightx = 1.0;
		gbcWriteBtn.gridx = 1;
		gbcWriteBtn.gridy = 2;
		add(getWriteBtn(), gbcWriteBtn);
	}
	
	private URIChooserDialog getDialog() {
		if (dialog==null) {
			this.dialog = new URIChooserDialog(Utils.getOwnerWindow(this), "", choosers);
		}
		return dialog;
	}

	private JButton getReadBtn() {
		if (readBtn==null) {
			readBtn = new JButton("Read");
			readBtn.setEnabled(false);
			readBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						actions.doRead(lastSelected);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		return readBtn;
	}

	private JButton getWriteBtn() {
		if (writeBtn==null) {
			writeBtn = new JButton("Write");
			writeBtn.setEnabled(false);
			writeBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					try {
						actions.doWrite(lastSelected);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
		return writeBtn;
	}
	
	private void setSelected(URI uri) {
		if (!NullUtils.areEquals(uri, lastSelected)) {
			selectedFile.setText(uri==null?"":uri.toString());
			getWriteBtn().setEnabled(uri!=null);
			getReadBtn().setEnabled(uri!=null); //TODO
			URI old = lastSelected;
			lastSelected = uri;
			firePropertyChange("SELECTED_URI", old, lastSelected);
		}
	}
}
