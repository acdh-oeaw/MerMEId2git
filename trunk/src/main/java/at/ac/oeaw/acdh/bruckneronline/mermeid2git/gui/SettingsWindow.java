package at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui;

import static at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.GuiHelpers.createJButton;
import static at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.GuiHelpers.createJCheckBox;
import static at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.GuiHelpers.createJLabel;
import static at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.GuiHelpers.createJTextField;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import at.ac.oeaw.acdh.bruckneronline.mermeid2git.App;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.SettingsXml;

/**
 * 
 * @author mcupak
 *
 */
public class SettingsWindow extends JDialog {
	private static final long serialVersionUID = 1L;

	public SettingsWindow(Window owner, SettingsXml settings) {
		super(owner, App.TITLE + " - Settings", ModalityType.APPLICATION_MODAL);
		
		JPanel jpMain = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints(
				0,	// gridx
				0, 	// gridy
				1,	// gridwidth
				1,	// gridheight 
				0,	// weightx
				0,	// weighty
				GridBagConstraints.BASELINE,	// anchor
				GridBagConstraints.BOTH,	// fill
				new Insets(2, 2, 2, 2),	// insets
				0,	// ipadx
				0);	// ipady

		jpMain.add(createJLabel("DB URI"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("DB User"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("DB Password"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("DB MerMEId collection"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("DB use SSL"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("Git repository directory"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("Git User"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("Git Password"), gbc); gbc.gridy++;
		jpMain.add(createJLabel("Git Committers"), gbc); gbc.gridy++;
		
		JButton jbApply = createJButton("Apply", 'A');
		jpMain.add(jbApply, gbc);
		
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1;
		
		JTextField jtfDbUri = createJTextField(settings.get("db.uri"));
		jpMain.add(jtfDbUri, gbc);
		gbc.gridy++;

		JTextField jtfDbUser = createJTextField(settings.get("db.user"));
		jpMain.add(jtfDbUser, gbc);
		gbc.gridy++;
		
		JTextField jtfDbPassword = createJTextField(settings.get("db.password"));
		jpMain.add(jtfDbPassword, gbc);
		gbc.gridy++;
		
		JTextField jtfDbCollection = createJTextField(settings.get("db.collection"));
		jpMain.add(jtfDbCollection, gbc);
		gbc.gridy++;
		
		JCheckBox jtfDbSsl = createJCheckBox(null, settings.getBool("db.ssl"));
		jpMain.add(jtfDbSsl, gbc);
		gbc.gridy++;
		
		JTextField jtfGitRepoLocalDir = createJTextField(settings.get("git.repo.localdir"));
		jpMain.add(jtfGitRepoLocalDir, gbc);
		gbc.gridy++;
		// TODO : filechooser
		
		JTextField jtfGitUser = createJTextField(settings.get("git.user"));
		jpMain.add(jtfGitUser, gbc);
		gbc.gridy++;
		
		JTextField jtfGitPassword = createJTextField(settings.get("git.password"));
		jpMain.add(jtfGitPassword, gbc);
		gbc.gridy++;
		
//		JTextField jtfGitCommiters = createJTextField(settings.get("git.committers"));
//		jpMain.add(jtfGitCommiters, gbc);
		
		JTextArea jtaGitCommiters = new JTextArea(settings.get("git.committers"));
		JScrollPane jspGitCommiters = new JScrollPane(jtaGitCommiters);
		gbc.weighty = 1;
		jpMain.add(jspGitCommiters, gbc);
		
		jbApply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.set("db.user", jtfDbUser.getText());
				settings.set("db.password", jtfDbPassword.getText());
				settings.set("db.uri", jtfDbUri.getText());
				settings.set("db.collection", jtfDbCollection.getText());
				settings.setBool("db.ssl", jtfDbSsl.isSelected());
				settings.setFile("git.repo.localdir", new File(jtfGitRepoLocalDir.getText()));
				settings.set("git.user", jtfGitUser.getText());
				settings.set("git.password", jtfGitPassword.getText());
				settings.set("git.committers", jtaGitCommiters.getText());
			}
		});
		
		addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) {}
			@Override public void windowIconified(WindowEvent e) {}
			@Override public void windowDeiconified(WindowEvent e) {}
			@Override public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				
				Dimension size = getSize();
				settings.setInt("window.settings.size.width", size.width);
				settings.setInt("window.settings.size.height", size.height);
				
				Point location = getLocation();
				settings.setInt("window.settings.location.x", location.x);
				settings.setInt("window.settings.location.y", location.y);
			}
			
			@Override public void windowClosed(WindowEvent e) {}
			@Override public void windowActivated(WindowEvent e) {}
		});

//		setContentPane(new JScrollPane(jpMain));
		setContentPane(jpMain);
		
		Integer width = settings.getInt("window.settings.size.width"); if (width == null) width = 640;
		Integer height = settings.getInt("window.settings.size.height"); if (height == null) height = 360;
		setSize(width, height);

		Integer x = settings.getInt("window.settings.location.x");
		Integer y = settings.getInt("window.settings.location.y");
		if (x != null && y != null) {
			setLocation(x.intValue(), y.intValue());
		} else {
			setLocationRelativeTo(null);
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
}
