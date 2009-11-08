package drafter.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.AbstractHighlighter;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.ComponentAdapter;
import org.jdesktop.swingx.decorator.CompoundHighlighter;
import org.jdesktop.swingx.decorator.Filter;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.jdesktop.swingx.decorator.PatternPredicate;

import drafter.data.Player;
import drafter.data.PlayerTableModel;
import drafter.util.browser.BrowserUtil;

/**
 * Basic implementation of the table containing the 
 * rankings.  This class uses swingx's JXTable for 
 * its superior highlighting and hiding features.
 * 
 * Each additional data source will have its own 
 * instance of this table.
 * 
 * @author Josh
 *
 */
public class RankingsTable extends JXTable {

	private static final long serialVersionUID = 1L;
	private static final String NAME_COLUMN = "Name";
	private static final String HIDDEN_COLUMN = "Hidden";
	private static final String RANK_COLUMN = "Rank";
	private static final String POSITION_COLUMN = "Position";
	private static final String PROJ_POINTS_COLUMN = "Points";
	private static final String BYE_WEEK_COLUMN = "Bye Week";

	private static String[] columns = new String[] {RANK_COLUMN, NAME_COLUMN, POSITION_COLUMN, "Team", PROJ_POINTS_COLUMN, BYE_WEEK_COLUMN, HIDDEN_COLUMN};

	private JPopupMenu popupMenu;

	private int selectedRow;

	private boolean showHiddenPlayers = false;
	private boolean filterPosition = false;
	private char positionChar;
	private PlayerTableModel teamListModel;
	private PlayerTableModel interestedListModel;

	public RankingsTable(Vector<Vector<String>> data, PlayerTableModel interestedListModel, PlayerTableModel teamListModel, boolean showByeWeek) {
		this(data, interestedListModel, teamListModel, showByeWeek, true);
	}

