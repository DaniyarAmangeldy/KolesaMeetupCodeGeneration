package kz.arbuz.permrequester.annotation

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
annotation class OnPermissionResult(
    val requestCode: Int
)