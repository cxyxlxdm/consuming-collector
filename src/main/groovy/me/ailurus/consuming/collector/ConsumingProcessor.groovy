package me.ailurus.consuming.collector

import com.android.build.gradle.BaseExtension
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

/**
 * Created by Intellij IDEA
 * Author: liang
 * Time: 2018/5/6 11:53 
 */
class ConsumingProcessor {

    static void processJar(Project project, File jarFile, List<String> includePackages, List<String> excludeFiles) {
        if (!(jarFile && jarFile.isFile())) {
            return
        }
        def optJar = new File(jarFile.parent, jarFile.name + '.opt')
        def file = new JarFile(jarFile)
        def enumeration = file.entries()
        def jarOutputStream = new JarOutputStream(new FileOutputStream(optJar))

        while (enumeration.hasMoreElements()) {
            def jarEntry = enumeration.nextElement()
            def entryName = jarEntry.name
            def baseExtension = project.extensions.findByType(BaseExtension.class)
            def excludes = baseExtension.packagingOptions.excludes
            if (!excludes.contains(entryName) && matchEntryName(entryName)) {
                def zipEntry = new ZipEntry(entryName)
                def inputStream = file.getInputStream(jarEntry)
                jarOutputStream.putNextEntry(zipEntry)

                project.logger.info 'entryName in ' + jarFile.name + ' : ' + entryName
                if (!entryName.endsWith('.class')) return
                def classPool = ClassPool.default
                def ctClass = classPool.makeClass(inputStream, false)
                if (ctClass.isFrozen()) ctClass.defrost()
                if (fileRegular(ctClass, includePackages, excludeFiles)) {
                    ctClass.declaredMethods.each {
                        injectCode(it, project)
                    }
                    def bytes = ctClass.toBytecode()
                    jarOutputStream.write(bytes)
                } else {
                    jarOutputStream.write(IOUtils.toByteArray(inputStream))
                }

                jarOutputStream.closeEntry()
                inputStream.close()
            }
        }

        jarOutputStream.close()
        file.close()

        if (jarFile.exists()) jarFile.delete()
        optJar.renameTo(jarFile)
    }

    static void processClass(Project project, File file, List<String> includePackages, List<String> excludeFiles) {
        if (!(file && file.exists())) {
            return
        }
        file.eachFileRecurse {
            if (!it.isDirectory() && matchClassName(it.name)) {
                def classPool = ClassPool.getDefault()
                def inputStream = new FileInputStream(it)
                def ctClass = classPool.makeClass(inputStream, false)
                if (ctClass.isFrozen()) ctClass.defrost()

                def optFile = new File(it.parent, it.name + '.opt')
                def outputStream = new FileOutputStream(optFile)

                if (fileRegular(ctClass, includePackages, excludeFiles)) {
                    ctClass.declaredMethods.each {
                        injectCode(it, project)
                    }
                }

                def bytes = ctClass.toBytecode()
                outputStream.write(bytes)

                if (inputStream) inputStream.close()
                if (outputStream) {
                    outputStream.close()
                    if (it.exists()) it.delete()
                    optFile.renameTo(it)
                }
                project.logger.info 'the fully-qualified name of the class is : ' + ctClass.name
            }
        }
    }

    static void injectCode(CtMethod ctMethod, Project project) {
        try {
            ctMethod.addLocalVariable('startCollectConsuming', CtClass.longType)
            ctMethod.insertBefore(Constants.BEFORE_METHOD + Constants.LOGCAT_BEFORE_DEBUG + ctMethod.name + Constants.LOGCAT_AFTER_START)
            ctMethod.addLocalVariable('endCollectConsuming', CtClass.longType)
            ctMethod.addLocalVariable('durationCollectConsuming', CtClass.longType)
            ctMethod.insertAfter(Constants.AFTER_METHOD + Constants.LOGCAT_BEFORE_DEBUG + ctMethod.name + Constants.LOGCAT_AFTER_END
                    + Constants.LOGCAT_DURATION + Constants.LOGCAT_BEFORE_WARN + ctMethod.name + Constants.LOGCAT_AFTER_DURATION)
        } catch (Exception e) {
            project.logger.error e.message
        }
    }

    static boolean fileRegular(CtClass ctClass, List<String> includePackages, List<String> excludeFiles) {
        if (includePackages.contains(ctClass.packageName) && !excludeFiles.contains(ctClass.name))
            true
        else
            false
    }

    static boolean matchEntryName(String entryName) {
        return !entryName.contains('/R\$') && !entryName.endsWith('/R.class') &&
                !entryName.endsWith('/BuildConfig.class') && !entryName.contains('android/support/')
    }

    static boolean matchClassName(String className) {
        return !className.endsWith('R.class') && !className.contains('R\$') &&
                !className.endsWith('BuildConfig.class') && !className.contains('android.support')
    }
}
