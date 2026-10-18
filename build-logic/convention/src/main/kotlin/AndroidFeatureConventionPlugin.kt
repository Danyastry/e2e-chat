import com.danyastry.e2echat.buildlogic.library
import com.danyastry.e2echat.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("e2echat.android.library.compose")

        dependencies {
            add("implementation", project(":core:crypto"))
            add("implementation", project(":core:protocol"))
            add("implementation", project(":core:ui"))
            add("implementation", libs.library("androidx-material3"))
            add("implementation", libs.library("androidx-lifecycle-viewmodel-compose"))
            add("implementation", libs.library("androidx-lifecycle-runtime-compose"))
            add("testImplementation", libs.library("kotlinx-coroutines-test"))
        }
    }
}
