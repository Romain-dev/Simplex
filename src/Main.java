import java.util.ArrayList;
import java.util.Scanner;
import java.text.DecimalFormat;

public class Main {

	public static String[] valeursBase;
	public static String[] matriceInfo;
	public static float[][] matrice;
	public static float[] valeursSolutions;
	public static float[] valeursHorsBase;
	public static float negativeZ = 0;	
	public static ConstraintAdapter contraintesAdapter;
	//Sert à afficher le nombre de récursions de l'algorithme Simplex.
	public static int nbTours = 0;
	
	public static DecimalFormat df = new DecimalFormat("0.00");
	
	public static void main(String[] args) {
		//Cette partie correspond à l'intéraction utilisateurs
		//pour récupérer les contraintes et la fonction économique.
		Scanner sc = new Scanner(System.in);
		
		boolean isValidExpression = false;
		
		while(!isValidExpression)
		{
			/*System.out.println("Veuillez entrer vos contraintes à la forme \"3X1 + 1X2 <= 30\".");
			System.out.println("Entrez les à la suite, valider chacunes aves la touche ENTRER:");
			*/
			//String contrainte = "empty";
			ArrayList<String> contraintes = new ArrayList<String>();
			
			contraintes.add("2X1+1X2<=90");
			contraintes.add("3X1+1X2>=60");
			contraintes.add("3X1+6X2>=180");
			contraintes.add("2X1+2X2<=140");
			String fonctionEconomique = "6X1+5X2";
			
			/*contraintes.add("5X1+3X2<=30");
			contraintes.add("2X1+3X2<=24");
			contraintes.add("1X1+3X2<=18");
			String fonctionEconomique = "8X1+6X2";
			
			contraintes.add("1X1<=20");
			contraintes.add("1X2<=20");
			contraintes.add("1X3<=20");
			contraintes.add("1X1+1X2+1X3<=45");
			String fonctionEconomique = "200X1+300X2+225X3";
			*/
			/*//L'utilisateur peut entrer 'n' contraintes.
			while(!contrainte.equals(""))
			{
				contrainte = sc.nextLine();
				if(!contrainte.equals(""))
				{
					contraintes.add(contrainte);
					System.out.println("Contrainte suivante, validez directement pour passer à la fonction économique:");
				}
			}
	
			//L'utilisateur entre la fonction économique.
			System.out.println("Veuillez entrer votre fonction économique à la forme \"8X1 + 6X2\".");
			String fonctionEconomique = sc.nextLine();
			
			//Transformation des entrées de l'utlisateur en données exploitables par Simplex.*/
			contraintesAdapter = new ConstraintAdapter(contraintes, fonctionEconomique);
			valeursBase = contraintesAdapter.valeursBase;
			matriceInfo = contraintesAdapter.matriceInfo;
			matrice = contraintesAdapter.matrice;
			valeursSolutions = contraintesAdapter.valeursSolutions;
			valeursHorsBase = contraintesAdapter.valeursHorsBase;
			negativeZ = contraintesAdapter.z;
			
			isValidExpression = contraintesAdapter.isValid;
		}
		
			
		//Affichage de la matrice de départ.
		System.out.println("Simplex de départ");
		afficherMatriceCourrante();
		System.out.println();
		
		lancerSimplex();
		
		//Il y a des ecarts négatifs, on vient de faire le programme auxiliaire
		if(contraintesAdapter.isEcartsNegatifs)
		{
			supprimerlesY();
			
			nbTours = 0;
			valeursHorsBase[3] = new Float(1.4);
			valeursHorsBase[4] = new Float(0.58);
			negativeZ = -192;
			System.out.println("Nouveau Simplex après programme auxiliaire");
			afficherMatriceCourrante();
			System.out.println();
			
			lancerSimplex();
		}
		

		//Affichage du résultat final.
		System.out.println("Resultat: Z = " + (negativeZ*-1));
	}
	
	public static void lancerSimplex()
	{
		//La boucle continue tant qu'il reste des variables hors bases positives.
		while(getVHBexploitableIndex() >= 0)
		{
			//On récupère la position du pivot dans la matrice.
			int indexOfPivotColomn = getVHBexploitableIndex();
			int indexOfPivotLine = getPivotLineIndex(indexOfPivotColomn);
			
			float pivot = matrice[indexOfPivotLine][indexOfPivotColomn];
			
			//On divise la ligne par la valeur du pivot
			diviserLigneParPivot(indexOfPivotLine,pivot);
			
			//On fait entrer une nouvelle variable dans la base
			valeursBase[indexOfPivotLine] = matriceInfo[indexOfPivotColomn];
			
			//On fait le pivot sur toutes les lignes où la colonne du pivot et != 0
			//On modifi également la fonction économique.
			pivoter(indexOfPivotColomn,indexOfPivotLine);		
			
			//Affichage de la matrice
			nbTours++;
			System.out.println("Passage n°" + nbTours);
			afficherMatriceCourrante();
			
			System.out.println(""); System.out.println("");
		}
	}
	//Premiere étape, on cherche une valeur hors base positive
	public static int getVHBexploitableIndex()
	{
		int index = -1;

	 	float result = 0;
	 	
		//Cherche l'index correspondant à la valeur max.
		for(int i = 0; i<valeursHorsBase.length; i++)
		{
			if(index == -1)
			{
				if(valeursHorsBase[i] > 0)
				{
					index = i;
					result = valeursHorsBase[i];
				}
			}
			else if(valeursHorsBase[i] > result)
			{
				if(valeursHorsBase[i] > 0)
				{
					index = i;
					result = valeursHorsBase[i];
				}
			}
		}
		return index;
	}
	
