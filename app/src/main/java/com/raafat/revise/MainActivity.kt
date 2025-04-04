package com.raafat.revise

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.raafat.revise.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var webViewManager: WebViewManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var dialogHelper: DialogHelper

    private lateinit var ayaList: List<Aya>
    private var isSpinnerItemClickedBefore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Quran)
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize our singleton in the application
        AppSingleton.initialize(applicationContext)

        // Initialize our helper classes
        webViewManager = WebViewManager(this)
        preferenceManager = PreferenceManager(this)
        dialogHelper = DialogHelper(this)

        // Setup view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
            .get(MainViewModel::class.java)

        // Set up insets for portrait mode only
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Only apply insets in portrait mode
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            } else {
                view.setPadding(0, 0, 0, 0)
            }

            WindowInsetsCompat.CONSUMED
        }

        setupUI()
        setupObservers()

        window.decorView.setBackgroundColor(Color.parseColor("#191919")) // Match the hex color shown in image

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set system bar colors
        window.statusBarColor = Color.parseColor("#FF191919") // Transparent status bar
        window.navigationBarColor = Color.parseColor("#FF191919") // Custom color for nav bar

        // Set light/dark status bar icons based on background
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        
    }

    private fun setupUI() {
        val suraNames = resources.getStringArray(R.array.sura_names)
        val savedSura = preferenceManager.getSelectedSura()
        val savedAya = preferenceManager.getSelectedAya()

        // Initialize spinner
        setupSuraSpinner(suraNames, savedSura)

        // Setup event listeners
        setupEventListeners(savedAya)
    }

    private fun setupSuraSpinner(suraNames: Array<String>, savedSura: Int) {
        val adapter = CustomSpinnerAdapter(this, suraNames)
        binding.suraSpinner.adapter = adapter
        binding.suraSpinner.setSelection(savedSura)
    }

    private fun setupEventListeners(savedAya: Float) {
        // Setup spinner listener
        binding.suraSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                handleSuraSelection(position, savedAya)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        // Setup previous aya button
        binding.previousAya.setOnClickListener {
            lifecycleScope.launch {
                handlePreviousClick()
            }
        }

        // Setup main clicker for next aya
        binding.clicker.setOnClickListener {
            lifecycleScope.launch {
                handleNextClick()
            }
        }

        // Setup hide/show toggle
        binding.hide.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                webViewManager.toggleVisibility(binding.quranContent)
            }
        }

        // Setup slider listeners
        binding.slider.addOnChangeListener { _, value, _ ->
            viewModel.onSliderValueChanged(value)
        }

        binding.slider.addOnSliderTouchListener(object : com.google.android.material.slider.Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: com.google.android.material.slider.Slider) {
                viewModel.onStartTouch()
            }

            override fun onStopTrackingTouch(slider: com.google.android.material.slider.Slider) {
                viewModel.onStopTouch()
                binding.hide.isChecked = false
            }
        })
    }

    private fun setupObservers() {
        // Observe aya list
        lifecycleScope.launch {
            viewModel.ayaListFlow.collect { list ->
                if (list.isNotEmpty()) {
                    ayaList = list
                    lifecycleScope.launch {
                        val savedAya = preferenceManager.getSelectedAya()
                        webViewManager.setupWebView(binding.quranContent)
                        updateAyaContent(savedAya.toInt())
                    }
                }
            }
        }

        // Observe slider value changes
        viewModel.sliderValue.observe(this) { value ->
            updateAyaInfo(value)
        }

        // Observe touch state
        viewModel.isTouching.observe(this) { isTouching ->
            if (!isTouching && ::ayaList.isInitialized) {
                lifecycleScope.launch {
                    updateAyaContent(binding.slider.value.toInt())
                }
            }
        }
    }

    private fun handleSuraSelection(position: Int, savedAya: Float) {
        if (isSpinnerItemClickedBefore) {
            binding.hide.isChecked = false

            binding.slider.value = 1f
            binding.slider.valueFrom = 1f
            binding.slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()

            binding.pageCount.text = getString(R.string.page_word_ar).plus(pageForSuraArray[position].toString())
            binding.ayaCount.text = getString(R.string.aya_word_ar).plus(1)

            if (::ayaList.isInitialized) {
                lifecycleScope.launch {
                    updateAyaContent(1)
                }
            }
        } else {
            isSpinnerItemClickedBefore = true
            binding.slider.valueFrom = 1f
            binding.slider.valueTo = numberOfAyahsForSuraArray[position].toFloat()
            binding.slider.value = savedAya

            if (::ayaList.isInitialized) {
                lifecycleScope.launch {
                    updateAyaContent(savedAya.toInt())
                }
            }
        }
    }

    private suspend fun handlePreviousClick() {
        if (!binding.hide.isChecked) {
            // Normal mode - scroll or previous aya
            if (webViewManager.hasScrollOverflow(binding.quranContent)) {
                webViewManager.scrollToTop(binding.quranContent)
            } else if (binding.slider.value > 1) {
                binding.slider.value -= 1
                updateAyaContent(binding.slider.value.toInt())
            } else {
                checkPreviousSura()
            }
        } else {
            // Hide mode - previous word or previous aya
            val hasPrev = webViewManager.showPrevWord(binding.quranContent, binding.slider.value.toInt())
            if (hasPrev && binding.slider.value > 1) {
                binding.slider.value -= 1
                updateAyaContentPrev(binding.slider.value.toInt())
            }
        }
    }

    private suspend fun handleNextClick() {
        if (!binding.hide.isChecked) {
            // Normal mode - scroll or next aya
            if (webViewManager.isScrolledToBottom(binding.quranContent)) {
                webViewManager.scrollToNextPage(binding.quranContent)
            } else if (binding.slider.value < binding.slider.valueTo) {
                binding.slider.value += 1
                updateAyaContent(binding.slider.value.toInt())
            } else {
                checkNextSura()
            }
        } else {
            // Hide mode - next word or next aya
            val hasNext = webViewManager.showNextWord(binding.quranContent)
            if (hasNext && binding.slider.value < binding.slider.valueTo) {
                binding.slider.value += 1
                updateAyaContent(binding.slider.value.toInt())
            }
        }
    }

    private fun checkPreviousSura() {
        if (binding.suraSpinner.selectedItemPosition > 0) {
            val prevSuraPosition = binding.suraSpinner.selectedItemPosition - 1
            val sura = (binding.suraSpinner.adapter.getItem(prevSuraPosition) as String).substringAfter(" ")

            dialogHelper.showCustomDialog(
                message = getString(R.string.go_to).plus(sura),
                onPositiveClick = {
                    binding.suraSpinner.setSelection(prevSuraPosition)
                }
            )
        }
    }

    private fun checkNextSura() {
        if (binding.suraSpinner.selectedItemPosition < binding.suraSpinner.adapter.count - 1) {
            val nextSuraPosition = binding.suraSpinner.selectedItemPosition + 1
            val sura = (binding.suraSpinner.adapter.getItem(nextSuraPosition) as String).substringAfter(" ")

            dialogHelper.showCustomDialog(
                message = getString(R.string.go_to).plus(sura),
                onPositiveClick = {
                    binding.suraSpinner.setSelection(nextSuraPosition)
                }
            )
        }
    }

    private suspend fun updateAyaContent(ayaNo: Int) {
        try {
            val currentAya = ayaList.first {
                it.sora == (binding.suraSpinner.selectedItemPosition + 1) && it.ayaNo == ayaNo
            }
            updateAyaInfo(ayaNo.toFloat())
            webViewManager.displayAya(binding.quranContent, currentAya)
        } catch (e: NoSuchElementException) {
            // Handle the case where the aya is not found
            // This is a safeguard against potential data inconsistencies
            e.printStackTrace()
        }
    }

    private suspend fun updateAyaContentPrev(ayaNo: Int) {
        try {
            val currentAya = ayaList.first {
                it.sora == (binding.suraSpinner.selectedItemPosition + 1) && it.ayaNo == ayaNo
            }
            updateAyaInfo(ayaNo.toFloat())
            webViewManager.displayAyaPrev(binding.quranContent, currentAya)
        } catch (e: NoSuchElementException) {
            // Handle the case where the aya is not found
            e.printStackTrace()
        }
    }

    private fun updateAyaInfo(value: Float) {
        if (!::ayaList.isInitialized) return

        try {
            val chosenAyah = ayaList.first {
                it.sora == (binding.suraSpinner.selectedItemPosition + 1) && it.ayaNo == value.toInt()
            }
            binding.pageCount.text = getString(R.string.page_word_ar).plus(chosenAyah.page.toString())
            binding.ayaCount.text = getString(R.string.aya_word_ar).plus(chosenAyah.ayaNo.toString())
            binding.slider.value = chosenAyah.ayaNo.toFloat()
        } catch (e: NoSuchElementException) {
            // Handle the case where the aya is not found
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.saveSelectedSura(binding.suraSpinner.selectedItemPosition)
        preferenceManager.saveSelectedAya(binding.slider.value)
    }

    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)

        super.attachBaseContext(newBase)
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogHelper.dismissActiveDialog()
    }
}