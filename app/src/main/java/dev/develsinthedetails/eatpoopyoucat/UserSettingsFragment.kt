package dev.develsinthedetails.eatpoopyoucat

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class UserSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}