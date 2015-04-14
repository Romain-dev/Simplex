import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.io.IOException;
import java.math.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.text.DecimalFormat;

public class Main {

	public static String[] valeursBase;
	public static String[] matriceInfo;
	public static float[][] matrice;
	public static float[] valeursSolutions;
	public static float[] valeursHorsBase;
	public static float negativeZ = 0;	

	public static DecimalFormat df = new DecimalFormat("0.00");
	
	public static void main(String[] args) {
		//Cette partie correspond � l'int�raction utilisateurs
		//pour r�cup�rer les contraintes et la fonction �conomique.
		Scanner sc = new Scanner(System.in);
		
		boolean isValidExpression = false;
		
		while(!isValidExpression)
		{
			System.out.println("Veuillez entrer vos contraintes � la forme \"3X1 + 1X2 <= 30\".");
			System.out.println("Entrez les � la suite, valider chacunes aves la touche ENTRER:");
			
			String contrainte = "empty";
			ArrayList<String> contraintes = new ArrayList<String>();
			
			//L'utilisateur peut entrer 'n' contraintes.
			while(!contrainte.equals(""))
			{
				contrainte = sc.nextLine();
				if(!contrainte.equals(""))
				{
					contraintes.add(contrainte);
					System.out.println("Contrainte suivante, validez directement pour passer � la fonction �conomique:");
				}
			}
	
			//L'utilisateur entre la fonction �conomique.
			System.out.println("Veuillez entrer votre fonction �conomique � la forme \"8X1 + 6X2\".");
			String fonctionEconomique = sc.nextLine();
			
			//Transformation des entr�es de l'utlisateur en donn�es exploitables par Simplex.
			ConstraintAdapter contraintesAdapter = new ConstraintAdapter(contraintes, fonctionEconomique);
			valeursBase = contraintesAdapter.valeursBase;
			matriceInfo = contraintesAdapter.matriceInfo;
			matrice = contraintesAdapter.matrice;
			valeursSolutions = contraintesAdapter.valeursSolutions;
			valeursHorsBase = contraintesAdapter.valeursHorsBase;
			
			isValidExpression = contraintesAdapter.isValid;
		}
		
		//Sert � afficher le nombre de r�cursions de l'algorithme Simplex.
		int nbTours = 0;
			
		//Affichage de la matrice de d�part.
		System.out.println("Simplex de d�part");
		afficherMatriceCourrante();
		System.out.println();
		
		//La boucle continue tant qu'il reste des variables hors bases positives.
		while(getVHBexploitableIndex() >= 0)
		{
			//On r�cup�re la position du pivot dans la matrice.
			int indexOfPivotColomn = getVHBexploitableIndex();
			int indexOfPivotLine = getPivotLineIndex(indexOfPivotColomn);
			
			float pivot = matrice[indexOfPivotLine][indexOfPivotColomn];
			
			//On divise la ligne par la valeur du pivot
			diviserLigneParPivot(indexOfPivotLine,pivot);
			
			//On fait entrer une nouvelle variable dans la base
			valeursBase[indexOfPivotLine] = matriceInfo[indexOfPivotColomn];
			
			//On fait le pivot sur toutes les lignes o� la colonne du pivot et != 0
			//On modifi �galement la fonction �conomique.
			pivoter(indexOfPivotColomn,indexOfPivotLine);		
			
			//Affichage de la matrice
			nbTours++;
			System.out.println("Passage n�" + nbTours);
			afficherMatriceCourrante();
			
			System.out.println(""); System.out.println("");
		}

		//Affichage du r�sultat final.
		System.out.println("Resultat: Z = " + (negativeZ*-1));
	}
	
	//Premiere �tape, on cherche une valeur hors base positive
	public static int getVHBexploitableIndex()
	{
		int index = -1;

		//Cherche l'index correspondant � la valeur max.
		for(int i = 0; i<valeursHorsBase.length; i++)
		{
			if(index == -1)
			{
				if(valeursHorsBase[i] > 0)
					index = i;
			}
			else if(valeursHorsBase[i] > valeursHorsBase[i-1])
			{
				if(valeursHorsBase[i] > 0)
					index = i;
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
			if(matrice[i][column] == 0)
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
			//On cherche les lignes � faire pivoter
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
				
				//Soustraction de la ligne par le pivot coefficient�.
				for(int j = 0; j<matriceInfo.length; j++)
				{
					matrice[i][j] = matrice[i][j] - lignePivot[j];
				}
				
				//On met � jours la valeur solution.
				valeursSolutions[i] = valeursSolutions[i] - (valeursSolutions[lineIndex] * coefficient);
			}
		}
		
		//On met a jours les valeurs hors base (VHB).
		float coefficient = valeursHorsBase[columnIndex];
		
		//Soustraction de la ligne VHB par celle du pivot coefficient�.
		for(int x = 0; x<valeursHorsBase.length; x++)
		{
			valeursHorsBase[x] = valeursHorsBase[x] - (matrice[lineIndex][x] * coefficient);
		}
		
		//On met � jours le r�sultat.
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
		System.out.println("|  |" + df.format(negativeZ) + "|");
	}
}