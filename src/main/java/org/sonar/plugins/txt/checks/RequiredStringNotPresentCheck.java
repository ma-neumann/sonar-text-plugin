package org.sonar.plugins.txt.checks;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.txt.checks.util.FileIOUtil;
import org.sonar.plugins.txt.checks.util.LargeFileEncounteredException;
import org.sonar.plugins.txt.checks.util.LineNumberFinderUtil;

@Rule(key = "RequiredStringNotPresentRegexMatchCheck",
      name = "Required String not Present",
      description = "Allows you to enforce \"When string 'A' is present string 'B' must also be present\". Raises an issue when text in the file matches to some 'trigger' regular expression but none match to a 'must exist' regular expression. Multi-line (Java Match.DOTALL) regular expression matcher.",
      tags = { "bad-practice" })
public class RequiredStringNotPresentCheck extends AbstractTextCheck {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractTextCheck.class);

  @RuleProperty(key = "triggerRegularExpression", type = "TEXT", defaultValue = EXPRESSION_MULTILINE_DEFAULT, description = EXPRESSION_MULTILINE_DESCRIPTION)
  private String triggerExpression;

  @RuleProperty(key = "mustExistRegularExpression", type = "TEXT", defaultValue = EXPRESSION_MULTILINE_DEFAULT, description = EXPRESSION_MULTILINE_DESCRIPTION)
  private String mustExistExpression;

  @RuleProperty(key = "filePattern", type = "TEXT", defaultValue = FILEPATTERN_DEFAULT, description = FILEPATTERN_DESCRIPTION)
  private String filePattern = FILEPATTERN_DEFAULT;

  @RuleProperty(key = "message", type = "TEXT", description = MESSAGE_DESCRIPTION)
  private String message = MESSAGE_DEFAULT;

  public String getExpression() {
    return triggerExpression;
  }

  public String getFilePattern() {
    return filePattern;
  }

  public String getMessage() {
    return message;
  }

  public void setTriggerExpression(final String expression) {
    this.triggerExpression = expression;
  }

  public void setMustExistExpression(final String mustExistExpression) {
    this.mustExistExpression = mustExistExpression;
  }

  public void setFilePattern(final String filePattern) {
    this.filePattern = filePattern;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  @Override
  public void validate(final TextSourceFile textSourceFile, final String projectKey) {
    boolean triggerMatchFound = false;
    int lineNumberOfTriggerMatch = -1;
    boolean mustExistMatchFound = false;

    setTextSourceFile(textSourceFile);

    if (triggerExpression != null && mustExistExpression != null &&
        isFileIncluded(filePattern) &&
        shouldFireForProject(projectKey) &&
        shouldFireOnFile(textSourceFile.getInputFile())
        ) {


      String entireFileAsString;
      String fileLocationDescriptor = textSourceFile.getInputFile().uri().toString();
      try (InputStream fileInputStream = textSourceFile.getInputFile().inputStream();) {
        entireFileAsString = FileIOUtil.readInputStreamToString(fileInputStream, MAX_CHARACTERS_SCANNED, fileLocationDescriptor);

        Pattern regexp = Pattern.compile(triggerExpression, Pattern.DOTALL);
        Matcher matcher = regexp.matcher(entireFileAsString);
        if (matcher.find()) {
//          System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
          int positionOfMatch = matcher.start();
          lineNumberOfTriggerMatch = LineNumberFinderUtil.countLines(entireFileAsString, positionOfMatch);
          triggerMatchFound = true;
        }

        regexp = Pattern.compile(mustExistExpression, Pattern.DOTALL);
        matcher = regexp.matcher(entireFileAsString);
        if (matcher.find()) {
//          System.out.println("Match: " + line + " on line " + lineReader.getLineNumber());
          mustExistMatchFound = true;
        }

        if (triggerMatchFound && !mustExistMatchFound) {
          createViolation(lineNumberOfTriggerMatch, message);
        }

      } catch (LargeFileEncounteredException ex) {
//        System.out.println("Skipping file. Text scanner (" + this.getClass().getSimpleName() + ") maximum file size ( " + (MAX_CHARACTERS_SCANNED-1) + " chars) encountered for file '" + textSourceFile.getInputFile().file().getAbsolutePath() + "'. Did not check this file AT ALL.");
        return;
      } catch (IOException e) {
        LOG.error("Skipping file '" + fileLocationDescriptor + "' due to unexpected exception.", e);
      }

    }
  }


}
