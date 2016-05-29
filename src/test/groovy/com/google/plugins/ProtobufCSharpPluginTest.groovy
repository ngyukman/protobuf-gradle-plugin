package com.google.protobuf.gradle.plugins

import com.google.protobuf.gradle.GenerateProtoTask
import com.google.protobuf.gradle.ProtobufExtract
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ProtobufCSharpPluginTest extends Specification {

    @Rule
    final TemporaryFolder tempDir = new TemporaryFolder()

    def "Applying com.google.protobuf.csharp adds corresponding task to project"() {
        given: "a basic project with com.google.protobuf.csharp"
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.google.protobuf.csharp'

        when: "project evaluated"
        project.evaluate()

        then: "generate tasks added"
        assert project.tasks.generateProtoCSharp instanceof GenerateProtoTask
        assert project.tasks.extractProtoCSharp instanceof ProtobufExtract
    }

    def "Custom sourceSet should get its own GenerateProtoTask"() {
        given: "a basic project with com.google.protobuf.csharp"
        Project project = ProjectBuilder.builder().build()
        project.apply plugin: 'com.google.protobuf.csharp'

        when: "adding custom sourceSet nano"
        project.csharpSourceSets.create('nano')

        and: "project evaluated"
        project.evaluate()

        then: "tasks for nano added"
        assert project.tasks.generateNanoProtoCSharp instanceof GenerateProtoTask
        assert project.tasks.extractNanoProtoCSharp instanceof ProtobufExtract
    }

    // MSBuild 14 / Mono has to be installed for testing
    def "testProjectCSharp should be successfully executed"() {
        given: "project from testProjectCSharp"
        def projectDir = tempDir.newFolder()
        ProtobufPluginTestHelper.copyTestProject('testProjectCSharp', projectDir)

        when: "build is invoked"
        def result = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments('build')
            .build()

        then: "it succeed"
        result.task(":msbuild").outcome == TaskOutcome.SUCCESS
    }
}