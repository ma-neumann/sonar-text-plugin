package org.sonar.plugins.txt;

import org.sonar.api.Plugin;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.plugins.txt.batch.TextIssueSensor;

public final class TextPlugin implements Plugin {

  public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".properties,.txt,.md";
  public static final String FILE_SUFFIXES_KEY = "sonar.txt.file.suffixes";
  public static final String REPOSITORY_KEY = "txt";
  public static final String LANGUAGE_KEY = "txt";
  public static final String NAME = "Text (Community)";
  public static final String SONAR_WAY_PROFILE_NAME = "Sonar way";
  public static final String SONAR_WAY_JSON_FILE_PATH = "org/sonar/l10n/txt/default_quality_profile/Sonar_way_profile.json";

  @Override
  public void define(Context context) {
    context.addExtensions(
        PropertyDefinition.builder(TextPlugin.FILE_SUFFIXES_KEY)
            .name("File suffixes")
            .description("List of suffixes of files that trigger automatic activation of this plugin for projects (with default quality profile).<br><br>Note: All files in projects are available to this plugin.<br><br>Note: Must not use file suffixes used by other plugins.")
            .defaultValue(TextPlugin.FILE_SUFFIXES_DEFAULT_VALUE)
            .multiValues(true)
            .category(TextPlugin.NAME)
            .build(),
        TextIssueSensor.class,
        TextLanguage.class,
        TextRulesDefinition.class,
        TextSonarWayProfile.class
    );
  }
}
