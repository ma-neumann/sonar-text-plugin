package org.sonar.plugins.txt.checks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;

@Rule(key = "SimpleRegexMatchCheck",
      name = "Simple Regex Match",
      description = "Simple regular expression matcher.",
      tags = { "bad-practice" })
public class SimpleTextMatchCheck extends AbstractTextCheck {

  @RuleProperty(key = "expression", type = "TEXT", defaultValue = EXPRESSION_SINGLELINE_DEFAULT, description = "Don't try to match to newlines (\\r or \\n); consider using the multiline check type if you have that need.\n\nThis rule type evaluates your pattern against a single line of text at a time and uses java.io.LineNumberReader.readLine() to obtain that text.")
  private String expression;

  @RuleProperty(key = "filePattern", defaultValue = FILEPATTERN_DEFAULT, description = FILEPATTERN_DESCRIPTION)
  private String filePattern = FILEPATTERN_DEFAULT;

  @RuleProperty(key = "message", description = MESSAGE_DESCRIPTION)
  private String message = MESSAGE_DEFAULT;

  public String getExpression() {
    return expression;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getMessage() {
    return message;
  }

  public void setExpression(final String expression) {
    this.expression = expression;
  }

  public void setFilePattern(final String filePattern) {
    this.filePattern = filePattern;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public void validate(final TextSourceFile textSourceFile, final String projectKey) {
    setTextSourceFile(textSourceFile);
    if (expression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      Pattern regexp = Pattern.compile(expression);
      Matcher matcher = regexp.matcher(""); // Apply the pattern to search this empty string just to get a matcher reference. We'll reset it in a moment to work against a real string.

      File inputFile = textSourceFile.getInputFile().file();
      CharsetDecoder decoder = (StandardCharsets.UTF_8).newDecoder();
      decoder.onMalformedInput(CodingErrorAction.IGNORE);

      try (LineNumberReader lineReader = new LineNumberReader(new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), decoder)));
          ) {
    	      String line = null;
    	      while ((line = lineReader.readLine()) != null) {
    	        matcher.reset(line); // Reuse the matcher by discarding its current state and providing new input text
    	        if (matcher.find()) {
    	          createViolation(lineReader.getLineNumber(), message);
    	        }
    	      }
    	    }
    	    catch (IOException ex){
    	      throw new RuntimeException(ex);
    	    }
    }
  }
}
