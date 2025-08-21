/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.build;

import org.gradle.api.Project;
import org.jetbrains.kotlin.gradle.dsl.JvmTarget;
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion;
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile;

import java.util.Arrays;

/**
 * @author Brian Clozel
 * @author Sebastien Deleuze
 */
public class KotlinConventions {

	void apply(Project project) {
		project.getPlugins().withId("org.jetbrains.kotlin.jvm",
				(plugin) -> project.getTasks().withType(KotlinCompile.class, this::configure));
	}

	private void setAdditionalCliOptions(KotlinCompile compile) {
		if (compile.getProject().hasProperty("kotlin_additional_cli_options")) {
			String cli = (String) compile.getProject().getProperties().get("kotlin_additional_cli_options");
			compile.getCompilerOptions().getFreeCompilerArgs().addAll(Arrays.stream(cli.trim().split(" ")).toList());
			System.out.println("Kotlin additional CLI args: " + cli + " in " + compile.getProject());
		}
	}

	private void setLanguageVersion(KotlinCompile compile) {
		if (compile.getProject().hasProperty("kotlin_language_version")) {
			String lv = (String) compile.getProject().getProperties().get("kotlin_language_version");
			compile.getCompilerOptions().getLanguageVersion().set(KotlinVersion.fromVersion(lv));
			System.out.println("Kotlin language version: " + lv + " in " + compile.getProject());
		}
	}

	private void setApiVersion(KotlinCompile compile) {
		if (compile.getProject().hasProperty("kotlin_api_version")) {
			String av = (String) compile.getProject().getProperties().get("kotlin_api_version");
			compile.getCompilerOptions().getApiVersion().set(KotlinVersion.fromVersion(av));
			System.out.println("Kotlin api version: " + av + " in " + compile.getProject());
		}
	}

	private void configure(KotlinCompile compile) {
		compile.compilerOptions(options -> {

			setLanguageVersion(compile);
			setApiVersion(compile);
			setAdditionalCliOptions(compile);

			options.getJvmTarget().set(JvmTarget.JVM_17);
			options.getJavaParameters().set(true);
			options.getFreeCompilerArgs().addAll(
					"-Xsuppress-version-warnings",
					"-Xjsr305=strict", // For dependencies using JSR 305
					"-opt-in=kotlin.RequiresOptIn",
					"-Xjdk-release=17", // Needed due to https://youtrack.jetbrains.com/issue/KT-49746
					"-Xreport-all-warnings",
					"-Xrender-internal-diagnostic-names",
					"-Xannotation-default-target=param-property"
			);
			options.getAllWarningsAsErrors().set(false); // KT-75078
		});
	}

}
