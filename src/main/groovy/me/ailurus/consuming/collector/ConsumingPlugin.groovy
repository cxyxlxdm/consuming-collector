package me.ailurus.consuming.collector

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskInstantiationException

/**
 * Created by Intellij IDEA
 * Author: liang
 * Time: 2018/5/6 11:22 
 */
class ConsumingPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        verifyRequiredPlugins project

        project.extensions.create('collectConsuming', ConsumingExtension)
        registerTransform(project)
    }

    private static void registerTransform(Project project) {
        def extension = project.extensions.getByType(BaseExtension)
        extension.bootClasspath.each {
            project.logger.lifecycle it.path
        }
        def transform = new ConsumingTransform(project, extension.bootClasspath)
        extension.registerTransform(transform)
    }

    private static void verifyRequiredPlugins(Project project) {
        if (!project.plugins.hasPlugin(AppPlugin) && !project.plugins.hasPlugin(LibraryPlugin)) {
            throw new TaskInstantiationException('com.android.application or com.android.library plugin must be applied before')
        }
    }
}
