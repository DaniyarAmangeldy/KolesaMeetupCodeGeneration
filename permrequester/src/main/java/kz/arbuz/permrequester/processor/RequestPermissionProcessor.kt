package kz.arbuz.permrequester.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import kz.arbuz.permrequester.annotation.OnPermissionResult
import kz.arbuz.permrequester.annotation.PermissionRequired
import kz.arbuz.permrequester.processor.RequestPermissionProcessor.Companion.KOTLIN_GEN_DIRECTORY
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic.Kind.ERROR
import kotlin.properties.Delegates

@AutoService(Processor::class)
class RequestPermissionProcessor: AbstractProcessor() {

    companion object {
        const val KOTLIN_GEN_DIRECTORY = "kapt.kotlin.generated"
        private const val PERMISSION_GRANTED = 0
    }

    private var messager: Messager by Delegates.notNull()

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        messager = processingEnv.messager
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedAnnotationTypes() = setOf(
        OnPermissionResult::class.java.canonicalName,
        PermissionRequired::class.java.canonicalName,
    )

    override fun process(
        annotations: MutableSet<out TypeElement>,
        roundEnv: RoundEnvironment
    ): Boolean {
        roundEnv
            .getElementsAnnotatedWith<PermissionRequired>()
            .filterIsInstance<TypeElement>()
            .forEach { typeElement ->
                // find callback method annotated as @OnPermissionResult
                val nestedElements = typeElement.enclosedElements
                val callbackElement = nestedElements
                    .filterIsInstance<ExecutableElement>()
                    .firstOrNull { it.hasAnnotation<OnPermissionResult>() }
                if (callbackElement == null) {
                    messager.printMessage(
                        ERROR, "@OnPermissionResult is missing or annotated wrong element"
                    )
                    return false
                }
                val requestCode = callbackElement.getAnnotation<OnPermissionResult>().requestCode
                val packageName = processingEnv.elementUtils.getPackageOf(typeElement).toString()
                val className = "${typeElement.simpleName}Generated"
                val classBuilder = buildClass(
                    name = className,
                    typeElement = typeElement,
                    requestCode = requestCode,
                    callbackElement = callbackElement
                )
                val outputPath = processingEnv.options.getValue(KOTLIN_GEN_DIRECTORY)
                FileSpec
                    .builder(packageName, className)
                    .addType(classBuilder.build())
                    .build()
                    .writeTo(File(outputPath))
            }
        return true
    }

    @OptIn(ExperimentalStdlibApi::class,
        DelicateKotlinPoetApi::class
    )
    private fun buildClass(
        name: String,
        typeElement: TypeElement,
        requestCode: Int,
        callbackElement: Element
    ) = TypeSpec
        .objectBuilder(name)
        .addFunction(
            FunSpec
                .builder("onRequestPermissionsResult")
                .addParameter("activity", typeElement.asClassName())
                .addParameter("requestCode", typeNameOf<Int>())
                .addParameter("permissions", typeNameOf<Array<out String>>())
                .addParameter("grantResults", typeNameOf<IntArray>())
                .beginControlFlow("if (requestCode == $requestCode && grantResults.any { it == $PERMISSION_GRANTED })")
                .addStatement("activity.%N()", callbackElement.simpleName)
                .endControlFlow()
                .build()
        )

    private inline fun <reified T: Annotation> Element.hasAnnotation() =
        getAnnotation(T::class.java) != null

    private inline fun <reified T: Annotation> Element.getAnnotation() =
        getAnnotation(T::class.java)

    private inline fun <reified T: Annotation> RoundEnvironment.getElementsAnnotatedWith() =
        getElementsAnnotatedWith(T::class.java)
}