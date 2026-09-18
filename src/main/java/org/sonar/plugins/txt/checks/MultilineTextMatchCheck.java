package org.sonar.plugins.txt.checks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.txt.checks.util.FileIOUtil;
import org.sonar.plugins.txt.checks.util.LargeFileEncounteredException;
import org.sonar.plugins.txt.checks.util.LineNumberFinderUtil;

@Rule(key = "MultilineTextMatchCheck",
      name = "Multiline Regex Check",
      description = "Multi-line (Java Match.DOTALL) regular expression matcher.",
      tags = { "bad-practice" })
public class MultilineTextMatchCheck extends AbstractTextCheck {
  @RuleProperty(key = "regularExpression", type = "TEXT", defaultValue = EXPRESSION_MULTILINE_DEFAULT, description = EXPRESSION_MULTILINE_DESCRIPTION)
  private String expression;

  @RuleProperty(key = "filePattern", type = "TEXT", defaultValue = FILEPATTERN_DEFAULT, description = FILEPATTERN_DESCRIPTION)
  private String filePattern = FILEPATTERN_DEFAULT;

  @RuleProperty(key = "message", type = "TEXT", description = MESSAGE_DESCRIPTION)
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

  public void setSearchRegularExpression(final String expression) {
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
    int lineNumberOfTriggerMatch = -1;

    setTextSourceFile(textSourceFile);

    if (expression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {

      String entireFileAsString;
      try {
        entireFileAsString = FileIOUtil.readFileAsString(textSourceFile, MAX_CHARACTERS_SCANNED);
      } catch (LargeFileEncounteredException ex) {
        // The util class logs the fact that we're skipping this file...
        return;
      }

      Pattern regexp = Pattern.compile(expression, Pattern.DOTALL);
      Matcher matcher = regexp.matcher(entireFileAsString);
      if (matcher.find()) {
//        System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
        int positionOfMatchBegin = matcher.start();
//        int positionOfMatchEnd = matcher.end();
        lineNumberOfTriggerMatch = LineNumberFinderUtil.countLines(entireFileAsString, positionOfMatchBegin);
        createViolation(lineNumberOfTriggerMatch, message);
      }

    }
  }

}
