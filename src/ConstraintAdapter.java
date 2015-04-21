import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Logger;

/*
 * Cette classe permet de convertir les contraintes  ainsi que la fonction 
 * �conomique (les in�quations) en la matrice simplex de d�part.
 */
public class ConstraintAdapter {
	//Liste contenant les in�quations.
	public ArrayList<String> contraintes;
	public String fonctionEconomique;
	public String[] valeursBase;
	public String[] matriceInfo;
	public float[][] matrice ;
	public float[] valeursSolutions;
	public float[] valeursHorsBase;
	public boolean isValid = true;
	public int nbAgrandissement = 1;
	public float z = 0;
	public int nbVariables = 0;
	
	public boolean isEcartsNegatifs = false;
	
	public ConstraintAdapter(ArrayList<String> contraintes,String fonctionEconomique) 
	{
		super();
		this.contraintes = contraintes;
		this.fonctionEconomique = fonctionEconomique;
		
		try 
		{
			//Transformation des in�quations.
			construireValeursBase();
			construireValeursHorsBaseEtMatriceInfo();
			nbVariables = matriceInfo.length - valeursBase.length;
			construireMatriceEtValeursSolution();
			if(isEcartsNegatifs)
			{
				mettreAjourFonctionEconomique();
			}
		}
		catch(Exception e)
		{
			System.out.println("Expression mal form�!!!");
			System.out.println("Le programme va se relancer");
			System.out.println(); System.out.println(); System.out.println();
			isValid = false;
		}
		
	}
	
	//Permet de construire les variables d'�cart et les valeurs base.
	public void construireValeursBase()
	{
		valeursBase = new String[contraintes.size()];
		
		for(int i = 0; i < contraintes.size(); i++)
		{
			//On cr�� une nouvelle variable d'�cart.
			valeursBase[i] = "E" + (i+1);
		}
	}
	
	public void construireValeursHorsBaseEtMatriceInfo() throws Exception
	{
		//On transforme la fonction �conomique
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
		
		//Construction des variables d'�carts.
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
			
			//Il faut introduire des variables Yx.
			if(contrainte.contains(">="))
			{
				isEcartsNegatifs = true;
				
				String[] elements = contrainte.split(">=");
				
				//on charge la valeur solution.
				valeursSolutions[i] = new Float(elements[1]);
				
				//une contrainte
				elements = elements[0].split("\\+");
				
				//Pour chaques �l�ments de la contrainte.
				int x =0;
				for(x = 0; x < elements.length; x++)
				{
					String[] element = elements[x].split("X");
					matrice[i][getIndexOfXxVal("X" + element[1])] = new Float(element[0]);
				}
				  
				agrandirMatriceInfo("Y");
				agrandirFonctionEconomique();
				elargirMatrice(i);
				valeursHorsBase[i+nbVariables] = -1;
				valeursBase[i] = "Y"+nbAgrandissement;
				z += valeursSolutions[i];
				nbAgrandissement++;
				
				matrice[i][getIndexOfXxVal("E"+(i+1))] = -1;
			}
			else
			{
				
				String[] elements = contrainte.split("<=");
				
				//on charge la valeur solution.
				valeursSolutions[i] = new Float(elements[1]);
				
				//une contrainte
				elements = elements[0].split("\\+");
				
				//Pour chaques �l�ments de la contrainte.
				int x =0;
				for(x = 0; x < elements.length; x++)
				{
					String[] element = elements[x].split("X");
					matrice[i][getIndexOfXxVal("X" + element[1])] = new Float(element[0]);
				}
				  
				matrice[i][getIndexOfXxVal("E"+(i+1))] = 1;
			}
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
	
	public void agrandirMatriceInfo(String element)
	{
		String[] nouvelleMatriceInfo = new String[matriceInfo.length+1];
		
		for(int i = 0; i<matriceInfo.length; i++)
		{
			nouvelleMatriceInfo[i] = matriceInfo[i];
		}
		
		nouvelleMatriceInfo[matriceInfo.length] = element + nbAgrandissement;
		matriceInfo = nouvelleMatriceInfo;
	}
	
	public void agrandirFonctionEconomique()
	{
		float[] nouvelleVHB = new float[valeursHorsBase.length+1];
		
		for(int i = 0; i<valeursHorsBase.length; i++)
		{
			nouvelleVHB[i] = valeursHorsBase[i];
		}
		
		nouvelleVHB[valeursHorsBase.length] = 0;
		valeursHorsBase = nouvelleVHB;	
	}
	
	public void elargirMatrice(int derniereLigneIndex)
	{
		float[][] nouvelleMatrice = new float[matrice.length][matrice[0].length+1];
		for(int i = 0;i<matrice.length;i++)
		{
			for(int j = 0;j<matrice[0].length;j++)
			{
				nouvelleMatrice[i][j] = matrice[i][j];
			}
		}
		
		nouvelleMatrice[derniereLigneIndex][matrice[0].length] = 1;
		matrice = nouvelleMatrice;
	}
	
	public void mettreAjourFonctionEconomique()
	{
		valeursHorsBase = new float[valeursHorsBase.length];
		for(int i = 0; i<valeursBase.length; i++)
		{
			if(valeursBase[i].contains("Y"))
			{
				for(int j=0; j<nbVariables + contraintes.size();j++)
				{
					valeursHorsBase[j] += matrice[i][j];
				}
			}
		}
	}
	
}
