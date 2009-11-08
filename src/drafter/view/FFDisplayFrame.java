package drafter.view;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;



import drafter.comms.Client;
import drafter.comms.Server;
import drafter.data.PlayerTableModel;
import drafter.parser.ESPNDataParser;
import drafter.parser.FFTPosDataParser;
import drafter.parser.YahooDataParser;

public class FFDisplayFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private JPanel contentPane;

	private Server server;

	protected ArrayList<RankingsTable> tableList;

	private boolean runAsClient = false;

	private PlayerTableModel interestedListModel;

	public FFDisplayFrame(String title) throws IOException, ClassNotFoundException {
		this(title, false);
	}

	public FFDisplayFrame(String title, boolean runAsClient) throws IOException, ClassNotFoundException {
		super(title);
		this.runAsClient = runAsClient;
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);

		interestedListModel = new PlayerTableModel();
		PlayerTableModel teamListModel = new PlayerTableModel();

		tableList = new ArrayList<RankingsTable>();
		JTabbedPane tabbedPane = new JTabbedPane();

		// Create table 1
		RankingsTable table1 = createRankingsTable(new ESPNDataParser().getData(),interestedListModel,teamListModel,false);
		tableList.add(table1);
		JScrollPane jScrollPane1 = new JScrollPane(table1);
		tabbedPane.add(jScrollPane1,"ESPN");

		// Create table 2
		RankingsTable table2 = createRankingsTable(new YahooDataParser().getData(),interestedListModel,teamListModel,false);
		tableList.add(table2);
		JScrollPane jScrollPane2 = new JScrollPane(table2);
		tabbedPane.add(jScrollPane2,"Yahoo");

		// Create table 3
		RankingsTable table3 = createRankingsTable(new FFTPosDataParser().getData(),interestedListModel,teamListModel,true);
		tableList.add(table3);
		JScrollPane jScrollPane3 = new JScrollPane(table3);
		tabbedPane.add(jScrollPane3,"Football Toolkit");

		
		setUpSyncComms();

		for (RankingsTable table : tableList) {
			table.addTableModelListener(new PlayerTableModelListener());
		}

		JPanel optionPanel = new JPanel(new BorderLayout());

		final JCheckBox showTakenCheckBox = new JCheckBox("Show Taken Players");
		showTakenCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(showTakenCheckBox.isSelected()) {
					for (RankingsTable table : tableList) {
						table.showTakenPlayers(true);
					}
				}
				else {
					for (RankingsTable table : tableList) {
						table.showTakenPlayers(false);
					}
				}
			}
		});

		optionPanel.add(showTakenCheckBox,BorderLayout.WEST);

		JPanel positionFilterPanel = new JPanel();
		final JComboBox positionFilterComboBox = new JComboBox(new Object[]{"None","QB", "RB","WR","TE","K","DEF"});
		JLabel positionFilterLabel = new JLabel("Position Filter");
		positionFilterPanel.add(positionFilterLabel,BorderLayout.WEST);
		positionFilterPanel.add(positionFilterComboBox,BorderLayout.EAST);

		positionFilterComboBox.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = (String) positionFilterComboBox.getSelectedItem();
				for (RankingsTable table : tableList) {
					table.filterPosition(selectedItem);
				}
			}
		});

		optionPanel.add(positionFilterPanel,BorderLayout.CENTER);

		contentPane.add(optionPanel,BorderLayout.NORTH);

		JSplitPane bottomPane = new JSplitPane();
//		JTabbedPane bottomPane = new JTabbedPane();
		JList myTeamList = new JList(teamListModel);
//		myTeamList.getColumnExt(1).setPreferredWidth(30);
		JPanel myTeamPanel = new JPanel(new BorderLayout());
		myTeamPanel.setBorder(BorderFactory.createTitledBorder("My Team"));
		myTeamPanel.add(new JScrollPane(myTeamList),BorderLayout.CENTER);
		bottomPane.setRightComponent(myTeamPanel);
//		bottomPane.add(myTeamPanel,"My Team");

		final JList myInterestedList = new JList(interestedListModel);
//		Dimension size = new Dimension(300,100);
//		myInterestedList.setPreferredSize(size);
//		myInterestedList.getColumnExt(1).setMaxWidth(30);
		JPanel myInterestedPanel = new JPanel(new BorderLayout());
		myInterestedPanel.setBorder(BorderFactory.createTitledBorder("Interested List"));
		myInterestedPanel.add(new JScrollPane(myInterestedList),BorderLayout.CENTER);
//		bottomPane.add(myInterestedPanel,"Interested List");

		myInterestedList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					final int selectedIndex = myInterestedList.locationToIndex(e.getPoint());
					myInterestedList.getSelectionModel().setSelectionInterval(selectedIndex,selectedIndex);
					JPopupMenu popupMenu = new JPopupMenu();
					popupMenu.add(new AbstractAction("Remove") {

						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e) {
							interestedListModel.remove(selectedIndex);
						}
					});
					popupMenu.show(myInterestedList, (int)e.getPoint().getX(), (int)e.getPoint().getY());
				}
			}
		});

		bottomPane.setLeftComponent(myInterestedPanel);
		bottomPane.setOneTouchExpandable(true);
		bottomPane.setDividerLocation(0.5);
		bottomPane.setDividerSize(2);
		bottomPane.setResizeWeight(0.5);

//		bottomPane.setPreferredSize(new Dimension(400, 100));

		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setTopComponent(tabbedPane);
		mainSplitPane.setBottomComponent(bottomPane);
		mainSplitPane.setResizeWeight(0.65);
		mainSplitPane.setDividerSize(3);

		contentPane.add(mainSplitPane,BorderLayout.CENTER);
//		contentPane.add(bottomPane,BorderLayout.SOUTH);

//		setPreferredSize(new Dimension(400, 600));

		pack();
		setVisible(true);
	}

	protected void setUpSyncComms() throws UnknownHostException, IOException, ClassNotFoundException {
		if(runAsClient) {
			new Client(tableList);
		}
		else {
			if(!Boolean.getBoolean("standalone")) {
				server = new Server(tableList);
			}
		}
	}

	protected RankingsTable createRankingsTable(Vector<Vector<String>> data, PlayerTableModel interestedListModel, PlayerTableModel teamListModel, boolean showByeWeek) {
		if(runAsClient) {
			data.clear();
			return new RankingsTable(data,interestedListModel,teamListModel,showByeWeek, false);
		}
		return new RankingsTable(data,interestedListModel,teamListModel,showByeWeek);
	}

	private class PlayerTableModelListener implements TableModelListener {

		@Override
		public void tableChanged(TableModelEvent e) {

			if(server != null) {
				server.updateModel(e);
			}

			for (RankingsTable table : tableList) {
				table.syncModel(e);
			}

			interestedListModel.update(e);
		}
	}
}
