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
	public int nbAgrandissement = 1;
	public float z = 0;
	
	//Nombre de variables du problème initial.
	public int nbVariables = 0;
	
	//Contient des écarts négatifs dans les contraintes (ex 6X1+2X2 >= 200 ==> 6X1+2X2-E1=200).
	public boolean isEcartsNegatifs = false;
	
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
			nbVariables = matriceInfo.length - valeursBase.length;
			construireMatriceEtValeursSolution();
			
			//Si il y a des écarts négatifs
			if(isEcartsNegatifs)
			{
				mettreAjourFonctionEconomique();
			}
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
			
			//Il faut introduire des variables Yx car il y a des variables négatives dans cette contrainte.
			if(contrainte.contains(">="))
			{
				isEcartsNegatifs = true;
				
				String[] elements = contrainte.split(">=");
				
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
				  
				//Traitement spécifique au fait qu'il y ait des variables négatives.
				//Introduction d'une variable Y.
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
	
	//Agrandir la variables MatriceInfo avec une variable de type Yx passé en paramètre.
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
	
	//Ajoute une case vide (0) en fin de la fonction économique.
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
	
	//Ajouter une colonne du cotès droit de la matrice pour introduire une nouvelle variable.
	public void elargirMatrice(int derniereLigneIndex)
	{
		float[][] nouvelleMatrice = new float[matrice.length][matrice[0].length+1];
		//Copie de la matrice en plus grand.
		for(int i = 0;i<matrice.length;i++)
		{
			for(int j = 0;j<matrice[0].length;j++)
			{
				nouvelleMatrice[i][j] = matrice[i][j];
			}
		}
		
		//On afffecte la valeur de Y pour cette ligne à 1.
		nouvelleMatrice[derniereLigneIndex][matrice[0].length] = 1;
		matrice = nouvelleMatrice;
	}
	
	
	//Redéfinir la fonction économique
	public void mettreAjourFonctionEconomique()
	{
		valeursHorsBase = new float[valeursHorsBase.length];
		for(int i = 0; i<valeursBase.length; i++)
		{
			//Si la ligne de la matrice contient la valeur base Yx, alors on ajoute la ligne de la matrice
			//à la fonction économique.
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
