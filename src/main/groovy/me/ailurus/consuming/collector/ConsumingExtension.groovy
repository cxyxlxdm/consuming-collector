package me.ailurus.consuming.collector

import org.gradle.api.Project

/**
 * Created by Intellij IDEA
 * Author: liang
 * Time: 2018/5/6 11:23 
 */
class ConsumingExtension {

    /**
     * 是否注入
     */
    boolean enable

    /**
     * 需要被注入的类的包名
     */
    List<String>  includePackages

    /**
     * 需要被注入的包里无需被注入的类
     */
    List<String> excludeFies

    static ConsumingExtension getConfig(Project project) {
        ConsumingExtension extension = project.extensions.findByType(ConsumingExtension.class)
        if (extension == null) {
            extension = new ConsumingExtension()
        }
        return extension
    }
}
