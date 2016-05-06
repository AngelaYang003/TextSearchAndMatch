import javax.print.attribute.AttributeSet;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame; 
import javax.swing.JPanel; 
import javax.swing.JComboBox; 
import javax.swing.JButton; 
import javax.swing.JLabel; 
import javax.swing.JList; 
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import java.awt.BorderLayout; 
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionListener; 
import java.awt.event.ActionEvent;  
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class GUI {  //Note: Typically the main method will be in a //separate class. As this is a simple one class //example it's all in the one class. 

  private static String displayResults;
  
  private JTextArea textArea;
  private JTextArea patternArea;
  private JTextArea resultArea;
  private JTextPane resultPane;
  private String defaultText;
  private String defaultPatterns;

  public static void main(String[] args) { 
	  new GUI(); 
	  displayResults = "";
  }  
  
  public GUI() { 
	  
	  prepareDefaultText();
	  
	  prepareDefaultPatterns();
	  
	  JFrame guiFrame = new JFrame();  
      
      guiFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
      guiFrame.setTitle("Text Search and Match"); 
      guiFrame.setSize(1200,700);  
      guiFrame.setLocationRelativeTo(null); //center the window  
      
      /*
       * This section creates a text area for inputing the patters to be searched.
       * It has a label and textArea and a search button
       * When the search button is pressed, the event handler for this button
       * calls the run() method and start the Rolling Hash algorithm
       */
      
      Panel pattenPanel = new Panel();
      pattenPanel.setLayout(new BoxLayout(pattenPanel, BoxLayout.PAGE_AXIS));
         
	      //pattern text box
	      patternArea = new JTextArea(defaultPatterns, 1, 1);
	      patternArea.setLineWrap( true );
	      patternArea.setWrapStyleWord( true );
	      TitledBorder titlePattern;
	      titlePattern = BorderFactory.createTitledBorder("Patterns to be Searched");
	      titlePattern.setTitleJustification(TitledBorder.CENTER);
	      patternArea.setBorder(titlePattern);
	     
	      pattenPanel.add(patternArea);
          
	      //button
	      JButton searchButton = new JButton("Search");  
	      searchButton.addActionListener(new ActionListener() { 
	    	  @Override public void actionPerformed(ActionEvent event) { 
	    	  Map<Integer, String> matchReultsTable  = run();
			  displayResult(matchReultsTable);
			  } 
	      });  
	      
	      pattenPanel.add(searchButton);
      
      
      /*
       * This is the section that creates the "Text to be Searched" text area in the
       * middle of the screen. It is pre-populated with some defaults sentences. But it allows 
       * user to copy and paste anything text. For example, find some text on any website and 
       * paste it here.
       */
	  Panel textPanel = new Panel();
	  textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.PAGE_AXIS));
	  	
	      //text area
	      textArea = new JTextArea(defaultText, 5, 10);
	      textArea.setPreferredSize(new Dimension(100, 100));
	      textArea.setLineWrap(true);
	      
	      TitledBorder titleText;
	      titleText = BorderFactory.createTitledBorder("Text to be Searched");
	      titleText.setTitleJustification(TitledBorder.CENTER);
	      textArea.setBorder(titleText);
	      
	      JScrollPane scrollTextPane = new JScrollPane();
	      scrollTextPane.add(textArea);
	      scrollTextPane.setPreferredSize(new Dimension(100, 100));
	      textPanel.add(textArea);
	      //textPanel.add(scrollTextPane);
	    
	      
	      
      /*
       * This is the section that creates the display area at button.
       * The original text is re-displayed here and the matched words are turned into
       * red font based on the output of the RollingHash algorithm
       */
      Panel resultPanel = new Panel();
      resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
      	
       
      	//result area
      	resultArea = new JTextArea("", 5, 10);
        resultArea.setPreferredSize(new Dimension(100, 100));
        resultArea.setLineWrap(true);
        TitledBorder titleResult;
        titleResult = BorderFactory.createTitledBorder("Search Results");
        titleResult.setTitleJustification(TitledBorder.CENTER);
        resultArea.setBorder(titleResult);
        
        resultPane = new JTextPane();                
        resultPane.setBorder(titleResult);
        resultPane.setMargin(new Insets(5, 5, 5, 5));
        
	      
        JScrollPane scrollResultPane = new JScrollPane();
        scrollResultPane.add(resultArea);
        resultPanel.add(resultPane);
        
      
      //add to frame
      Container contentPane = guiFrame.getContentPane();
      
      contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));
      contentPane.add(pattenPanel);
      contentPane.add(textPanel);
      contentPane.add(resultPanel);
     
      guiFrame.setVisible(true); 
     
  }  
  
  /*
   * Populate the the "Text to be Searched" textArea with some sentences
   */
  public  void prepareDefaultText() {
	  defaultText = new StringBuilder()
      .append("It was the best of times, it was the worst of times,\n")
      .append("it was the age of wisdom, it was the age of foolishness,\n")
      .append("it was the epoch of belief, it was the epoch of incredulity,\n")
      .append("it was the season of Light, it was the season of Darkness,\n")
      .append("it was the spring of hope, it was the winter of despair,\n")
      .append("we had everything before us, we had nothing before us")
      .toString();
  }
  
  /*
   * populate the "Patterns to be Searched" textarea with some
   * words
   */
  public void prepareDefaultPatterns() {
	  
	  defaultPatterns = "times wisdom belief before the age";
  }
  
  /*
   * This is the main method that takes string from "Patterns to be Matched" textarea
   * and split the string into individual patterns then stored in a hashSet.
   * It then calls the multiplePatternMatchSearch() on the core RollingHash class to activate
   * the pattern search algorithm.
   */
  public Map<Integer, String> run() {
      
      String text = textArea.getText().toLowerCase();
      
      String patternText = patternArea.getText();
      
      if (patternText.length() > 0) {
    	  
    	  
    	  //Clean up the pattern string a bit. Remove common special characters
    	  patternText = patternText.replace(",", "");
    	  patternText = patternText.replace(".", "");
    	  patternText = patternText.replace("\"", "");
    	  patternText = patternText.replace("'", "");
    	  patternText = patternText.replace("!", "");
    	  patternText = patternText.replace("[", "");
    	  patternText = patternText.replace("]", "");
    	  patternText = patternText.replace("(", "");
    	  patternText = patternText.replace(")", "");
    	  
    	  
    	  //Splitting it into individual patterns by space
    	  String[] patterns =  patternText.split(" ");
    	  
    	  //parse out each pattern and store them in a HashSet
    	  Set<String> patternSet = new HashSet<String>();
    	  for (String pattern : patterns) {
    		  //don't add whitespace, "a", "of", "the", "is", "The"
    		  if (pattern != null && pattern.trim().length() > 1 
    		      && !pattern.equalsIgnoreCase("a")
    		      && !pattern.equalsIgnoreCase("or")
    		      && !pattern.equalsIgnoreCase("at")
    		      && !pattern.equalsIgnoreCase("at")
    		      && !pattern.equalsIgnoreCase("on")
    		      && !pattern.equalsIgnoreCase("to")
    		      && !pattern.equalsIgnoreCase("of")
    		      && !pattern.equalsIgnoreCase("for")
    		      && !pattern.equalsIgnoreCase("the")
    		      && !pattern.equalsIgnoreCase("is")
    		      && !pattern.equalsIgnoreCase("this")
    		      && !pattern.equalsIgnoreCase("that")
    		      && !pattern.equalsIgnoreCase("am")
    		      && !pattern.equalsIgnoreCase("are")
    		      && !pattern.equalsIgnoreCase("was")
    		      && !pattern.equalsIgnoreCase("were")
    		      && !pattern.equalsIgnoreCase("there")
    		      
    		      ) {
    			  patternSet.add(pattern.trim().toLowerCase());
    			  System.out.println("pattern added: " + pattern.trim().toLowerCase());
    		  }
    	  }
    	  
          //call constructor of core RollingHash class to create a match searcher object
          
          RollingHash multiplePatternSearch = new RollingHash(patternSet);
          
          //call search on the entire text and pass in the pattern set
          
          Map<Integer, String> results =  multiplePatternSearch.multiplePatternMatchSearch(text);
          
          //set displayResults to original text first.Later the matched words will be turn into red font
          displayResults = text;
          
          return results;
      }
      
      return null;
  }

  /*
   * This method go thru the original text and match results to mark the matched words in red font.
   * If it finds a match at a location, it turns the font to red.
   */
  private void displayResult(Map<Integer, String> matchResultTable) {
	  
	  StyleContext sc = StyleContext.getDefaultStyleContext();
      javax.swing.text.AttributeSet asetRed = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
      javax.swing.text.AttributeSet asetBlack = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);

      asetRed = sc.addAttribute(asetRed, StyleConstants.FontFamily, "Lucida Console");
      asetRed = sc.addAttribute(asetRed, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

      asetBlack = sc.addAttribute(asetBlack, StyleConstants.FontFamily, "Lucida Console");
      asetBlack = sc.addAttribute(asetBlack, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);
      
      resultPane.setText(displayResults);
      
      StyledDocument sdoc = resultPane.getStyledDocument();
      
      //loop thru the matchResultTable and turn the word into red font 
      Iterator<Entry<Integer, String>> it =  matchResultTable.entrySet().iterator();
      while(it.hasNext()) {
    	  Map.Entry<Integer, String> matchedWord = it.next();
    	  sdoc.setCharacterAttributes(matchedWord.getKey(), matchedWord.getValue().length(), asetRed, false);
      }
  }
}
