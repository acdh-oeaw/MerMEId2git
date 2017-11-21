package at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui;

import static at.ac.oeaw.acdh.bruckneronline.mermeid2git.gui.GuiHelpers.createJButton;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultCaret;

import at.ac.oeaw.acdh.bruckneronline.mermeid2git.App;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.AppCallback;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.CommitProposal;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.CommitProposalComparator;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.CommitProposalComparator.CompareBy;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.Commiter;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.ExitCode;
import at.ac.oeaw.acdh.bruckneronline.mermeid2git.SettingsXml;

/**
 * 
 * @author mcupak
 *
 */
public class MainWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private final JFrame thisWindow;
	
	public MainWindow() {
		super(App.TITLE);
		
		this.thisWindow = this;
		
		final App app = App.getInstance();
		final SettingsXml settings = app.getSettings();
				
		JPanel jpMain = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints(
				0,	// gridx
				0, 	// gridy
				4,	// gridwidth
				1,	// gridheight 
				1,	// weightx
				1,	// weighty
				GridBagConstraints.BASELINE,	// anchor
				GridBagConstraints.BOTH,	// fill
				new Insets(2, 2, 2, 2),	// insets
				0,	// ipadx
				0);	// ipady

		CommitProposalComparator comparator = new CommitProposalComparator();
		String compareByName = settings.get("window.main.tab.sort.by");
		if (compareByName != null) {
			CompareBy cb = CompareBy.valueOf(compareByName);
			if (cb != null) {
				comparator.setCompareBy(cb);
			}
		}
		Boolean sortAsc = settings.getBool("window.main.tab.sort.asc");
		if (sortAsc != null) {
			comparator.setAcsending(sortAsc.booleanValue());
		}
		
		ResourcesTable resTab = new ResourcesTable(comparator);
		JScrollPane jspResources = new JScrollPane(resTab);

		JTextArea jtaInfo = new JTextArea();
		jtaInfo.setEditable(false);
		DefaultCaret dc = (DefaultCaret) jtaInfo.getCaret();
		dc.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane jspInfo = new JScrollPane(jtaInfo);

		// log into text area too
		JTextAreaHandler log2gui = new JTextAreaHandler(jtaInfo);
		log2gui.setFormatter(app.lgr.getHandlers()[0].getFormatter());
		app.lgr.addHandler(log2gui);

		JSplitPane jslpSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, jspResources, jspInfo);
		jslpSplitPane.setResizeWeight(0.5);
		jpMain.add(jslpSplitPane, gbc);
		gbc.gridy++;

		JButton jbPullAndDownload = createJButton("Download & report changes", 'D', false);
		JButton jbReportChanges = createJButton("Report changes only", 'R', false);
		JButton jbCommitAndPush = createJButton("Commit & push", 'C', false);
		JButton jbSettings = createJButton("Settings", 't', true);
		
		ActionListener alPullAndDownload = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jbPullAndDownload.setEnabled(false);
				jbReportChanges.setEnabled(false);
				jbCommitAndPush.setEnabled(false);
				
				new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						app.checkoutAll();
						app.pull();
						app.download();
						return null;
					}
					
					@Override
					protected void done() {
						try {
							get();
							jbReportChanges.setEnabled(true);
							
						} catch (InterruptedException | ExecutionException e) {
							throw new SwingException(e);
						}
					}
				}.execute();
			}
		};
		
		ActionListener alReportChanges = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jbPullAndDownload.setEnabled(false);
				jbReportChanges.setEnabled(false);
				jbCommitAndPush.setEnabled(false);
				
				new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						resTab.model.clear();
						app.getChanges();
						return null;
					}
					@Override
					protected void done() {
						try {
							get();
							jbPullAndDownload.setEnabled(true);
							jbReportChanges.setEnabled(true);
							if (!resTab.model.isEmpty()) {
								jbCommitAndPush.setEnabled(true);
							}
							
						} catch (InterruptedException | ExecutionException e) {
							throw new SwingException(e);
						}
					}
				}.execute();
			}
		};
		
		ActionListener alCommitAndPush = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jbCommitAndPush.setEnabled(false);
				jbReportChanges.setEnabled(false);
				jbPullAndDownload.setEnabled(false);
				
				new SwingWorker<Object, Object>() {
					@Override
					protected Object doInBackground() throws Exception {
						final CommitProposal[] listSnapshot = resTab.model.rows.toArray(new CommitProposal[resTab.model.rows.size()]);	// snapshot needed to avoid concurrent modification
						resTab.model.clear();
						for (int i = 0; i < listSnapshot.length; i++) {
							app.commit(listSnapshot[i]);
						}
						app.push();
						return null;
					}
					@Override
					protected void done() {
						try {
							get();
							jbPullAndDownload.setEnabled(true);
							alReportChanges.actionPerformed(null);
							jbReportChanges.setEnabled(true);
							
						} catch (InterruptedException | ExecutionException e) {
							throw new SwingException(e);
						}
					}
				}.execute();
			}
		};
		
		jbPullAndDownload.addActionListener(alPullAndDownload);
		jbReportChanges.addActionListener(alReportChanges);
		jbCommitAndPush.addActionListener(alCommitAndPush);
		
		jbSettings.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new SettingsWindow(thisWindow, settings);
			}
		});
		
		gbc.gridwidth = 1;
		gbc.weighty = 0;
		jpMain.add(jbPullAndDownload, gbc);
		gbc.gridx++;
		
		jpMain.add(jbReportChanges, gbc);
		gbc.gridx++;
		
		jpMain.add(jbCommitAndPush, gbc);
		gbc.gridx++;
		
		jpMain.add(jbSettings, gbc);
		
		addWindowListener(new WindowListener() {
			@Override public void windowOpened(WindowEvent e) {}
			@Override public void windowIconified(WindowEvent e) {}
			@Override public void windowDeiconified(WindowEvent e) {}
			@Override public void windowDeactivated(WindowEvent e) {}
			
			@Override
			public void windowClosing(WindowEvent e) {
				
				// save window size
				Dimension size = getSize();
				settings.setInt("window.main.size.width", size.width);
				settings.setInt("window.main.size.height", size.height);
				
				// save window location
				Point location = getLocation();
				settings.setInt("window.main.location.x", location.x);
				settings.setInt("window.main.location.y", location.y);
				
				// save table column sizes
				TableColumnModel tcm = resTab.getColumnModel();
				ColumnMeta[] cols = ColumnMeta.values();
				for (int i = 0; i < cols.length; i++) {
					ColumnMeta c = cols[i];
					int width = tcm.getColumn(i).getWidth();
					settings.setInt("window.main.tab.colwidth." + c.name(), width);
				}
				
				// save window splitter location
				settings.setInt("window.split.at", jslpSplitPane.getDividerLocation());
				
				// save comparator settings
				settings.set("window.main.tab.sort.by", comparator.getCompareBy().name());
				settings.setBool("window.main.tab.sort.asc", comparator.isAcsending());
				
				app.exit(ExitCode.OK);
			}
			
			@Override public void windowClosed(WindowEvent e) {}
			@Override public void windowActivated(WindowEvent e) {}
		});

		setContentPane(jpMain);
		
		// restore window size
		Integer width = settings.getInt("window.main.size.width"); if (width == null) width = 900;
		Integer height = settings.getInt("window.main.size.height"); if (height == null) height = 700;
		setSize(width, height);

		// restore window location
		Integer x = settings.getInt("window.main.location.x");
		Integer y = settings.getInt("window.main.location.y");
		if (x != null && y != null) {
			setLocation(x.intValue(), y.intValue());
		} else {
			setLocationRelativeTo(null);
		}
		
		// restore table column sizes
		TableColumnModel tcm = resTab.getColumnModel();
		ColumnMeta[] cols = ColumnMeta.values();
		for (int i = 0; i < cols.length; i++) {
			ColumnMeta c = cols[i];
			Integer colWidth = settings.getInt("window.main.tab.colwidth." + c.name());
			if (colWidth == null) {
				colWidth = cols[i].defaultSize;
			}
			tcm.getColumn(i).setPreferredWidth(colWidth.intValue());
		}
		
		// restore window divider location
		Integer dividerLocation = settings.getInt("window.split.at");
		if (dividerLocation == null) {
			jslpSplitPane.setDividerLocation(0.5d);
		} else {
			jslpSplitPane.setDividerLocation(dividerLocation);
		}

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setVisible(true);

		app.addCallback(new AppCallback() {
			@Override public void commitDone(File f, String commitMessage) {}
			
			@Override
			public void pushDone() {
				jbPullAndDownload.setEnabled(true);
				jbReportChanges.setEnabled(true);
			}

			@Override
			public void fileChangeDetected(CommitProposal cp) {
				resTab.model.addRow(cp);
			}

			@Override
			public void downloadDone() {
				jbPullAndDownload.setEnabled(true);
				jbReportChanges.setEnabled(true);
				jbReportChanges.doClick();
			}
		});
		
		new SwingWorker<Object, Object>() {
			@Override
			protected Object doInBackground() throws Exception {
				app.connect();
				return null;
			}
			@Override
			protected void done() {
				try {
					get();
				} catch (InterruptedException | ExecutionException e) {
					throw new SwingException(e);
				}
				jbPullAndDownload.setEnabled(true);
				jbReportChanges.setEnabled(true);
			}
		}.execute();
	}
	
	private enum ColumnMeta {
		RESOURCE("Resource", false, 125, String.class, CompareBy.RESOURCE),
		WAB_NUMBER("WAB", false, 50, String.class, CompareBy.WAB),
		WORK_TITLE("Title", false, 250, String.class, CompareBy.TITLE),
		COMMIT_MESSAGE("Commit message", true, 750, String.class, CompareBy.COMMIT_MESSAGE),
		RESPONSIBLE("Responsible", false, 250, String.class, CompareBy.RESPONSIBLE),
		COMMIT_AS("Commit as", true, 100, Commiter.class, CompareBy.COMMITER_NAME),
		INCLUDE("Include?", true, 50, Boolean.class, CompareBy.SKIP_FLAG);
		
		public final String name;
		public final boolean isEditable;
		public final int defaultSize;
		public final Class<?> dataClass;
		public final CompareBy compareBy;
		
		private ColumnMeta(String name, boolean isEditable, int defaultSize, Class<?> dataClass, CompareBy compareBy) {
			this.name = name;
			this.isEditable = isEditable;
			this.defaultSize = defaultSize;
			this.dataClass = dataClass;
			this.compareBy = compareBy;
		}
	}
	
	private class ResourcesTable extends JTable {
		private static final long serialVersionUID = 1L;
		
		private final ResourcesTable thisTable;
		private final ResourcesTableModel model;

		private ResourcesTable(CommitProposalComparator comparator) {
			this.thisTable = this;
			
			this.model = new ResourcesTableModel(comparator);
			setModel(model);
			setAutoCreateRowSorter(false);
//			indicateSorting();
			
			CommiterRenderer cr = new CommiterRenderer();
			
			JComboBox<Commiter> jcbCommiter = new JComboBox<>();
			jcbCommiter.setRenderer(cr);
			for (Commiter c : App.getInstance().getCommiters()) {
				jcbCommiter.addItem(c);
			}

			TableColumn commitAsCol = getColumnModel().getColumn(ColumnMeta.COMMIT_AS.ordinal());
			commitAsCol.setCellRenderer(cr);
			commitAsCol.setCellEditor(new DefaultCellEditor(jcbCommiter));
			
			ActionListener alIncludeAll = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (CommitProposal cp : model.rows) {
						cp.setSkip(false);
					}
					model.fireTableDataChanged();
				}
			};
			ActionListener alExcludeAll = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (CommitProposal cp : model.rows) {
						cp.setSkip(true);
					}
					model.fireTableDataChanged();
				}
			};
			
			JMenuItem jmiIncludeAll = new JMenuItem("Include all");
			jmiIncludeAll.addActionListener(alIncludeAll);
			
			JMenuItem jmiExcludeAll = new JMenuItem("Exclude all");
			jmiExcludeAll.addActionListener(alExcludeAll);
			
			JPopupMenu jpmHeaderPopupMenu = new JPopupMenu();
			jpmHeaderPopupMenu.add(jmiIncludeAll);
			jpmHeaderPopupMenu.add(jmiExcludeAll);
			
			JTable thisTable = this;
			addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent me) {
					if (SwingUtilities.isRightMouseButton(me)) {
						jpmHeaderPopupMenu.show(thisTable, me.getX(), me.getY());
					}
				}
			});
			
			JTableHeader header = getTableHeader();
			header.setDefaultRenderer(new ResourcesTableHeaderCellRenderer());
			header.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent me) {
					
					int colIndex = thisTable.columnAtPoint(me.getPoint());
					ColumnMeta c = ColumnMeta.values()[colIndex];
					
					if (c.compareBy == model.comparator.getCompareBy()) {
						model.comparator.switchAscDesc();
						
					} else {
						model.comparator.setCompareBy(c.compareBy);
					}
					
					model.sort();
				}
			});
		}
		
		private class ResourcesTableHeaderCellRenderer implements TableCellRenderer {

			private final TableCellRenderer defaultHeaderCellRenderer;
			private final char ascChar, descChar;
			
			private ResourcesTableHeaderCellRenderer() {
				this.defaultHeaderCellRenderer = getTableHeader().getDefaultRenderer();
				
				JLabel testRenderer = (JLabel) defaultHeaderCellRenderer.getTableCellRendererComponent(thisTable, "character displayability test", false, false, 0, 0);
				Font defaultFont = testRenderer.getFont();
				this.ascChar = defaultFont.canDisplay('\u2191') ? '\u2191' : 'A';
				this.descChar = defaultFont.canDisplay('\u2193') ? '\u2193' : 'D';
			}
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel renderer = (JLabel) defaultHeaderCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				ColumnMeta cm = ColumnMeta.values()[column];
				if (cm.compareBy == model.comparator.getCompareBy()) {
					renderer.setText(cm.name + " " + (model.comparator.isAcsending() ? ascChar : descChar));
				} else {
					renderer.setText(cm.name);
				}
				
				return renderer;
			}
		}
		
		private class CommiterRenderer implements ListCellRenderer<Commiter>, TableCellRenderer {

			private final DefaultListCellRenderer dlcr = new DefaultListCellRenderer();
			private final DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();

			@Override
			public Component getListCellRendererComponent(JList<? extends Commiter> list, Commiter value, int index, boolean isSelected, boolean cellHasFocus) {
				JLabel renderer = (JLabel) dlcr.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				setupRenderer(renderer, value);
				return renderer;
			}
			
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel renderer = (JLabel) dtcr.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				setupRenderer(renderer, (Commiter) value);
				return renderer;
			}
			
			private void setupRenderer(JLabel jl, Commiter c) {
				if (jl == null || c == null) {
					return;
				}
				jl.setText(c.personIdent.getName());
				jl.setToolTipText(c.personIdent.getName() + " <" + c.personIdent.getEmailAddress() + ">");
			}
		}
		
		private class ResourcesTableModel extends AbstractTableModel {
			private static final long serialVersionUID = 1L;
			
			private final List<CommitProposal> rows;
			private final CommitProposalComparator comparator;
			
			private ResourcesTableModel(CommitProposalComparator comparator) {
				this.rows = new ArrayList<>();
				this.comparator = comparator;
			}

			private void sort() {
				Collections.sort(rows, comparator);
				fireTableDataChanged();
			}
			
			public void addRow(CommitProposal cp) {
				if (!rows.contains(cp)
						&& rows.add(cp)) {
					sort();
				}
			}
			
			public void clear() {
				rows.clear();
				fireTableDataChanged();
			}
			
			public boolean isEmpty() {
				return rows.isEmpty();
			}
			
			@Override
		    public boolean isCellEditable(int row, int column) {
		        return ColumnMeta.values()[column].isEditable;
		    }

			@Override
			public int getRowCount() {
				return rows.size();
			}

			@Override
			public int getColumnCount() {
				return ColumnMeta.values().length;
			}

			@Override
		    public String getColumnName(int column) {
		        return ColumnMeta.values()[column].name;
		    }

			@Override
			public Class<?> getColumnClass(int column) {
				return ColumnMeta.values()[column].dataClass;
			}
			
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				CommitProposal cp = rows.get(rowIndex);
				
				if (columnIndex == ColumnMeta.RESOURCE.ordinal()) {
					return cp.file.getName();
					
				} else if (columnIndex == ColumnMeta.WAB_NUMBER.ordinal()) {
					return cp.wab;
					
				} else if (columnIndex == ColumnMeta.WORK_TITLE.ordinal()) {
					return cp.title;
					
				} else if (columnIndex == ColumnMeta.COMMIT_MESSAGE.ordinal()) {
					return cp.getCommitMessage();
					
				} else if (columnIndex == ColumnMeta.RESPONSIBLE.ordinal()) {
					return cp.responsible;
					
				} else if (columnIndex == ColumnMeta.COMMIT_AS.ordinal()) {
					return cp.getCommiter();
					
				} else if (columnIndex == ColumnMeta.INCLUDE.ordinal()) {
					return !cp.isSkip();
				}
				
				throw new IllegalArgumentException("columnIndex");
			}

			@Override
		    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
				CommitProposal cp = rows.get(rowIndex);
				
				if (columnIndex == ColumnMeta.COMMIT_MESSAGE.ordinal()) {
					String str = (String) aValue;
					cp.setCommitMessage(str);
					
				} else if (columnIndex == ColumnMeta.COMMIT_AS.ordinal()) {
					Commiter c = (Commiter) aValue;
					cp.setCommiter(c);
					
				} else if (columnIndex == ColumnMeta.INCLUDE.ordinal()) {
					Boolean flag = (Boolean) aValue;
					cp.setSkip(!flag.booleanValue());
					
				} else {
					throw new IllegalArgumentException("columnIndex");
				}
				
				fireTableCellUpdated(rowIndex, columnIndex);
		    }
		}
	}
	
	private class JTextAreaHandler extends Handler {

		private final JTextArea jta;
		
		private JTextAreaHandler(JTextArea jta) {
			this.jta = jta;
		}
		
		@Override
		public void publish(LogRecord record) {
			String str = getFormatter().format(record);
			jta.append(str);
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}
	}
}

class SwingException extends RuntimeException {	// because of how swing worker exception handling was made...
	private static final long serialVersionUID = 1L;

	public SwingException(Throwable cause) {
		super(cause);
	}
}
