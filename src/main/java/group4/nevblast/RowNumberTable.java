/*
 * 
 This file is part of NEVBLAST.

 NEVBLAST is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 NEVBLAST is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with NEVBLAST.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package group4.nevblast;

/**
 *
 * @author ziggy
 */
import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

/*
 *	Use a JTable as a renderer for row numbers of a given main table.
 *  This table must be added to the row header of the scrollpane that
 *  contains the main table.
 */
public class RowNumberTable extends JTable
	implements ChangeListener, PropertyChangeListener
{
	private JTable main;

	public RowNumberTable(JTable table)
	{
		main = table;
		main.addPropertyChangeListener( this );

		setFocusable( false );
		setAutoCreateColumnsFromModel( false );
		setModel( main.getModel() );
		setSelectionModel( main.getSelectionModel() );

		TableColumn column = new TableColumn();
              //  Object columns[] = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", 'X', "Y", "Z", "*", "-" };
		column.setHeaderValue(" ");
		addColumn( column );
                
		column.setCellRenderer(new RowNumberRenderer());

		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
             
	}

	@Override
	public void addNotify()
	{
		super.addNotify();

		Component c = getParent();

		//  Keep scrolling of the row table in sync with the main table.

		if (c instanceof JViewport)
		{
			JViewport viewport = (JViewport)c;
			viewport.addChangeListener( this );
		}
	}

	/*
	 *  Delegate method to main table
	 */
	@Override
	public int getRowCount()
	{
		return main.getRowCount();
	}

	@Override
	public int getRowHeight(int row)
	{
		return main.getRowHeight(row);
	}

	/*
	 *  This table does not use any data from the main TableModel,
	 *  so just return a value based on the row parameter.
	 */
	@Override
	public Object getValueAt(int row, int column)
	{
		return Integer.toString(row + 1);
	}

	/*
	 *  Don't edit data in the main TableModel by mistake
	 */
	@Override
	public boolean isCellEditable(int row, int column)
	{
		return false;
	}
//
//  Implement the ChangeListener
//
	public void stateChanged(ChangeEvent e)
	{
		//  Keep the scrolling of the row table in sync with main table

		JViewport viewport = (JViewport) e.getSource();
		JScrollPane scrollPane = (JScrollPane)viewport.getParent();
		scrollPane.getVerticalScrollBar().setValue(viewport.getViewPosition().y);
	}
//
//  Implement the PropertyChangeListener
//
	public void propertyChange(PropertyChangeEvent e)
	{
		//  Keep the row table in sync with the main table

		if ("selectionModel".equals(e.getPropertyName()))
		{
			setSelectionModel( main.getSelectionModel() );
		}

		if ("model".equals(e.getPropertyName()))
		{
			setModel( main.getModel() );
		}
	}

	/*
	 *  Borrow the renderer from JDK1.4.2 table header
	 */
	private static class RowNumberRenderer extends DefaultTableCellRenderer
	{
             String columns[] = { "","A", "R", "N", "D", "C", "Q", "E", "G", "H", "I", "L", "K", "M", "F", "P", "S", "T", "W", "Y", "V", "B", "Z", "X", "*"};
		public RowNumberRenderer()
		{
			setHorizontalAlignment(JLabel.CENTER);
		}

		public Component getTableCellRendererComponent(
			JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			if (table != null)
			{
				JTableHeader header = table.getTableHeader();

				if (header != null)
				{
					setForeground(header.getForeground());
					setBackground(header.getBackground());
					setFont(header.getFont());
				}
			}

			if (isSelected)
			{
				setFont( getFont().deriveFont(Font.BOLD) );
			}

			setText((value == null) ? "" : columns[Integer.valueOf(value.toString())]);
			setBorder(UIManager.getBorder("TableHeader.cellBorder"));

			return this;
		}
	}
}