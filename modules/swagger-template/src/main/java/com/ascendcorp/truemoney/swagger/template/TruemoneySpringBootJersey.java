package com.ascendcorp.truemoney.swagger.template;

import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.SupportingFile;
import io.swagger.codegen.languages.AbstractJavaJAXRSServerCodegen;
import io.swagger.models.Operation;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TruemoneySpringBootJersey extends AbstractJavaJAXRSServerCodegen {

  private String resourceFolder = "src/main/resources";
  private String TRUEMONEY_TEMPLATE_DIRECTORY_NAME = "truemoney";

  public TruemoneySpringBootJersey() {
    super();

    sourceFolder = "src/gen/java";
    invokerPackage = "io.swagger.api";
    artifactId = "swagger-jaxrs-server";
    outputFolder = "generated-code/JavaJaxRS-Jersey";

    modelTemplateFiles.put("model.mustache", ".java");
    apiTemplateFiles.put("api.mustache", ".java");
    apiTemplateFiles.put("apiService.mustache", ".java");
    apiTemplateFiles.put("apiServiceImpl.mustache", ".java");
    apiPackage = "io.swagger.api";
    modelPackage = "io.swagger.model";

    additionalProperties.put("title", title);

    embeddedTemplateDir =
        templateDir = TRUEMONEY_TEMPLATE_DIRECTORY_NAME + File.separator + "springboot-jersey";

    for (int i = 0; i < cliOptions.size(); i++) {
      if (CodegenConstants.LIBRARY.equals(cliOptions.get(i).getOpt())) {
        cliOptions.remove(i);
        break;
      }
    }

    CliOption library =
        new CliOption(CodegenConstants.LIBRARY, "library template (sub-template) to use");
    library.setDefault(DEFAULT_LIBRARY);

    Map<String, String> supportedLibraries = new LinkedHashMap<String, String>();

    supportedLibraries.put(DEFAULT_LIBRARY, "Jersey core 2.22.1");
    library.setEnum(supportedLibraries);

    cliOptions.add(library);
    cliOptions.add(new CliOption(CodegenConstants.IMPL_FOLDER, CodegenConstants.IMPL_FOLDER_DESC));
    cliOptions.add(new CliOption("title", "a title describing the application"));
  }

  @Override
  public String getName() {
    return "truemoney-springboot-jersey";
  }

  @Override
  public String getHelp() {
    return "Generates a Springboot x Jersey";
  }

  @Override
  public void processOpts() {
    super.processOpts();

    if (additionalProperties.containsKey(CodegenConstants.IMPL_FOLDER)) {
      implFolder = (String) additionalProperties.get(CodegenConstants.IMPL_FOLDER);
    }

    supportingFiles.clear();
    writeOptional(outputFolder, new SupportingFile("pom.mustache", "", "pom.xml"));
    writeOptional(outputFolder, new SupportingFile("README.mustache", "", "README.md"));
    supportingFiles.add(new SupportingFile("ApiOriginFilter.mustache",
        (sourceFolder + '/' + invokerPackage + ".config").replace(".", "/"),
        "ApiOriginFilter.java"));
    writeOptional(outputFolder, new SupportingFile("SpringBootApplication.mustache",
        (implFolder + '/' + invokerPackage).replace(".", "/"), "Application.java"));
    writeOptional(outputFolder, new SupportingFile("JerseyConfig.mustache",
        (implFolder + '/' + invokerPackage + ".config").replace(".", "/"), "JerseyConfig.java"));
    writeOptional(outputFolder, new SupportingFile("SwaggerConfig.mustache",
        (implFolder + '/' + invokerPackage + ".config").replace(".", "/"), "SwaggerConfig.java"));
    writeOptional(outputFolder, new SupportingFile("appInfo.mustache",
        (resourceFolder).replace(".", "/"), "appInfo.properties"));
    writeOptional(outputFolder, new SupportingFile("logback-spring.mustache",
        (resourceFolder).replace(".", "/"), "logback-spring.xml"));
    writeOptional(outputFolder, new SupportingFile("application-yml.mustache",
        (resourceFolder + '/' + "config"), "application.yml"));
    writeOptional(outputFolder, new SupportingFile("application-dev-yml.mustache",
        (resourceFolder + '/' + "config"), "application-dev.yml"));

    if (additionalProperties.containsKey("dateLibrary")) {
      setDateLibrary(additionalProperties.get("dateLibrary").toString());
      additionalProperties.put(dateLibrary, "true");
    }

    if ("joda".equals(dateLibrary)) {
      supportingFiles.add(new SupportingFile("JodaDateTimeProvider.mustache",
          (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaDateTimeProvider.java"));
      supportingFiles.add(new SupportingFile("JodaLocalDateProvider.mustache",
          (sourceFolder + '/' + apiPackage).replace(".", "/"), "JodaLocalDateProvider.java"));
    } else if ("java8".equals(dateLibrary)) {
      supportingFiles.add(new SupportingFile("LocalDateTimeProvider.mustache",
          (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateTimeProvider.java"));
      supportingFiles.add(new SupportingFile("LocalDateProvider.mustache",
          (sourceFolder + '/' + apiPackage).replace(".", "/"), "LocalDateProvider.java"));
    } else {
      writeOptional(outputFolder,
          new SupportingFile("ObjectMapperContextResolver.mustache",
              (implFolder + '/' + invokerPackage + ".config").replace(".", "/"),
              "ObjectMapperContextResolver.java"));
    }
  }

  @Override
  public void addOperationToGroup(String tag, String resourcePath, Operation operation,
      CodegenOperation co, Map<String, List<CodegenOperation>> operations) {
    String basePath = resourcePath;
    if (basePath.startsWith("/")) {
      basePath = basePath.substring(1);
    }
    int pos = basePath.indexOf("/");
    if (pos > 0) {
      basePath = basePath.substring(0, pos);
    }

    if (basePath == "") {
      basePath = "default";
    } else {
      if (co.path.startsWith("/" + basePath)) {
        co.path = co.path.substring(("/" + basePath).length());
      }
      co.subresourceOperation = !co.path.isEmpty();
    }
    List<CodegenOperation> opList = operations.get(basePath);
    if (opList == null) {
      opList = new ArrayList<CodegenOperation>();
      operations.put(basePath, opList);
    }
    opList.add(co);
    co.baseName = basePath;
  }
}