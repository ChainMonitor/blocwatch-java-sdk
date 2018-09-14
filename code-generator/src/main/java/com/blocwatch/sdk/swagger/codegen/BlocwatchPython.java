package com.blocwatch.sdk.swagger.codegen;

import io.swagger.codegen.CodegenType;
import io.swagger.codegen.languages.PythonClientCodegen;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/** CodeGen implementation for BlocWatch clients.
 *
 * This class provides minimal changes to the default python codegen:
 * <ol>
 *   <li>Recursive ADT imports have been removed from models (bitcoin query, etc).</li>
 *   <li>Datetime parsing in ApiClient is made customizable to handle millisecond timestamps.</li>
 *   <li>
 *     TODO: ApiClient resolution of string type to classes should not depend on modelPackage.
 *   </li>
 * </ol>
 */
public class BlocwatchPython extends PythonClientCodegen {

  private static final Pattern IMPORT_PATTERN =
      Pattern.compile(
          "from\\s+(?<packageName>[a-zA-Z0-9._-]+)\\s+import\\s+(?<className>[a-zA-Z0-9._-]+)");

  public BlocwatchPython() {
    this.embeddedTemplateDir = this.templateDir = "blocwatch-python";
  }

  @Override
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  @Override
  public String getName() {
    return "blocwatch-python";
  }

  @Override
  public String getHelp() {
    return "";
  }

  @Override
  public void processOpts() {
    super.processOpts();
  }

  @Override
  public String toModelImport(String name) {
    if (StringUtils.startsWith(name, "import")) {
      return name;
    } else {
      Matcher importMatcher = IMPORT_PATTERN.matcher(name);
      if (importMatcher.matches()) {
        return "import " + importMatcher.group("packageName");
      } else {
        return "import " + modelPackage() + "." + toModelFilename(name);
      }
    }
  }

  @Override
  public String getTypeDeclaration(String name) {
    return modelPackage() + "." + name;
  }
}
