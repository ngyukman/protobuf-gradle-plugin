package com.google.protobuf.gradle.plugins

import com.google.protobuf.gradle.ProtobufCSharpConvention
import com.google.protobuf.gradle.TaskGenerator
import com.google.protobuf.gradle.Utils
import javafx.concurrent.Task
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer

import org.gradle.internal.reflect.Instantiator
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.internal.tasks.DefaultSourceSetContainer

import javax.inject.Inject

class ProtobufCSharpPlugin implements Plugin<Project> {

    private final Instantiator instantiator
    private Project project
    private DefaultSourceSetContainer sourceSets

    @Inject
    ProtobufCSharpPlugin(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    void apply(final Project project) {
        this.project = project

        project.apply plugin: 'com.google.protobuf.base'

        sourceSets = instantiator.newInstance(DefaultSourceSetContainer.class, project.fileResolver, project.tasks, instantiator,
            project.services.get(SourceDirectorySetFactory.class));
        sourceSets.create('main')

        project.convention.plugins.csharpProtobuf = new ProtobufCSharpConvention(sourceSets);

        Utils.setupSourceSets(project, sourceSets, 'CSharp')
    }

    /**
     * Adds Protobuf-related tasks to the project.
     */
    void addProtoTasks() {
        sourceSets.all { sourceSet ->
            addTasksForSourceSet(sourceSet)
        }
    }

    /**
     * Performs after task are added and configured
     */
    void afterTaskAdded() {
        linkGenerateProtoTasksToCompile()
    }

    /**
     * Creates Protobuf tasks for a sourceSet
     */
    private addTasksForSourceSet(final SourceSet sourceSet) {
        def generateProtoTask = TaskGenerator.addGenerateProtoTask(project, sourceSet.name, [sourceSet], 'CSharp')
        generateProtoTask.sourceSet = sourceSet
        generateProtoTask.doneInitializing()
        generateProtoTask.builtins {
            csharp {}
        }

        def extractProtosTask = TaskGenerator.maybeAddExtractProtosTask(project, sourceSet.name, 'CSharp')
        generateProtoTask.dependsOn(extractProtosTask)
    }

    private linkGenerateProtoTasksToCompile() {
        def compileTasks = project.tasks.findAll { it.class.name.startsWith 'com.ullink.Msbuild' }
        sourceSets.each { sourceSet ->
            project.protobuf.generateProtoTasks.ofCSharpSourceSet(sourceSet.name).each { generateProtoTask ->
                compileTasks.each { it.dependsOn(generateProtoTask) }
            }
        }
    }
}
