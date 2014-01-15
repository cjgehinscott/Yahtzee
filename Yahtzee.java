/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.Arrays;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {
	
	/* Private instance variables */
	private int nPlayers; //number of Players 
	private String[] playerNames; //array containing players names
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator(); //Random Number Generator
	private int scoreCard [] []; //an array to keep score for each players categories
	private int diceCategories [][]; //array of all the dice combinations for winning points
	private int[] diceResults = new int[N_DICE];//keeps track of the last dice rolls
	private int bonusValue = 35;
	private int bonusLevel = 63;
	
	
	
	

	
	public void run() {
		setupPlayers();
		initDisplay();
		playGame();
	}
	
	/**
	 * Prompts the user for information about the number of players, then sets up the
	 * players array and number of players.
	 */
	private void setupPlayers() {
		nPlayers = chooseNumberOfPlayers();	
		
		/* Set up the players array by reading names for each player. */
		playerNames = new String[nPlayers];
		for (int i = 0; i < nPlayers; i++) {
			/* IODialog is a class that allows us to prompt the user for information as a
			 * series of dialog boxes.  We will use this here to read player names.
			 */
			IODialog dialog = getDialog();
			playerNames[i] = dialog.readLine("Enter name for player " + (i + 1));
		}
	}
	
	/**
	 * Prompts the user for a number of players in this game, reprompting until the user
	 * enters a valid number.
	 * 
	 * @return The number of players in this game.
	 */
	private int chooseNumberOfPlayers() {
		/* See setupPlayers() for more details on how IODialog works. */
		IODialog dialog = getDialog();
		
		while (true) {
			/* Prompt the user for a number of players. */
			int result = dialog.readInt("Enter number of players");
			
			/* If the result is valid, return it. */
			if (result > 0 && result <= MAX_PLAYERS)
				return result;
			
			dialog.println("Please enter a valid number of players.");
		}
	}
	
	/**
	 * Sets up the YahtzeeDisplay associated with this game.
	 */
	private void initDisplay() {
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
	}

	/**
	 * Actually plays a game of Yahtzee.  This is where you should begin writing your
	 * implementation.
	 */
	private void playGame() {
		scoreCard = new int[nPlayers][N_CATEGORIES]; //keeps track of player scores
		
		diceCategories = new int[nPlayers][N_CATEGORIES]; // keeps track of categories that have been finished
		
		//loops through each players turn based on the number of players entered
		for(int i=1; i<=N_SCORING_CATEGORIES; i++){
			for(int j=1; j<=nPlayers; j++){
				firstRoll(j);
				nextRoll(j);
				updateCategory(j);
			}
		}
		//Results and Winner are calculated in gameOver
		gameOver();	
	}
	
	/** prompts player to make first roll and then displays 
	 * the result after the role
	 */
	private void firstRoll(int playerNumber) {
		display.printMessage(playerNames[playerNumber-1] + "'s turn to roll, Click the 'Roll the Dice' button");
		display.waitForPlayerToClickRoll(playerNumber);
		rollDice();
		display.displayDice(diceResults);
	}
	
	/**Gives the dice a random value from 1 to 6 through 
	 * the use of the random number generator
	 */
	private void rollDice(){
		for(int i=0; i<N_DICE; i++){
			diceResults[i] = rgen.nextInt(1,6);
		}
		
	}
	
	//Overloaded method to keep track of individual die during re-roll
	//Courtesy of dhbikoff from his gitHub solution
	private void rollDice(int die){
		diceResults[die] = rgen.nextInt(1,6); 
	}
	
	/**For the next rolls the player must select which dice they 
	 * want to reroll and then after they are rolled the results
	 * of those dice are stored in the diceResults array 
	 */
	private void nextRoll(int playerNumber){
		for(int i=0; i<2; i++){
			display.printMessage(playerNames[playerNumber-1] + " select the dice you wish to re-roll and click the " + "\"Roll Again\"" + " button to reroll.");
			display.waitForPlayerToSelectDice();
			for(int j=0; j<N_DICE; j++){
				if(display.isDieSelected(j) == true){
					rollDice(j);
				}
			}
			display.displayDice(diceResults);
		}
	}
	
	/**Prompts player to select score category and ensures that
	 * each category is only used once. Updates the display score 
	 * to reflect category selected
	 */
	private void updateCategory(int playerNumber){
		display.printMessage(playerNames[playerNumber-1] + " choose a category for your roll.");
		while(true){
			int selectedCategory = display.waitForPlayerToSelectCategory();
			//Checks to see if the category selected is empty
			if(diceCategories[playerNumber-1] [selectedCategory-1] == 0){
				//check for valid dice values in selected category
				boolean valid = validCategory(diceResults,selectedCategory);
				//tallies score based on correct/incorrect category selected
				int score = calculateScore(selectedCategory, diceResults, valid);
				//record a finished category
				diceCategories[playerNumber-1][selectedCategory-1] = 1;
				//update the scorecard
				addScorecard(selectedCategory,playerNumber,score);
				display.updateScorecard(selectedCategory,playerNumber,score);
				break;
			}
			display.printMessage("The category you have selected has already been used. Please select another.");
		}
	}
	
	//Checks the validity of the selected Category
	private boolean validCategory(int[]diceValues, int category){
		if(category == THREE_OF_A_KIND || category == FOUR_OF_A_KIND || category == YAHTZEE){
			return checkThreeFourYahtzee(diceValues, category);
		}else if (category == FULL_HOUSE){
			return checkFullHouse(diceValues);
		}else if (category == SMALL_STRAIGHT || category == LARGE_STRAIGHT){
			return checkStraight(diceValues, category);
		}
		return true;
	}
	
	//checks to see if the dice combination is 3 of a kind, 4 of a kind, and a Yahtzee
	private boolean checkThreeFourYahtzee(int[]diceValues, int category){
		//tracks to see if dice are equal to eachother
		for(int i=0; i<N_DICE; i++){
			int count = 0;
			for(int j=0; j<N_DICE; j++){
				if(diceValues[i] == diceValues[j]){
					count++;
				}
			}
			
			if((category == THREE_OF_A_KIND && count >= 3) ||
					(category == FOUR_OF_A_KIND && count >= 4) ||
					(category == YAHTZEE && count >= N_DICE)){
				return true;
			}	
		}
		return false;
	}
	
	/*We need to check for 3 of a kind and a pair to equal a Full
	 * House.  I break this problem into two parts for easier
	 * following
	 */
	private boolean checkFullHouse(int[]diceValues){
		//checks for 3 of a kind
		boolean foundThree = false;
		//checks for a pair
		boolean foundTwo = false;
		
		for(int i = 0; i<N_DICE; i++){
			int count = 0;
			for(int j = 0; j<N_DICE; j++){
				if(diceValues[i]==diceValues[j]){
					count++;
				}
			}
			if(count == 3){
				foundThree = true;
			}else if(count == 2){
				foundTwo = true;
			}
		}
		if(foundThree && foundTwo == true){
			return true;
		}else{
			return false;
		}
	}
	private boolean checkStraight(int[]diceValues, int category){
		Arrays.sort(diceValues);
		int count = 0;
		for(int i = 0; i<N_DICE-1; i++){
			if(diceValues[i+1] - diceValues[i] == 1){
				count++;	
			}
		}
		if(category == SMALL_STRAIGHT && count >=3){
			return true;
		}else if(category == LARGE_STRAIGHT && count == 4){
			return true;
		}else{
			return false;
		}
	}
	private int calculateScore(int category,int[] dice, boolean valid){
		int score = 0;
		
		if(!valid){
			return score;
		}
		
		if(category >= ONES && category <=SIXES){
			for(int i = 0; i < N_DICE; i++){
				if(dice[i] == category){
					score += category;
				}
			}
		}else if(category == THREE_OF_A_KIND || category == FOUR_OF_A_KIND || 
				category == CHANCE){
			for(int i = 0; i < N_DICE; i++){
				score += category;
			}
		}else if(category == FULL_HOUSE){
			score = 25;
		}else if(category == SMALL_STRAIGHT){
			score = 30;
		}else if(category == LARGE_STRAIGHT){
			score = 40;
		}else if(category == YAHTZEE){
			score = 50;
		}else{
			display.printMessage("Score Error");
		}
		return score;
	} 
	private void addScorecard(int category, int playerNumber, int score){
		scoreCard[playerNumber-1][category-1] = score;
		int upper = 0;
		int lower = 0;
		int sum = 0;
		
		//add up upper score
		for(int i = ONES; i <= SIXES; i++){
			upper += scoreCard[playerNumber-1][i-1];
			display.updateScorecard(UPPER_SCORE, playerNumber , upper);
		}
		scoreCard[playerNumber-1][UPPER_SCORE - 1] = upper;
		
		//Check to see if Bonus needs to be added
		if(upper >= bonusLevel){
			display.updateScorecard(UPPER_BONUS, playerNumber, bonusValue);
			scoreCard[playerNumber-1][UPPER_BONUS-1] = bonusValue;
			display.updateScorecard(UPPER_BONUS, playerNumber, bonusValue);
		}
		
		//Add up Lower Score
		for(int j = THREE_OF_A_KIND; j <= CHANCE; j++){
			lower += scoreCard[playerNumber-1][j-1];
		}
		scoreCard[playerNumber-1][LOWER_SCORE-1] = lower;
		display.updateScorecard(LOWER_SCORE, playerNumber, lower);
		
		//Add up Upper Score, Lower Score, and Bonus
		sum = lower + upper + scoreCard[playerNumber-1][UPPER_BONUS-1];
		scoreCard[playerNumber-1][TOTAL-1] = sum;
		display.updateScorecard(TOTAL, playerNumber, sum);
	}
	
	//Announces the end of the game and the player with the highest score as the winner
	private void gameOver(){
		int player = 0;
		int hiScore = 0;
		for(int i = 1; i <= nPlayers; i++){
			if(scoreCard[i-1][TOTAL-1] > hiScore){
				player = i;
				hiScore = scoreCard[i-1][TOTAL-1];		
			}
		}
		
		//Announce the winner
		String winner = playerNames[player-1].toUpperCase();
		display.printMessage(winner + " wins with a total score of "
				+ scoreCard[player-1][TOTAL-1] + "!!!");
	}
}
