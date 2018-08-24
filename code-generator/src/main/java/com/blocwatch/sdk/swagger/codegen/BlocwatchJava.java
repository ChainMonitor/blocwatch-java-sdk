package com.blocwatch.sdk.swagger.codegen;

import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.AbstractJavaCodegen;
import io.swagger.models.HttpMethod;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.PropertyBuilder;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Code-generator for BlocWatch Java clients. */
public class BlocwatchJava extends AbstractJavaCodegen {

  private static final String GET_PARAM_REQUEST_SUFFIX = "Request";

  public BlocwatchJava() {
    outputFolder = "generated-code" + File.separator + "java";
    embeddedTemplateDir = templateDir = "blocwatch-java";
    invokerPackage = "io.swagger.client";
    artifactId = "swagger-java-client";
    apiPackage = "io.swagger.client.api";
    modelPackage = "io.swagger.client.model";

    additionalProperties.put("blocWatchUserAgent", "BlocWatch-Generated-Java-Client/1.0-SNAPSHOT");
  }

  @Override
  public void processOpts() {
    super.processOpts();

    final String invokerFolder = (sourceFolder + '/' + invokerPackage).replace(".", "/");
    final String authFolder = (sourceFolder + '/' + invokerPackage + ".auth").replace(".", "/");

    supportingFiles.add(
        new SupportingFile("apiException.mustache", invokerFolder, "ApiException.java"));
    supportingFiles.add(
        new SupportingFile("Configuration.mustache", invokerFolder, "Configuration.java"));
    supportingFiles.add(new SupportingFile("Pair.mustache", invokerFolder, "Pair.java"));

    supportingFiles.add(new SupportingFile("ApiClient.mustache", invokerFolder, "ApiClient.java"));
    supportingFiles.add(
        new SupportingFile("RFC3339DateFormat.mustache", invokerFolder, "RFC3339DateFormat.java"));

    supportingFiles.add(
        new SupportingFile("auth/ApiKeyAuth.mustache", authFolder, "ApiKeyAuth.java"));
    supportingFiles.add(
        new SupportingFile("auth/Authentication.mustache", authFolder, "Authentication.java"));
    supportingFiles.add(
        new SupportingFile("auth/HttpBasicAuth.mustache", authFolder, "HttpBasicAuth.java"));
    supportingFiles.add(new SupportingFile("auth/OAuth.mustache", authFolder, "OAuth.java"));
    supportingFiles.add(
        new SupportingFile("auth/OAuthFlow.mustache", authFolder, "OAuthFlow.java"));
  }

  @Override
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  @Override
  public String getName() {
    return "blocwatch-java";
  }

  @Override
  public String getHelp() {
    return "";
  }

  @Override
  public Map<String, Object> postProcessOperations(Map<String, Object> objs) {
    objs = super.postProcessOperations(objs);
    Map<String, Object> operations = (Map<String, Object>) objs.get("operations");
    if (operations != null) {
      List<CodegenOperation> ops = (List<CodegenOperation>) operations.get("operation");
      for (CodegenOperation operation : ops) {
        if (HttpMethod.valueOf(operation.httpMethod) == HttpMethod.GET) {
          String getOperationRequest = operation.operationIdCamelCase + GET_PARAM_REQUEST_SUFFIX;
          operation.vendorExtensions.put("x-blocwatch-GetMethodRequestType", getOperationRequest);

          List<Map<String, String>> imports = (List<Map<String, String>>) objs.get("imports");
          Map<String, String> newImport = new HashMap<>();
          newImport.put("import", String.format("%s.%s", modelPackage(), getOperationRequest));
          imports.add(newImport);

          Consumer<CodegenParameter> addVendorExtensions =
              param -> {
                Map<String, Object> accessor = new HashMap<>();
                accessor.put("accessor", String.format("get%s", camelize(param.paramName)));
                param.vendorExtensions.put("x-blocwatch-ParameterAccessor", accessor);
              };

          operation.allParams.forEach(addVendorExtensions);
          operation.queryParams.forEach(addVendorExtensions);
          operation.formParams.forEach(addVendorExtensions);
          operation.pathParams.forEach(addVendorExtensions);
        }
      }
    }
    return objs;
  }

  @Override
  public void preprocessSwagger(Swagger swagger) {
    for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
      Path path = entry.getValue();
      Operation getOperation = path.getGet();
      if (getOperation != null) {
        String requestModelName = getOperation.getOperationId() + GET_PARAM_REQUEST_SUFFIX;
        ModelImpl model = new ModelImpl();
        model.setName(requestModelName);

        for (Parameter parameter : getOperation.getParameters()) {
          Property property = null;
          if (parameter instanceof PathParameter) {
            PathParameter pathParam = (PathParameter) parameter;
            property = PropertyBuilder.build(pathParam.getType(), pathParam.getFormat(), null);
            property.setExample(pathParam.getExample());
            if (property instanceof ArrayProperty) {
              ArrayProperty arrayProperty = (ArrayProperty) property;
              arrayProperty.setItems(pathParam.getItems());
            }
          }

          if (parameter instanceof QueryParameter) {
            QueryParameter qp = (QueryParameter) parameter;
            property = PropertyBuilder.build(qp.getType(), qp.getFormat(), null);
            property.setExample(qp.getExample());

            if (property instanceof ArrayProperty) {
              ArrayProperty arrayProperty = (ArrayProperty) property;
              arrayProperty.setItems(qp.getItems());
            }
          }

          if (property != null) {
            property.setDescription(parameter.getDescription());
            property.setRequired(parameter.getRequired());
            property.setAllowEmptyValue(parameter.getAllowEmptyValue());
            property.setName(parameter.getName());

            model.addProperty(parameter.getName(), property);
          }
        }

        if (!model.getProperties().isEmpty()) {
          // Handle void / arg free methods.
          swagger.addDefinition(getOperation.getOperationId() + GET_PARAM_REQUEST_SUFFIX, model);
        }
      }
    }
    super.preprocessSwagger(swagger);
  }
}
