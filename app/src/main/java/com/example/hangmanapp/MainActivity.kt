package com.example.hangmanapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.vector.VectorProperty
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.Collections
import java.util.Scanner

class MainActivity : ComponentActivity() {

    // declared values
    private val txtWordToBeGuessed: TextView = findViewById<TextView>(R.id.txtWordToBeGuessed)
    private val edtInput: EditText = findViewById<EditText>(R.id.edtInput)
    private val txtLettersTried: TextView = findViewById<TextView>(R.id.txtLettersTried)
    private val txtTriesLeft: TextView = findViewById<TextView>(R.id.txtTriesLeft)
    private val rotateAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
    private val scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale)
    private val scaleAndRotateAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_and_rotate)
    private val trReset = findViewById<TableRow>(R.id.trReset)
    private val trTriesLeft = findViewById<TableRow>(R.id.trTriesLeft)

    private val WINNING_MESSAGE = "You Won!"
    private val LOSING_MESSAGE = "You Lost."
    private val MESSAGE_WITH_LETTERS_TRIED = "Letters tried: "

    private val textWatcher = object: TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            TODO("Not needed")
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int){
            // if there is some letter on the input field
            if (s != null)
                if (s.isNotEmpty())
                    checkIfLetterIsInWord(s[0])
        }

        override fun afterTextChanged(s: Editable?) {
            TODO("Not needed")
        }
    }

    // declared variables
    private var wordDisplayedCharArray: CharArray = charArrayOf()
    var myListOfWords = ArrayList<String>()
    private var wordToBeGuessed = ""
    var wordDisplayedString = ""
    var lettersTried = ""
    var triesLeft = ""


    private fun revealLetterInWord(letter: Char){
        var indexOfLetter = wordToBeGuessed.indexOf(letter)

        // loop if index is positive or 0
        while (indexOfLetter >= 0){
            wordDisplayedCharArray[indexOfLetter] = wordToBeGuessed[indexOfLetter]
            indexOfLetter = wordToBeGuessed.indexOf(letter, indexOfLetter + 1)
        }

        // update the string as well
        wordDisplayedString = "$wordDisplayedCharArray"
    }

    private fun displayWordOnScreen(){
        var formattedString = ""
        for (c in wordDisplayedCharArray){
            formattedString += "$c "
        }
        txtWordToBeGuessed.text = formattedString
    }

    private fun checkIfLetterIsInWord(letter: Char){
        // if the letter was found inside the word to be guessed
        if (wordToBeGuessed.indexOf(letter) >= 0){
            // animate
            txtWordToBeGuessed.startAnimation(scaleAnimation)

            // if the letter was not displayed yet
            if(wordDisplayedString.indexOf(letter) < 0){
                // replace underscores with that letter & update changes on screen
                revealLetterInWord(letter)
                displayWordOnScreen()

                // check if game is won
                if (!wordDisplayedString.contains('_')){
                    trTriesLeft.startAnimation(scaleAndRotateAnimation)
                    txtLettersTried.text = WINNING_MESSAGE
                }
            }
        }
        // otherwise, if the letter was not found inside the word to be guessed
        else{
            // decrease number of tries, and show it on screen
            decreaseAndDisplayTriesLeft()

            // check if the game is lost
            if (triesLeft.isEmpty()){
                trTriesLeft.startAnimation(scaleAndRotateAnimation)
                txtTriesLeft.text = LOSING_MESSAGE
                txtWordToBeGuessed.text = wordToBeGuessed
            }
        }
        // if the letter that was tried is new
        if (lettersTried.indexOf(letter) < 0){
            lettersTried += "$letter, "
            var messageToBeDisplayed = MESSAGE_WITH_LETTERS_TRIED + lettersTried
            txtLettersTried.text = messageToBeDisplayed
        }
    }

    private fun decreaseAndDisplayTriesLeft(){
        // if there are still tries left
        if (triesLeft.isNotEmpty()){
            // animate
            txtTriesLeft.startAnimation(scaleAnimation)

            // take out the last 2 characters/remove a try
            triesLeft = triesLeft.substring(0, triesLeft.length - 2)
        }
    }

    private fun initializeGame() {
        // 1.WORD
        // Shuffle array list and get first element, and then remove it
        myListOfWords.shuffle()
        wordToBeGuessed = myListOfWords[0]
        myListOfWords.removeAt(0)

        // initialize char array
        wordDisplayedCharArray = wordToBeGuessed.toCharArray()

        // add underscores
        for (i in 1..wordDisplayedCharArray.size - 2)
            wordDisplayedCharArray[i] = '_'

        // reveal all occurrences of first and last character
        revealLetterInWord(wordDisplayedCharArray[0])
        revealLetterInWord(wordDisplayedCharArray[wordDisplayedCharArray.size - 1])

        // initialize a string from this char array (for searching purposes)
        wordDisplayedString = "$wordDisplayedCharArray"

        // display word
        displayWordOnScreen()

        // 2.INPUT
        // clear input field
        edtInput.setText("")

        //3.LETTERS TRIED
        // initialize string for letters tried with a space and display on screen
        lettersTried = " "
        txtLettersTried.text = MESSAGE_WITH_LETTERS_TRIED

        // 4.TRIES LEFT
        // initialize the string for tries left
        triesLeft = " X X X X X X"
        txtTriesLeft.text = triesLeft
    }

    private fun resetGame(v: View){
        // start and clear animation
        trReset.startAnimation(rotateAnimation)
        trTriesLeft.clearAnimation()

        // setup a new game
        initializeGame()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scaleAndRotateAnimation.fillAfter = true

        //for debugging
//        var testerWord = ""

        // traverse database file and populate array list
        var myInputStream: InputStream? = null
        var scannerVariable: Scanner? = null
        try {
            var myInputStream: InputStream = File ("database_file.txt").inputStream()
            var scannerVariable = Scanner(myInputStream)
            while (scannerVariable.hasNext()){
//                testerWord = scannerVariable.next()
//                myListOfWords.add(testerWord)
                myListOfWords.add(scannerVariable.next())
//                Toast.makeText(this@MainActivity, testerWord, Toast.LENGTH_SHORT).show()
            }

        } catch (e: IOException){
            Toast.makeText(this@MainActivity, e.javaClass.simpleName + ": " + e.message, Toast.LENGTH_SHORT).show()
        }
        finally {
            // close scanner
            if (scannerVariable != null)
                scannerVariable.close()
            // close InputStream
            try {
                if (myInputStream != null)
                    myInputStream.close()
            } catch (e: IOException){
                Toast.makeText(this@MainActivity, e.javaClass.simpleName + ": " + e.message, Toast.LENGTH_SHORT).show()
            }
        }
        initializeGame()

        // setup the text changed listener for the edit text
        edtInput.addTextChangedListener(textWatcher)
    }
}
