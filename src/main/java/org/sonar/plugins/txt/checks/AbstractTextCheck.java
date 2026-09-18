package org.sonar.plugins.txt.checks;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.WildcardPattern;
import org.sonar.check.RuleProperty;

public abstract class AbstractTextCheck {
  public static final int MAX_CHARACTERS_SCANNED = 500001;

  protected static final String EXPRESSION_SINGLELINE_DEFAULT = "^some single-line.*regex search string$";
  protected static final String EXPRESSION_MULTILINE_DEFAULT = "(?m)^some.*regex search string\\. dot matches all$";

  protected static final String EXPRESSION_SINGLELINE_DESCRIPTION = "Single-line regex (Java non Match.DOTALL). Don't try to match to newlines (\\r or \\n); consider using the multiline check type if you have that need.\n\nThis rule type evaluates your pattern against a single line of text at a time and uses java.io.LineNumberReader.readLine() to obtain that text.";
  protected static final String EXPRESSION_MULTILINE_DESCRIPTION = "Multi-line regex (Java Match.DOTALL). Only the first " + (MAX_CHARACTERS_SCANNED-1) + " characters of each file will be processed. Note that ^ and $ character matching is to beginning and end of file UNLESS you start your expression with (?m).";
  
  protected static final String EXPRESSION_SINLELINE_OR_MULTILINE_DESCRIPTION = "Single-line (Java non Match.DOTALL) or multi-line regex (Java Match.DOTALL) depending on flag 'applyExpressionToOneLineOfTextAtATime'. In multi-line mode, only the first " + (MAX_CHARACTERS_SCANNED-1) + " characters of each file will be processed. Also in multi-line mode, note that ^ and $ character matching is to beginning and end of file UNLESS you start your expression with (?m).";
  protected static final String EXPRESSION_SINLELINE_OR_MULTILINE_SELECTOR_DESCRIPTION = "Select this to feed the regular expression evaluator one line at a time. Uncheck it if your expression needs to 'see' multiple lines. When not checked only the first " + (MAX_CHARACTERS_SCANNED-1) + " characters of each file will be processed.";
  
  protected static final String FILEPATTERN_DEFAULT = "**/*";
  protected static final String FILEPATTERN_DESCRIPTION = "Ant Style path expression. To include all of the files in this project use '***/**'.\n\nFiles scanned will be limited by the values of 'sonar.sources' and 'sonar.exclusions'.";

  protected static final String MESSAGE_DEFAULT = "";
  protected static final String MESSAGE_DESCRIPTION = "Reason explaining why this text has been matched.";

  private RuleKey ruleKey;
  private TextSourceFile textSourceFile;

  @RuleProperty(key = "doNotFireForTheseProjectKeys", type = "TEXT", description = "Use to exclude certain projects from this rule.\n\nSample RegEx patterns: '^someMavenGroupIdPrefix' or 'someArtifactIdEndingDenotingSpecialProjectsToBeExcludedFromRule$'")
  private String doNotFireForProjectKeysRegex;

  @RuleProperty(key = "doNotFireForTheseFileNames", type = "TEXT", description = "Use to exclude certain file names from this rule.\n\nSample RegEx pattern: '^(local\\.properties|README.txt)$'")
  private String doNotFireForTheseFileNamesRegex;

  protected final void createViolation(final Integer linePosition, final String message) {
    textSourceFile.addViolation(new TextIssue(ruleKey, linePosition, message));
  }

  /**
   * Apply the Ant style file pattern to decide if the file is included
   */
  protected boolean isFileIncluded(final String filePattern) {
    return (filePattern == null) ? true :  WildcardPattern.create(filePattern).match(textSourceFile.getLogicalPath());
  }

  protected boolean shouldFireForProject(final String currentProjectKey) {
    if (doNotFireForProjectKeysRegex == null || "".equals(doNotFireForProjectKeysRegex.trim())) {
      return true;
    } else {
  	  Pattern regexp = Pattern.compile(doNotFireForProjectKeysRegex);
      Matcher matcher = regexp.matcher(currentProjectKey);
      return !matcher.find();
    }
  }

  protected boolean shouldFireOnFile(final InputFile currentFile) {
	  if (doNotFireForTheseFileNamesRegex == null || "".equals(doNotFireForTheseFileNamesRegex.trim())) {
	    return true;
	  } else {
	    Pattern regexp = Pattern.compile(doNotFireForTheseFileNamesRegex);
	    Matcher matcher = regexp.matcher(currentFile.filename());
	    return !matcher.find();
	  }
  }

  public final void setRuleKey(final RuleKey ruleKey) {
    this.ruleKey = ruleKey;
  }

  public RuleKey getRuleKey() {
    return this.ruleKey;
  }

  public void setDoNotFireForProjectKeysRegex(final String doNotFireForProjectKeysRegex) {
    this.doNotFireForProjectKeysRegex = doNotFireForProjectKeysRegex;
  }

  public void setDoNotFireForTheseFileNamesRegex(final String doNotFireForTheseFileNamesRegex) {
    this.doNotFireForTheseFileNamesRegex = doNotFireForTheseFileNamesRegex;
  }

  protected void setTextSourceFile(final TextSourceFile sourceFile) {
    this.textSourceFile = sourceFile;
  }

  public abstract void validate(TextSourceFile sourceFile, String projectKey);

  protected TextSourceFile getTextSourceFile() {
    return textSourceFile;
  }

}
