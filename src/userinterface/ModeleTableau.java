package userinterface;

import javax.swing.table.DefaultTableModel;

class ModeleTableau extends DefaultTableModel
{
	private static final long	serialVersionUID	= -8470134351681442630L;

	ModeleTableau()
	{
		super();
	}

	// Permet d'éviter qu'on modifie les cellules
	@Override
	public boolean isCellEditable( int a , int b )
	{
		return false;
	}
}
