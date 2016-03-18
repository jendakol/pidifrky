package cz.jenda.pidifrky.ui

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.support.v7.preference.{ListPreference, Preference, PreferenceFragmentCompat, PreferenceScreen}
import android.view.View
import cz.jenda.pidifrky.R
import cz.jenda.pidifrky.logic._
import cz.jenda.pidifrky.logic.dbimport.DbImporter
import cz.jenda.pidifrky.ui.api.{BasicActivity, Toast}
import cz.jenda.pidifrky.ui.dialogs.NormalProgressDialog

import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
 * @author Jenda Kolena, jendakolena@gmail.com
 */
class SettingsActivity extends BasicActivity with PreferenceFragmentCompat.OnPreferenceStartScreenCallback {

  private val titleStack = new mutable.Stack[CharSequence]()

  override protected def onCreate(savedInstanceState: Bundle): Unit = {
    super.onCreate(savedInstanceState)

    if (savedInstanceState == null) {
      val manager = getSupportFragmentManager

      val fragment = Option(manager.findFragmentByTag(SettingsFragment.FragmentTag)).getOrElse(new SettingsFragment)

      manager.beginTransaction()
        .replace(android.R.id.content, fragment, SettingsFragment.FragmentTag)
        .commit()
    }

    setTitle(R.string.button_appsettings)
  }

  override def onPreferenceStartScreen(preferenceFragmentCompat: PreferenceFragmentCompat, preferenceScreen: PreferenceScreen): Boolean = {
    val key = preferenceScreen.getKey

    val ft = getSupportFragmentManager.beginTransaction()
    val fragment = new SettingsFragment
    val args = new Bundle()

    args.putString(PreferenceFragmentCompat.ARG_PREFERENCE_ROOT, key)
    fragment.setArguments(args)
    ft.add(android.R.id.content, fragment, key)
    ft.addToBackStack(key)
    ft.commit()

    titleStack.push(getTitle)
    setTitle(preferenceScreen.getTitle)

    true
  }

  override protected def upButtonClicked(): Unit = {
    //go to previous screen, or to parent activity
    try {
      if (!getSupportFragmentManager.popBackStackImmediate()) {
        super.upButtonClicked()
      }
      else {
        //previous pref screen loaded
        setTitle(titleStack.pop())
      }
    }
    catch {
      case NonFatal(e) =>
        DebugReporter.debugAndReport(e, "Error while going back in preferences")
        //close the activity
        this.finish()
    }
  }
}

class SettingsFragment extends PreferenceFragmentCompat with SharedPreferences.OnSharedPreferenceChangeListener {

  import Application.executionContext

  override def onCreatePreferences(bundle: Bundle, rootKey: String): Unit = {
    setPreferencesFromResource(R.xml.settings, rootKey)

    //hide the debug menu if the app is not in debug mode
    Option(findPreference("DEBUG_MENU")).foreach(_.setVisible(Utils.isDebug))

    PidifrkySettings.sharedPreferences.foreach { sharedPreferences =>

      val screen = getPreferenceManager.getPreferenceScreen
      for (i <- 0 to screen.getPreferenceCount - 1) {
        val pref = screen.getPreference(i)

        onSharedPreferenceChanged(sharedPreferences, pref.getKey)
      }
    }
  }

  override def onResume(): Unit = {
    super.onResume()

    PidifrkySettings.sharedPreferences.foreach(_.registerOnSharedPreferenceChangeListener(this))
  }

  override def onPause(): Unit = {
    super.onPause()

    PidifrkySettings.sharedPreferences.foreach(_.unregisterOnSharedPreferenceChangeListener(this))
  }

  override def onViewCreated(view: View, savedInstanceState: Bundle): Unit = {
    super.onViewCreated(view, savedInstanceState)

    //to fix the transparent background
    //this is very ugly, but there is some fu..ng bug which causes the color to be purple instead of grey, so I have to hardcode the value here because I have no more power to solve the bug...
    view.setBackgroundColor(Color.parseColor("#616161"))
  }

  override def onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String): Unit = {
    Option(findPreference(key)) match {
      case Some(p: ListPreference) =>
        val index = p.findIndexOfValue(sharedPreferences.getString(key, ""))
        if (index >= 0) {
          p.setSummary(p.getEntries()(index))
        }

      case _ => //ok, not showing
    }
  }

  override def onPreferenceTreeClick(preference: Preference): Boolean = {
    Application.withCurrentContext { implicit ctx =>
      preference.getKey match {
        case "download_db" =>
          val dialog = NormalProgressDialog('testing, R.string.downloading_database, R.string.processing_cards, 100, cancellable = false)

          val progressListener = ProgressListener.forDialog(dialog)

          dialog.show()

          DbImporter.update(progressListener).andThen {
            case Success(_) =>
              dialog.dismiss()
              Toast("DB updated!", 3000)
            case Failure(e) =>
              Toast(Format(e), 3000)
              DebugReporter.debug(e)
              dialog.dismiss()
          }
          true

        case "download_images" =>
          DbImporter.downloadImages(fullImages = false)
          Toast("Downloading images", Toast.Long)(getActivity)
          true

        case "debug_clear_preferences" =>
          Application.initSettingsDefault(clear = true)
          Toast("Preferences reset", Toast.Long)(getActivity)
          true

        case "debug_send" =>
          DebugReporter.sendIfAllowed()
          Toast("Preferences reset", Toast.Long)(getActivity)
          true

        case _ => super.onPreferenceTreeClick(preference)
      }
    }.getOrElse(super.onPreferenceTreeClick(preference))
  }
}

object SettingsFragment {
  final var FragmentTag = getClass.getName
}
