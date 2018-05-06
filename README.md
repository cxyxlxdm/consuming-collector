# consuming-collector

Collect the time-consuming of method.

## How to use

1. `gradle clean install`

2. Add the following code in `build.gradle` in the rootProject dir.

    ```Groovy
    buildscript {
        repositories {
            mavenLocal()
        }
        dependencies {
            classpath 'me.ailurus:consuming-collector:1.0.0-SNAPSHOT'
        }
    }
    ```
    
3. Add the following code in `build.gradle` in the subProject dir.

    ```
    apply plugin: 'consuming'
    
    collectConsuming {
        enable = true
        includePackages = ['xxxx', 'xxxx']
        excludeFies = ['xxx', 'xxx']
    }
    ```
    
## Issue

**[Not Support]**: Return type is not void;