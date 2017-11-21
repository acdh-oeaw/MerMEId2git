package at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 
 * @author mcupak
 *
 */
public class GuiHelpers {

	private static final Dimension PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW = new Dimension(200, 24);
	private static final Dimension PREFERRED_AND_MINIMUM_SIZE_JTEXTFIELD = new Dimension(400, PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW.height);

	public static JLabel createJLabel(String text) {
		JLabel jl = new JLabel();
		jl.setText(text);
		jl.setHorizontalAlignment(JLabel.RIGHT);
		jl.setMinimumSize(PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW);
		jl.setPreferredSize(PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW);
		return jl;
	}
	
	public static JTextField createJTextField(String text) {
		JTextField jtf = new JTextField();
		jtf.setText(text);
		jtf.setMinimumSize(PREFERRED_AND_MINIMUM_SIZE_JTEXTFIELD);
		jtf.setPreferredSize(PREFERRED_AND_MINIMUM_SIZE_JTEXTFIELD);
		return jtf;
	}
	
	public static JCheckBox createJCheckBox(String text, boolean initalValue) {
		JCheckBox jchb = new JCheckBox();
		jchb.setText(text);
		jchb.setSelected(initalValue);
		jchb.setMinimumSize(PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW);
		jchb.setPreferredSize(PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW);
		return jchb;
	}
	
	public static JButton createJButton(String text, char mnemonic) {
		JButton jb = new JButton();
		jb.setText(text);
		jb.setMnemonic(mnemonic);
		jb.setMinimumSize(PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW);
		jb.setPreferredSize(PREFERRED_AND_MINIMUM_SIZE_SINGLE_ROW);
		return jb;
	}
	
	public static JButton createJButton(String text, char mnemonic, boolean enabled) {
		JButton jb = createJButton(text, mnemonic);
		jb.setEnabled(enabled);
		return jb;
	}
}
