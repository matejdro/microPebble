import de.mobilej.unmock.UnMockExtension
import org.gradle.accessors.dm.LibrariesForLibs

val libs = the<LibrariesForLibs>()

apply(plugin = "de.mobilej.unmock")

configure<UnMockExtension> {
   keepStartingWith("android.content.ComponentName")
   keepStartingWith("android.content.Intent")
   keepStartingWith("android.content.ContentProviderOperation")
   keepStartingWith("android.content.ContentProviderResult")
   keepStartingWith("android.content.ContentUris")
   keepStartingWith("android.content.ContentValues")
   keep("android.content.Context")
   keepStartingWith("android.content.res.Configuration")
   keepStartingWith("android.content.UriMatcher")
   keep("android.database.AbstractCursor")
   keep("android.database.CrossProcessCursor")
   keepStartingWith("android.database.MatrixCursor")
   keep("android.location.Location")
   keep("android.net.Uri")
   keep("android.net.UriCodec")
   keep("android.os.BaseBundle")
   keep("android.os.Bundle")
   keep("android.os.BadTypeParcelableException")
   keepStartingWith("android.text.")
   keepStartingWith("android.util.")
   keep("android.view.ContextThemeWrapper")
   keep("android.widget.BaseAdapter")
   keep("android.widget.ArrayAdapter")
   keepStartingWith("com.android.internal.R")
   keepStartingWith("com.android.internal.util.")
   keepStartingWith("org.")
   keepStartingWith("libcore.")
}

dependencies {
   add("unmock", libs.unmock.androidJar)
}