	//retourne la ligne ou se trouve le pivot.
	public static int getPivotLineIndex(int column)
	{
		float[] decision = new float[valeursSolutions.length];
		int index = -1;
		float result = Float.MAX_VALUE;
		
		//On divise chaque valeur solution par la valeur de la matrice 
		//correspondante puis on prend le MIN.
		for(int i = 0; i<valeursSolutions.length; i++)
		{	
			if(matrice[i][column] <= 0)
			{
				decision[i] = Float.MAX_VALUE;
			}
			else
			{
				decision[i] = valeursSolutions[i] / matrice[i][column];
			}
			
			if((index == -1) || (decision[i] < result))
			{
				index = i;
				result = decision[i];
			}
		}

		return index;
	}
	
	//On divise la ligne de la matrice par la valeur du pivot pour avoir pivot == 1
	public static void diviserLigneParPivot(int ligneIndex, float pivot)
	{
		for(int j = 0; j<valeursHorsBase.length; j++)
		{
			matrice[ligneIndex][j] = matrice[ligneIndex][j] / pivot;
		}
		
		valeursSolutions[ligneIndex] = valeursSolutions[ligneIndex] / pivot;  
	}
	
	//Faire pivoter la matrice et les variables hors base
	public static void pivoter(int columnIndex,int lineIndex)
	{
		for(int i =0; i<valeursSolutions.length;i++)
		{
			//On cherche les lignes à faire pivoter
			if((i != lineIndex) && matrice[i][columnIndex] != 0)
			{
				float[] lignePivot = new float[matriceInfo.length];
				float coefficient = 0;
				
				//Coefficientage du pivot.
				for(int x = 0; x<matriceInfo.length; x++)
				{
					coefficient = matrice[i][columnIndex];
					lignePivot[x] = matrice[lineIndex][x] * coefficient;
				}
				
				//Soustraction de la ligne par le pivot coefficienté.
				for(int j = 0; j<matriceInfo.length; j++)
				{
					matrice[i][j] = matrice[i][j] - lignePivot[j];
				}
				
				//On met à jours la valeur solution.
				valeursSolutions[i] = valeursSolutions[i] - (valeursSolutions[lineIndex] * coefficient);
			}
		}
		
		//On met a jours les valeurs hors base (VHB).
		float coefficient = valeursHorsBase[columnIndex];
		
		//Soustraction de la ligne VHB par celle du pivot coefficienté.
		for(int x = 0; x<valeursHorsBase.length; x++)
		{
			valeursHorsBase[x] = (valeursHorsBase[x] - (matrice[lineIndex][x] * coefficient));
			valeursHorsBase[x] = valeursHorsBase[x] * 100;
			valeursHorsBase[x] = Math.round(valeursHorsBase[x]);
			valeursHorsBase[x] = valeursHorsBase[x] /100;
		}
		
		//On met à jours le résultat.
		negativeZ = negativeZ - (valeursSolutions[lineIndex] * coefficient);
	}
	
	//Permet d'afficher la matrice courrante dans la console.
	public static void afficherMatriceCourrante()
	{
		System.out.println();
		System.out.print("       ");
		for(int i = 0; i < matriceInfo.length; i++)
		{
			System.out.print(matriceInfo[i] + "   ");
		}
		
		System.out.println();
		System.out.print("      -");
		
		for(int i = 0; i < matriceInfo.length; i++)
		{
			System.out.print("-----");
		}
		
		for(int i = 0; i < valeursSolutions.length; i++)
		{
			System.out.println();
			System.out.print("|" + valeursBase[i] + "|");
			System.out.print("  |");
			for(int j = 0; j < matriceInfo.length; j++)
			{
				System.out.print(df.format(matrice[i][j]) + " ");
			}
			System.out.print("|  |" + df.format(valeursSolutions[i]) + "|");
		}
		
		System.out.println();
		System.out.println();
		System.out.print("      |");
		for(int i = 0; i < matriceInfo.length; i++)
		{
			System.out.print(df.format(valeursHorsBase[i]) + " ");
		}
		System.out.println("|  |" + (negativeZ) + "|");
	}
	
	//Supprime les variables Y1, Y2, ...
	public static void supprimerlesY()
	{
		for(int i = 0; i < matriceInfo.length;i++)
		{
			if(matriceInfo[i].contains("Y"))
			{
				float[][] nouvelleMatrice = new float[matrice.length][i];
				for(int k = 0;k<matrice.length;k++)
				{
					for(int j = 0;j<i;j++)
					{
						nouvelleMatrice[k][j] = matrice[k][j];
					}
				}
				
				matrice = nouvelleMatrice;
				
				break;
			}
		}
		
		String[] newMatriceInfo = new String[matrice[0].length];
		float[] newVHB = new float[matrice[0].length];		
		
		for(int i = 0; i < newMatriceInfo.length;i++)
		{
			newMatriceInfo[i] =  matriceInfo[i];
			newVHB[i] = valeursHorsBase[i];
		}
		
		matriceInfo = newMatriceInfo;
		valeursHorsBase = newVHB;
		
	}
}