package com.y3owk1n.neru.android

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val padding = (24 * resources.displayMetrics.density).toInt()
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(padding, padding, padding, padding)
            setBackgroundColor(Color.rgb(238, 242, 255))
        }

        content.addView(TextView(this).apply {
            text = "Neru for Android"
            textSize = 28f
            setTextColor(Color.rgb(23, 50, 122))
            gravity = Gravity.CENTER
        }, matchWidth())

        content.addView(TextView(this).apply {
            text = "Enable the accessibility service, connect a physical keyboard, then press Ctrl+Shift+G from any app.\n\n" +
                "Grid labels: A S D F G H J K L\n" +
                "Type row, column, then subgrid key\n" +
                "Return: tap target\n" +
                "Backspace: previous level\n" +
                "Escape: close"
            textSize = 17f
            setTextColor(Color.rgb(10, 19, 56))
            gravity = Gravity.CENTER
            setPadding(0, padding, 0, padding)
        }, matchWidth())

        content.addView(Button(this).apply {
            text = "Open Accessibility Settings"
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }, matchWidth())

        setContentView(content)
    }

    private fun matchWidth() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT,
    )
}
