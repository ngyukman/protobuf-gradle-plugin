package com.google.protobuf.gradle

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.SourceSet
import org.gradle.api.internal.tasks.DefaultSourceSetContainer

class ProtobufCSharpConvention {
    def ProtobufCSharpConvention(DefaultSourceSetContainer sourceSets) {
        csharpSourceSets = sourceSets
    }

    def final DefaultSourceSetContainer csharpSourceSets

    public NamedDomainObjectContainer<SourceSet> csharpSourceSets(Closure closure) {
        return csharpSourceSets.configure(closure);
    }
}
