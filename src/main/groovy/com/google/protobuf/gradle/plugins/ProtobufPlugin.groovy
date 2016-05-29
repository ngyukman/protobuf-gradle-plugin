/*
 * Original work copyright (c) 2015, Alex Antonov. All rights reserved.
 * Modified work copyright (c) 2015, Google Inc. All rights reserved.
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

package com.google.protobuf.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.AppliedPlugin

class ProtobufPlugin implements Plugin<Project> {
    // any one of these plugins should be sufficient to proceed with applying this plugin
    private static final List<String> javaPluginOptions = ['java']
    private static final List<String> csharpPluginOptions = ['msbuild']
    private static final List<String> androidPluginOptions = ['com.android.application',
                                                              'com.android.library',
                                                              'android',
                                                              'android-library']


    private List<String> pluginsApplied = [];
    private Project project;

    void apply(final Project project) {
        this.project = project

        // At least one of the prerequisite plugins must by applied before this plugin can be applied, so
        // we will use the PluginManager.withPlugin() callback mechanism to delay applying this plugin until
        // after that has been achieved. If project evaluation completes before one of the prerequisite plugins
        // has been applied then we will assume that none of prerequisite plugins were specified and we will
        // throw an Exception to alert the user of this configuration issue.
        applyWithPrerequisitePlugin(javaPluginOptions, 'com.google.protobuf.java')
        applyWithPrerequisitePlugin(csharpPluginOptions, 'com.google.protobuf.csharp')
        applyWithPrerequisitePlugin(androidPluginOptions, 'com.google.protobuf.android')

        project.afterEvaluate {
            if (pluginsApplied.empty) {
                throw new GradleException('The com.google.protobuf plugin could not be applied during project evaluation.'
                        + ' The Java plugin or one of the Android plugins must be applied to the project first.')
            }
        }
    }

    void applyWithPrerequisitePlugin(List<String> possiblePluginNames, String pluginToBeApplied) {
        possiblePluginNames.each { pluginName ->
            project.pluginManager.withPlugin(pluginName, { prerequisitePlugin ->
                applyWithPrerequisitePlugin (prerequisitePlugin, pluginToBeApplied)
            })
        }
    }

    void applyWithPrerequisitePlugin(AppliedPlugin prerequisitePlugin, String pluginToBeApplied) {
        if (pluginToBeApplied in pluginsApplied) {
            project.logger.warn('The com.google.protobuf plugin was already applied to the project: ' + project.path
                    + ' and will not be applied again after plugin: ' + prerequisitePlugin.id)
        } else {
            pluginsApplied.add pluginToBeApplied
            project.apply plugin: pluginToBeApplied
        }
    }
}
