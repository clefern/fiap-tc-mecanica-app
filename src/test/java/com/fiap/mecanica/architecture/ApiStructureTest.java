package com.fiap.mecanica.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RestController;

@AnalyzeClasses(
    packages = "com.fiap.mecanica",
    importOptions = {ImportOption.DoNotIncludeTests.class})
public class ApiStructureTest {

  @ArchTest
  static final ArchRule controllers_should_implement_api_interfaces =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .implement(
              com.tngtech.archunit.base.DescribedPredicate.describe(
                  "an interface ending with 'Api'",
                  interfaceClass -> interfaceClass.getName().endsWith("Api")))
          .because("Controllers must implement API interfaces to separate Swagger documentation");

  @ArchTest
  static final ArchRule api_interfaces_should_be_in_presentation_api_package =
      classes()
          .that()
          .haveSimpleNameEndingWith("Api")
          .and()
          .areInterfaces()
          .should()
          .resideInAPackage("..presentation.api..")
          .because("API interfaces must be located in the presentation.api package");

  @ArchTest
  static final ArchRule controllers_should_be_in_presentation_controller_package =
      classes()
          .that()
          .areAnnotatedWith(RestController.class)
          .should()
          .resideInAPackage("..presentation.controller..")
          .because("REST Controllers must be located in the presentation.controller package");
}