	public RankingsTable(Vector<Vector<String>> data, PlayerTableModel interestedListModel, PlayerTableModel teamListModel, boolean showByeWeek, boolean allowHide) {
		super(data,new Vector<String>(Arrays.asList(columns)));

		setEditable(false);
		setHighlighters(HighlighterFactory.createAlternateStriping(),new HiddenHighlighter());
		setColumnControlVisible(true);
		setRolloverEnabled(true);
		packAll();

		Comparator<Object> numberComparator = new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				Integer d1 = Integer.valueOf(o1 == null ? "0" : (String)o1);
				Integer d2 = Integer.valueOf(o2 == null ? "0" : (String)o2);
				return d1.compareTo(d2);
			}
		};

		getColumnExt(RANK_COLUMN).setComparator(numberComparator);
		getColumnExt(BYE_WEEK_COLUMN).setComparator(numberComparator);
		getColumnExt(PROJ_POINTS_COLUMN).setComparator(numberComparator);
		
		getColumnExt(BYE_WEEK_COLUMN).setVisible(showByeWeek);
		
		getColumnExt(HIDDEN_COLUMN).setVisible(false);

		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		addMouseListener(new PlayerPopupMenuClickListener());

		showTakenPlayers(false);

		this.teamListModel = teamListModel;
		this.interestedListModel = interestedListModel;

		initPopupMenu(allowHide);

	}

	public String[] getColumnIdentifiers() {
		return columns;
	}

	public void addTableModelListener(
			TableModelListener playerTableModelListener) {
		getModel().addTableModelListener(playerTableModelListener);
	}

	@Override
	public DefaultTableModel getModel() {
		return (DefaultTableModel) super.getModel();
	}

	private PatternFilter createHiddenFilter() {
		return new PatternFilter("false", 0, getColumnIndex(HIDDEN_COLUMN));
	}

	private PatternFilter createPositionFilter() {
		return new PatternFilter("^"+positionChar, 0, getColumnIndex(POSITION_COLUMN));
	}

	private PatternFilter createHiddenPositionFilter() {
		return new MultiColumnFilter(new FilterColumnPattern[] {
				new FilterColumnPattern(getColumnIndex(POSITION_COLUMN), "^"+positionChar),
				new FilterColumnPattern(getColumnIndex(HIDDEN_COLUMN), "false")
		});
	}

	public void showTakenPlayers(boolean show) {
		if(show) {
			showHiddenPlayers = true;
		}
		else {
			showHiddenPlayers = false;
		}

		applyFilters();
	}

	public void filterPosition(String selectedItem) {

		if(selectedItem.equals("None")) {
			filterPosition = false;
		}
		else {
			filterPosition = true;
			positionChar = selectedItem.charAt(0);
		}

		applyFilters();
	}

	private void applyFilters() {

		Filter filter = null;

		if(!showHiddenPlayers && !filterPosition) {
			filter = createHiddenFilter();
		}
		else if(!showHiddenPlayers && filterPosition) {
			filter = createHiddenPositionFilter();
		}
		else if(showHiddenPlayers && filterPosition) {
			filter = createPositionFilter();
		}

		if(filter == null) {
			setFilters(null);
		}
		else {
			setFilters(new FilterPipeline(filter));
		}
	}

	private void initPopupMenu(boolean allowHide) {
		popupMenu = new JPopupMenu("Menu");
		if(allowHide) {
			popupMenu.add(new HidePlayerAction());
		}
		popupMenu.add(new MarkInterestedAction());
		popupMenu.add(new SelectForTeamAction());
		popupMenu.add(new ViewPlayerInBrowserAction("Yahoo!"));
		popupMenu.add(new ViewPlayerInBrowserAction("ESPN"));
		popupMenu.add(new ViewPlayerInBrowserAction("NFL.com"));
		popupMenu.add(new ViewPlayerInBrowserAction("FFToolbox.com"));
		String extraSource = System.getProperty("extraSource");
		if(extraSource != null) {
			popupMenu.add(new ViewPlayerInBrowserAction(extraSource));
		}
	}

	private String getName(int row) {
		return ((String) getModel().getValueAt(convertRowIndexToModel(row), getColumnIndex(NAME_COLUMN))).trim();
	}

	private String getPos(int row) {
		return ((String) getModel().getValueAt(convertRowIndexToModel(row), getColumnIndex(POSITION_COLUMN))).trim();
	}

	private boolean isHidden(int row) {
		String hidden = (String) getModel().getValueAt(convertRowIndexToModel(row), getColumnIndex(HIDDEN_COLUMN));
		return "false".equals(hidden);
	}

	public void setHidden(int row) {
		getModel().setValueAt("true", convertRowIndexToModel(row), getColumnIndex(HIDDEN_COLUMN));
	}

	private int getColumnIndex(String name) {
		for (int i = 0; i < columns.length; i++) {
			if(columns[i].equals(name)) {
				return i;
			}
		}
		return -1;
	}

	private class MultiColumnFilter extends PatternFilter {

		private FilterColumnPattern[] filters;

		public MultiColumnFilter( FilterColumnPattern ... filters  ) {
			this.filters = filters;
		}

		@Override
		public boolean test( int row ) {

			FilterColumnPattern positionFilter = filters[0];
			String text = getInputString( row, positionFilter.getColumn() );

			Pattern pattern = Pattern.compile(positionFilter.getPattern());

			if(pattern.matcher(text).find()) {
				FilterColumnPattern hiddenFilter = filters[1];
				text = getInputString( row, hiddenFilter.getColumn() );

				pattern = Pattern.compile(hiddenFilter.getPattern());

				if(pattern.matcher(text).find()) {
					return true;
				}
			}

			return false;
		}
	}

	private class FilterColumnPattern {

		private String pattern;
		private int column;

		public FilterColumnPattern(int column, String pattern) {
			this.column = column;
			this.pattern = pattern;
		}

		public int getColumn() {
			return column;
		}

		public String getPattern() {
			return pattern;
		}
	}

	private class PlayerPopupMenuClickListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if(SwingUtilities.isRightMouseButton(e)) {
				selectedRow = rowAtPoint(e.getPoint());
				getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
				popupMenu.show(RankingsTable.this, (int)e.getPoint().getX(), (int)e.getPoint().getY());
			}
		}
	}

	private class HidePlayerAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public HidePlayerAction() {
			super("Hide");
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = getName(selectedRow);
			if(isHidden(selectedRow)) {
				setHidden(selectedRow);
				System.out.println("Hide " + name);
			}
		}
	}

	private class MarkInterestedAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public MarkInterestedAction() {
			super("Add to interested list");
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = getName(selectedRow);
			String pos = getPos(selectedRow);
			Player player = new Player();
			player.setName(name);
			player.setPos(pos);
			interestedListModel.addPlayer(player);
		}
	}

	private class SelectForTeamAction extends AbstractAction {

		private static final long serialVersionUID = 1L;
		public SelectForTeamAction() {
			super("Add to my team");
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = getName(selectedRow);
			String pos = getPos(selectedRow);
			Player player = new Player();
			player.setName(name);
			player.setPos(pos);
			teamListModel.addPlayer(player);
		}
	}

	private class ViewPlayerInBrowserAction extends AbstractAction {

		private static final long serialVersionUID = 1L;

		private String source;

		public ViewPlayerInBrowserAction(String source) {
			super("View Details from " + source);
			this.source = source;
		}

		//		public ViewPlayerInBrowserAction() {
		//			super("View Details");
		//		}
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = getName(selectedRow);

			String searchTerm = name.replaceFirst("\\s+", "+");
			if(source != null) {
				searchTerm += "+" + source;
			}
			String searchQuery = "http://www.google.com/search?client=firefox-a&rls=org.mozilla%3Aen-US%3Aofficial&hs=chK&hl=en&q="+ searchTerm  +"&btnI=I%27m+Feeling+Lucky&aq=f&oq=&aqi=g10";

			BrowserUtil.displayURL(searchQuery);

		}
	}

	private class FontHighlighter extends AbstractHighlighter {

		private Font font;
		public FontHighlighter(HighlightPredicate predicate, Font font) {
			super(predicate);
			this.font = font;
		}
		@Override
		protected Component doHighlight(Component component, ComponentAdapter arg1) {
			component.setFont(font);
			return component;
		}

	}

	private class HiddenHighlighter extends CompoundHighlighter {

		public HiddenHighlighter() {
			Font italicFont = getFont().deriveFont(Font.ITALIC);
			PatternPredicate patternPredicate = new PatternPredicate("true", getColumnIndex(HIDDEN_COLUMN));
			ColorHighlighter grayColorHighlighter = new ColorHighlighter(patternPredicate, null,
					Color.GRAY, null, Color.GRAY);
			FontHighlighter italicFontHighlighter = new FontHighlighter(patternPredicate,
					italicFont);

			addHighlighter(italicFontHighlighter);
			addHighlighter(grayColorHighlighter);
		}
	}

	public void syncModel(TableModelEvent e) {
		Object source = e.getSource();

		if(source instanceof DefaultTableModel) {
			DefaultTableModel model = (DefaultTableModel) source;

			if(model != getModel()) {

				// temporarily disable table model listeners, as we will keep
				// updating the other tables, then they will update us, etc
				TableModelListener[] tableModelListeners = getModel().getTableModelListeners();
				for (TableModelListener tableModelListener : tableModelListeners) {
					getModel().removeTableModelListener(tableModelListener);
				}

				//sync data from another table
				int column = e.getColumn();
				int nameCol = model.findColumn(NAME_COLUMN);
				int firstRow = e.getFirstRow();
				String name = (String) model.getValueAt(firstRow, nameCol);
				Object valueAt = model.getValueAt(firstRow, column);

				int myNameCol = getModel().findColumn(NAME_COLUMN);
				int myHiddenCol = getModel().findColumn(HIDDEN_COLUMN);
				for (int i = 0; i < getModel().getRowCount(); i++) {
					if(name.equals(getModel().getValueAt(i, myNameCol))) {
						getModel().setValueAt(valueAt, i, myHiddenCol);
						System.out.println("Updating status for "+name);
						break;
					}
				}

				applyFilters();

				for (TableModelListener tableModelListener : tableModelListeners) {
					getModel().addTableModelListener(tableModelListener);
				}
			}
		}
	}
}
