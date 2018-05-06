package me.ailurus.consuming.collector

import com.android.build.api.transform.*
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApkVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 * Created by Intellij IDEA
 * Author: liang
 * Time: 2018/5/6 11:29 
 */
class ConsumingTransform extends Transform {

    private Project project

    ConsumingTransform(Project project, List<File> fileList) {
        this.project = project
        ClassPool classPool = ClassPool.default
        if (classPool != null) {
            fileList.each {
                classPool.insertClassPath(it.absolutePath)
            }
        }
    }

    @Override
    String getName() {
        return Constants.PLUGIN_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        if (project.plugins.hasPlugin(LibraryPlugin)) {
            return TransformManager.SCOPE_FULL_LIBRARY
        }
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        def extension = ConsumingExtension.getConfig(project)
        def enable = false
        def includePackages = []
        def excludeFiles = []

        if (extension.enable) {
            enable = extension.enable
        }

        try {
            if (extension.includePackages && extension.includePackages.size() > 0)
                includePackages.addAll(extension.includePackages)
            if (extension.excludeFies && extension.excludeFies.size() > 0)
                excludeFiles.addAll(extension.excludeFies)
        } catch (Exception e) {
            project.logger.error(e.message)
        }

        includePackages.each {
            project.logger.debug 'includePackage : ' + it
        }

        excludeFiles.each {
            project.logger.debug 'excludeFile : ' + it
        }

        ClassPool pool = ClassPool.default
        if (project.plugins.hasPlugin(AppPlugin)) {
            project.android.applicationVariants.all { ApkVariant variant ->
                def compileJavaWithJavacTask = variant.javaCompiler
                compileJavaWithJavacTask.inputs.files.each {
                    try {
                        pool.insertClassPath(it.absolutePath)
                    } catch (Exception e) {
                        project.logger.error e.message
                    }
                    project.logger.debug 'pool insert Complete path  : ' + it.absolutePath
                }
            }
        }

        def inputs = transformInvocation.inputs
        def outputProvider = transformInvocation.outputProvider

        if (project.plugins.hasPlugin(LibraryPlugin)) {
            project.android.libraryVariants.all { LibraryVariant variant ->
                def compileJavaWithJavacTask = variant.javaCompiler
                compileJavaWithJavacTask.inputs.files.each {
                    if (it.absolutePath.endsWith('.jar') && enable) {
                        ConsumingProcessor.processJar(project, it, includePackages, excludeFiles)
                    }
                }

            }
        }

        inputs.each {
            it.jarInputs.each {
                project.logger.debug 'jar :' + it.file.absolutePath
                if (enable) ConsumingProcessor.processJar(project, it.file, includePackages, excludeFiles)
                File dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.JAR)
                FileUtils.copyFile(it.file, dest)
            }

            it.directoryInputs.each {
                project.logger.debug 'dir :' + it.file.absolutePath
                if (enable) ConsumingProcessor.processClass(project, it.file, includePackages, excludeFiles)
                File dest = outputProvider.getContentLocation(it.name, it.contentTypes, it.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(it.file, dest)
            }
        }
    }
}
