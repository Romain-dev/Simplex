import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
 * Cette classe permet de convertir les contraintes  ainsi que la fonction 
 * économique (les inéquations) en la matrice simplex de départ.
 */
public class ConstraintAdapter {
	//Liste contenant les inéquations.
	public ArrayList<String> contraintes;
	public String fonctionEconomique;
	public String[] valeursBase;
	public String[] matriceInfo;
	public float[][] matrice ;
	public float[] valeursSolutions;
	public float[] valeursHorsBase;
	public boolean isValid = true;
	
	public ConstraintAdapter(ArrayList<String> contraintes,String fonctionEconomique) 
	{
		super();
		this.contraintes = contraintes;
		this.fonctionEconomique = fonctionEconomique;
		
		try 
		{
			//Transformation des inéquations.
			construireValeursBase();
			construireValeursHorsBaseEtMatriceInfo();
			construireMatriceEtValeursSolution();
		}
		catch(Exception e)
		{
			System.out.println("Expression mal formé!!!");
			System.out.println("Le programme va se relancer");
			System.out.println(); System.out.println(); System.out.println();
			isValid = false;
		}
		
	}
	
	//Permet de construire les variables d'écart et les valeurs base.
	public void construireValeursBase()
	{
		valeursBase = new String[contraintes.size()];
		
		for(int i = 0; i < contraintes.size(); i++)
		{
			//On créé une nouvelle variable d'écart.
			valeursBase[i] = "E" + (i+1);
		}
	}
	
	public void construireValeursHorsBaseEtMatriceInfo() throws Exception
	{
		//On transforme la fonction économique
		fonctionEconomique = fonctionEconomique.replace(" ", "");
		String[] elements = fonctionEconomique.split("\\+");
		
		matriceInfo = new String[elements.length + valeursBase.length];
		valeursHorsBase = new float[elements.length + valeursBase.length];
		
		//Construction des valeurs hors base Xx et de la variable matriceInfo
		int i =0;
		for(i = 0; i < elements.length; i++)
		{
			String[] element = elements[i].split("X");
			matriceInfo[i] = "X" + element[1];
			valeursHorsBase[i] = new Float(element[0]);
		}
		
		//Construction des variables d'écarts.
		for(int j = 0; j < valeursBase.length; j++)
		{
			matriceInfo[i + j] = valeursBase[j];
			valeursHorsBase[i + j] = 0;
		}	
	}
	
	//Construit le contenue de la matrice principale ainsi que les variables solutions.
	public void construireMatriceEtValeursSolution()
	{
		valeursSolutions = new float[valeursBase.length];
		matrice = new float[valeursBase.length][matriceInfo.length];
		
		//pour chaque ligne de la matrice simplex
		for(int i =0; i<valeursBase.length;i++)
		{
			
			String contrainte = contraintes.get(i);
			contrainte = contrainte.replace(" ", "");
			String[] elements = contrainte.split("<=");
			
			//on charge la valeur solution.
			valeursSolutions[i] = new Float(elements[1]);
			
			//une contrainte
			elements = elements[0].split("\\+");
			
			//Pour chaques éléments de la contrainte.
			int x =0;
			for(x = 0; x < elements.length; x++)
			{
				String[] element = elements[x].split("X");
				matrice[i][getIndexOfXxVal("X" + element[1])] = new Float(element[0]);
			}
			  
			matrice[i][getIndexOfXxVal("E"+(i+1))] = 1;
		} 
	}
	
	//Donne la position dans une ligne de la matrice de la variable Xx.
	public int getIndexOfXxVal(String xVal)
	{
		int index = -1;
		for(int i = 0; i < matriceInfo.length; i++)
		{
			if(matriceInfo[i].equals(xVal))
			{
				index = i;
				break;
			}
		}
		
		return index;
	}
}
