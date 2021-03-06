/*
 * Copyright (c) 2015, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.protobuf.gradle

import org.apache.commons.lang.StringUtils
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.internal.file.FileResolver

class Utils {
  /**
   * Returns the conventional name of a configuration for a sourceSet
   */
  static String getConfigName(String sourceSetName, String type, String suffix = "") {
    return (sourceSetName == SourceSet.MAIN_SOURCE_SET_NAME ?
        type : (sourceSetName + StringUtils.capitalize(type))) + suffix
  }

  /**
   * Returns the conventional substring that represents the sourceSet in task names,
   * e.g., "generate<sourceSetSubstring>Proto"
   */
  static String getSourceSetSubstringForTaskNames(String sourceSetName) {
    return sourceSetName == SourceSet.MAIN_SOURCE_SET_NAME ?
        '' : StringUtils.capitalize(sourceSetName)
  }

  static boolean isAndroidProject(Project project) {
    return project.hasProperty('android') && project.android.sourceSets
  }

  /**
   * Creates a configuration if necessary for a source set so that the build
   * author can configure dependencies for it.
   */
  private static void createConfiguration(Project project, String sourceSetName, String suffix) {
    String configName = getConfigName(sourceSetName, 'protobuf', suffix)
    project.configurations.create(configName) {
      visible = false
      transitive = false
      extendsFrom = []
    }
  }

  /**
   * Adds the proto extension to all SourceSets, e.g., it creates
   * sourceSets.main.proto and sourceSets.test.proto.
   */
  static void addSourceSetExtensions(Object sourceSets, FileResolver fileResolver) {
    sourceSets.all { sourceSet ->
      sourceSet.extensions.create('proto', ProtobufSourceDirectorySet, sourceSet.name, fileResolver)
    }
  }

  static void setupSourceSets(Project project, Object sourceSets, String suffix = "") {
    addSourceSetExtensions(sourceSets, project.fileResolver)
    sourceSets.all { sourceSet ->
      createConfiguration(project, sourceSet.name, suffix)
    }
  }
}
